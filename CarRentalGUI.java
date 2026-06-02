import databasehandler.DBHandler;
import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CarRentalGUI extends JFrame {
    private CarRentalSystem system;
    private JTextArea logArea;
    private JTable dbTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> userBookBox;
    private JComboBox<String> userRetBox;
    private JComboBox<String> adminUserBox;
    private JComboBox<String> userWalletBox;
    private Map<String, SystemPayment> userWallets;

    public CarRentalGUI() {
        // 1. Inizializzazione Sistema
        DBHandler.ConnectAndInitialize();
        system = CarRentalSystem.GetIstance();

        // 2. Setup Finestra
        setTitle("Car Sharing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inizializzazione ComboBox Utenti
        userBookBox = new JComboBox<>();
        userRetBox = new JComboBox<>();
        adminUserBox = new JComboBox<>();
        userWalletBox = new JComboBox<>();
        userWallets = new HashMap<>();
        refreshUserCombo();

        // 3. Creazione Tab (Admin e User)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Amministrazione", createAdminPanel());
        tabbedPane.addTab("Area Clienti", createUserPanel());
        tabbedPane.addTab("Vista Database", createDatabasePanel());

        add(tabbedPane, BorderLayout.CENTER);

        // 4. Area Log (in basso)
        logArea = new JTextArea(5, 20);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log Operazioni"));
        add(logScroll, BorderLayout.SOUTH);

        setSize(900, 600);
        setLocationRelativeTo(null); // Centra la finestra comodamente sullo schermo
        setVisible(true);
    }

   // --- PANNELLO ADMIN ---
    private JPanel createAdminPanel() {
        // Layout a 4 righe
        JPanel panel = new JPanel(new GridLayout(5, 1)); 

        // 1. Sezione Crea Parcheggio
        JPanel parkPanel = new JPanel(new FlowLayout());
        parkPanel.setBorder(BorderFactory.createTitledBorder("Nuovo Parcheggio"));
        JTextField parkNameField = new JTextField(10);
        JButton btnAddPark = new JButton("Crea Parcheggio");
        
        btnAddPark.addActionListener(e -> {
            String nome = parkNameField.getText();
            if (!nome.isEmpty()) {
                system.CreateParkingLot(nome);
                log("Parcheggio creato: " + nome);
                refreshTable("PARCHEGGIO");
                parkNameField.setText(""); // Pulisce il campo
            }
        });
        parkPanel.add(new JLabel("Nome Parcheggio:"));
        parkPanel.add(parkNameField);
        parkPanel.add(btnAddPark);

        // 2. Sezione Aggiungi Auto 
        JPanel carPanel = new JPanel(new FlowLayout());
        carPanel.setBorder(BorderFactory.createTitledBorder("Nuova Auto"));
        JTextField targaField = new JTextField(8);
        JTextField modelField = new JTextField(10);
        
        //Menu a tendina per scegliere il tipo di auto
        String[] tipiAuto = {"Suv", "Station Wagon"};
        JComboBox<String> tipoBox = new JComboBox<>(tipiAuto);
        
        JTextField parkField = new JTextField(10);
        JButton btnAddCar = new JButton("Aggiungi Auto");

        btnAddCar.addActionListener(e -> {
            String tipoScelto = (String) tipoBox.getSelectedItem();
            String tipoFormattato = tipoScelto.replace(" ", ""); 
            system.AddNewCar(targaField.getText(), modelField.getText(), tipoFormattato, parkField.getText());
            
            log("Auto aggiunta: " + targaField.getText() + " [" + tipoScelto + "]");
            refreshTable("AUTO");
            
            // Pulisce i campi
            targaField.setText("");
            modelField.setText("");
            parkField.setText("");
        });
        
        carPanel.add(new JLabel("Targa:"));
        carPanel.add(targaField);
        carPanel.add(new JLabel("Modello:"));
        carPanel.add(modelField);
        
        // Aggiungiamo la scelta del tipo nella grafica
        carPanel.add(new JLabel("Tipo:")); 
        carPanel.add(tipoBox);             
        
        carPanel.add(new JLabel("Parcheggio:"));
        carPanel.add(parkField);
        carPanel.add(btnAddCar);

        // 3. Sezione Utenti 
        JPanel userPanel = new JPanel(new FlowLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("Registra Nuovo Utente"));
        
        JTextField userNameField = new JTextField(15);
        JCheckBox subCheck = new JCheckBox("Abbonato"); // Checkbox per l'abbonamento
        JButton btnAddUser = new JButton("Salva Utente");
        
        btnAddUser.addActionListener(e -> {
            String nome = userNameField.getText();
            boolean isAbbonato = subCheck.isSelected();

            if (!nome.isEmpty()) {
                if (userExists(nome)) {
                    JOptionPane.showMessageDialog(this, "Attenzione: L'utente " + nome + " è già registrato!", "Errore Duplicato", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Inseriamo l'utente nel database
                DBHandler.InsertUser(nome, isAbbonato);
                
                log("Utente registrato: " + nome + (isAbbonato ? " (Abbonato)" : ""));
                refreshTable("USER");
                refreshUserCombo();
                
                // Pulisce i campi
                userNameField.setText("");
                subCheck.setSelected(false);
            } else {
                JOptionPane.showMessageDialog(this, "Inserisci un nome valido!");
            }
        });

        userPanel.add(new JLabel("Nome Cognome:"));
        userPanel.add(userNameField);
        userPanel.add(subCheck);
        userPanel.add(btnAddUser);

        // 4. Sezione Modifica Abbonamento
        JPanel updatePanel = new JPanel(new FlowLayout());
        updatePanel.setBorder(BorderFactory.createTitledBorder("Gestione Abbonamento"));
        JCheckBox updateSubCheck = new JCheckBox("Abbonato");
        JButton btnUpdateUser = new JButton("Aggiorna");
        
        adminUserBox.addActionListener(e -> {
             String name = (String) adminUserBox.getSelectedItem();
             if(name != null) updateSubCheck.setSelected(isUserAbbonato(name));
        });

        btnUpdateUser.addActionListener(e -> {
            String nome = (String) adminUserBox.getSelectedItem();
            if (nome != null) {
                boolean isAbbonato = updateSubCheck.isSelected();
                updateUserSubscription(nome, isAbbonato);
                log("Abbonamento aggiornato per " + nome + ": " + (isAbbonato ? "Sì" : "No"));
                refreshTable("USER");
            }
        });
        
        updatePanel.add(new JLabel("Utente:"));
        updatePanel.add(adminUserBox);
        updatePanel.add(updateSubCheck);
        updatePanel.add(btnUpdateUser);

        // 5. Sezione Reset DB 
        JPanel resetPanel = new JPanel(new FlowLayout());
        resetPanel.setBorder(BorderFactory.createTitledBorder("Gestione Dati"));
        JButton btnReset = new JButton("RESETTA TUTTO IL DB");
        btnReset.setBackground(Color.RED);
        btnReset.setForeground(Color.WHITE);
        // Forza il sistema a mostrare il colore annullando il rendering nativo del bottone
        btnReset.setOpaque(true);
        btnReset.setBorderPainted(false);
        
        btnReset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Sei sicuro di voler cancellare TUTTI i dati?", 
                "Conferma Reset", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                DBHandler.ClearDatabase();
                log("DATABASE RESETTATO COMPLETAMENTE.");
                tableModel.setRowCount(0); 
                tableModel.setColumnCount(0);
                
                // Svuota i menu a tendina perché ora il database è vuoto
                refreshUserCombo();
            }
        });
        resetPanel.add(btnReset);

        // Aggiunta dei pannelli al layout principale
        panel.add(parkPanel);
        panel.add(carPanel);
        panel.add(userPanel);
        panel.add(updatePanel);
        panel.add(resetPanel);
        
        return panel;
    }

    // --- PANNELLO USER ---
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));

        // Sezione A: Prenota
        JPanel bookPanel = new JPanel(new FlowLayout());
        bookPanel.setBorder(BorderFactory.createTitledBorder("Prenota Auto"));
        JTextField carBookField = new JTextField(10);
        JTextField hoursField = new JTextField(3);
        JButton btnBook = new JButton("Prenota");

        btnBook.addActionListener(e -> {
            try {
                // Simuliamo il recupero dell'oggetto utente 
                User u = new User((String) userBookBox.getSelectedItem());
                if (isUserAbbonato(u.GetName())) {
                    u.Payabbonamento();
                }
                
                int h = Integer.parseInt(hoursField.getText());
                system.Bookcar(u, carBookField.getText(), h);
                log("Prenotazione richiesta per " + u.GetName());
                refreshTable("NOLEGGI");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });

        bookPanel.add(new JLabel("Utente:"));
        bookPanel.add(userBookBox);
        bookPanel.add(new JLabel("Targa Auto:"));
        bookPanel.add(carBookField);
        bookPanel.add(new JLabel("Ore:"));
        bookPanel.add(hoursField);
        bookPanel.add(btnBook);

        // Sezione B: Configura Pagamento
        JPanel walletPanel = new JPanel(new FlowLayout());
        walletPanel.setBorder(BorderFactory.createTitledBorder("Imposta Metodo di Pagamento"));
        
        String[] types = {"Bancomat", "Carta di Credito"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        JTextField codeField = new JTextField(12);
        JButton btnSaveMethod = new JButton("Salva Metodo");

        btnSaveMethod.addActionListener(e -> {
            String user = (String) userWalletBox.getSelectedItem();
            String type = (String) typeBox.getSelectedItem();
            String code = codeField.getText();
            
            if (user != null && !code.isEmpty()) {
                SystemPayment method;
                if ("Bancomat".equals(type)) {
                    method = new BancomatPayment(code);
                } else {
                    method = new CreditCardPayment(code);
                }
                userWallets.put(user, method);
                log("Metodo di pagamento salvato per " + user);
                codeField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Dati mancanti!");
            }
        });
        
        walletPanel.add(new JLabel("Utente:"));
        walletPanel.add(userWalletBox);
        walletPanel.add(new JLabel("Tipo:"));
        walletPanel.add(typeBox);
        walletPanel.add(new JLabel("Codice:"));
        walletPanel.add(codeField);
        walletPanel.add(btnSaveMethod);

        // Sezione C: Restituisci
        JPanel returnPanel = new JPanel(new FlowLayout());
        returnPanel.setBorder(BorderFactory.createTitledBorder("Restituisci Auto"));
        JTextField parkRetField = new JTextField(10);
        JTextField kmField = new JTextField(5);
        JButton btnReturn = new JButton("Restituisci");

        btnReturn.addActionListener(e -> {
            try {
                String userName = (String) userRetBox.getSelectedItem();
                if (!userWallets.containsKey(userName)) {
                    JOptionPane.showMessageDialog(this, "Nessun metodo di pagamento configurato per " + userName);
                    return;
                }
                
                User u = new User(userName);
                double km = Double.parseDouble(kmField.getText());
                String park = parkRetField.getText();
                
                SystemPayment method = userWallets.get(userName);

                system.ReturnCar(u, park, km, method);
                log("Auto restituita da " + u.GetName());
                refreshTable("PAGAMENTI");
            } catch (Exception ex) {
                log("Errore restituzione: " + ex.toString());
            }
        });

        returnPanel.add(new JLabel("Utente:"));
        returnPanel.add(userRetBox);
        returnPanel.add(new JLabel("Parcheggio Arrivo:"));
        returnPanel.add(parkRetField);
        returnPanel.add(new JLabel("Km Fatti:"));
        returnPanel.add(kmField);
        returnPanel.add(btnReturn);

        panel.add(bookPanel);
        panel.add(walletPanel);
        panel.add(returnPanel);
        return panel;
    }

    // --- PANNELLO DATABASE (Tabella Dinamica) ---
    private JPanel createDatabasePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] tables = {"AUTO", "PARCHEGGIO", "NOLEGGI", "PAGAMENTI", "UTENTE"};
        JComboBox<String> tableSelector = new JComboBox<>(tables);
        JButton btnLoad = new JButton("Carica Tabella");

        tableModel = new DefaultTableModel();
        dbTable = new JTable(tableModel);
        
        btnLoad.addActionListener(e -> {
            refreshTable((String) tableSelector.getSelectedItem());
        });

        JPanel topBar = new JPanel();
        topBar.add(new JLabel("Seleziona Tabella:"));
        topBar.add(tableSelector);
        topBar.add(btnLoad);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(dbTable), BorderLayout.CENTER);

        return panel;
    }

    // Funzione per loggare messaggi
    private void log(String msg) {
        logArea.append(msg + "\n");
        // Sposta lo scroll in basso
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // Funzione magica per caricare i dati dal DB alla JTable
    private void refreshTable(String tableName) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:carsharing.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            Vector<String> columnNames = new Vector<>();
            // Aggiungi nomi colonne
            for (int i = 1; i <= colCount; i++) {
                columnNames.add(meta.getColumnName(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            // Aggiungi righe
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }
            
            tableModel.setDataVector(data, columnNames);
        } catch (SQLException e) {
            log("Errore lettura DB: " + e.getMessage());
        }
    }

    private void refreshUserCombo() {
        Vector<String> users = new Vector<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:carsharing.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT NOME FROM USER")) {
            while (rs.next()) {
                users.add(rs.getString("NOME"));
            }
        } catch (SQLException e) {
            log("Errore aggiornamento utenti: " + e.getMessage());
        }
        
        userBookBox.setModel(new DefaultComboBoxModel<>(users));
        userRetBox.setModel(new DefaultComboBoxModel<>(users));
        adminUserBox.setModel(new DefaultComboBoxModel<>(users));
        userWalletBox.setModel(new DefaultComboBoxModel<>(users));
    }

    private boolean userExists(String name) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:carsharing.db");
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM USER WHERE NOME = ?")) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log("Errore verifica esistenza utente: " + e.getMessage());
            return false;
        }
    }

    private void updateUserSubscription(String name, boolean isAbbonato) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:carsharing.db");
             PreparedStatement pstmt = conn.prepareStatement("UPDATE USER SET ABBONATO = ? WHERE NOME = ?")) {
            pstmt.setBoolean(1, isAbbonato);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log("Errore aggiornamento abbonamento: " + e.getMessage());
        }
    }

    private boolean isUserAbbonato(String name) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:carsharing.db");
             PreparedStatement pstmt = conn.prepareStatement("SELECT ABBONATO FROM USER WHERE NOME = ?")) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean("ABBONATO");
            }
        } catch (SQLException e) {
            log("Errore verifica abbonamento: " + e.getMessage());
        }
        return false;
    }
}