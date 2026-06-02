package databasehandler;

import java.sql.*;

public class DBHandler {
    private static final String protocol = "jdbc:sqlite:carsharing.db";

    public static void ConnectAndInitialize() {
        try (Connection connect = DriverManager.getConnection(protocol)) {
            if (connect != null) {
                System.out.println("Connessione al DB");
                CreateTable(connect);
                connect.close();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void CreateTable(Connection c) throws SQLException {
        Statement stmt = c.createStatement();

        String sqlUser = "CREATE TABLE IF NOT EXISTS USER ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "NOME TEXT NOT NULL, "
                + "ABBONATO BOOLEAN DEFAULT 0"
                + ");";
        stmt.execute(sqlUser);

        String sqlParking = "CREATE TABLE IF NOT EXISTS PARCHEGGIO ("
                + "NOME TEXT PRIMARY KEY, "
                + "AUTO_PRESENTE TEXT"
                + ");";
        stmt.execute(sqlParking);

        String sqlCar = "CREATE TABLE IF NOT EXISTS AUTO ("
                + "TARGA TEXT PRIMARY KEY, "
                + "MODELLO TEXT NOT NULL, "
                + "IN_PARCHEGGIO TEXT, "
                + "FOREIGN KEY (IN_PARCHEGGIO) REFERENCES PARCHEGGIO(NOME)"
                + ");";
        stmt.execute(sqlCar);

        String sqlRental = "CREATE TABLE IF NOT EXISTS NOLEGGI ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "CLIENTE TEXT, "
                + "TARGA_AUTO TEXT, "
                + "INIZIO BIGINT, "
                + "DURATA_ORE INTEGER, "
                + "ATTIVO BOOLEAN, "
                + "FOREIGN KEY (CLIENTE) REFERENCES USER(NOME), "
                + "FOREIGN KEY (TARGA_AUTO) REFERENCES AUTO(TARGA)"
                + ");";
        stmt.execute(sqlRental);

        String sqlPayment = "CREATE TABLE IF NOT EXISTS PAGAMENTI ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "METODO TEXT NOT NULL, "
                + "CODICE TEXT NOT NULL, "
                + "IMPORTO DOUBLE NOT NULL, "
                + "EFFETTUATO_DA TEXT, "
                + "FOREIGN KEY (EFFETTUATO_DA) REFERENCES USER(NOME)"
                + ");";
        stmt.execute(sqlPayment);

        System.out.println("Inizializzazione completata");
    }

    // --- METODI OPERATIVI ---

    public static void InsertCar(String t, String m, String p) {
        String sqlCar = "INSERT INTO AUTO(TARGA, MODELLO, IN_PARCHEGGIO) VALUES (?,?,?)";
        String sqlPark = "UPDATE PARCHEGGIO SET AUTO_PRESENTE = ? WHERE NOME = ?";

        try (Connection connect = DriverManager.getConnection(protocol)) {
            connect.setAutoCommit(false); // Transazione

            try (PreparedStatement stmt1 = connect.prepareStatement(sqlCar);
                    PreparedStatement stmt2 = connect.prepareStatement(sqlPark)) {

                stmt1.setString(1, t);
                stmt1.setString(2, m);
                stmt1.setString(3, p);
                stmt1.executeUpdate();

                stmt2.setString(1, t);
                stmt2.setString(2, p);
                stmt2.executeUpdate();

                connect.commit();
            } catch (SQLException e) {
                connect.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore InsertCar: " + e.getMessage());
        }
    }

    public static void MoveCar(String t, String arr) {
        String sqlClearOld = "UPDATE PARCHEGGIO SET AUTO_PRESENTE = NULL WHERE NOME = (SELECT IN_PARCHEGGIO FROM AUTO WHERE TARGA = ?)";
        String sqlMoveCar = "UPDATE AUTO SET IN_PARCHEGGIO=? WHERE TARGA=?";
        String sqlSetNew = "UPDATE PARCHEGGIO SET AUTO_PRESENTE = ? WHERE NOME = ?";

        try (Connection connect = DriverManager.getConnection(protocol)) {
            connect.setAutoCommit(false);

            try (PreparedStatement stmt1 = connect.prepareStatement(sqlClearOld);
                    PreparedStatement stmt2 = connect.prepareStatement(sqlMoveCar);
                    PreparedStatement stmt3 = connect.prepareStatement(sqlSetNew)) {

                stmt1.setString(1, t);
                stmt1.executeUpdate();

                stmt2.setString(1, arr);
                stmt2.setString(2, t);
                stmt2.executeUpdate();

                stmt3.setString(1, t);
                stmt3.setString(2, arr);
                stmt3.executeUpdate();

                connect.commit();
            } catch (SQLException e) {
                connect.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore MoveCar: " + e.getMessage());
        }
    }

    public static void InsertRental(String u, String t, int d) {
        String sqlClearPark = "UPDATE PARCHEGGIO SET AUTO_PRESENTE = NULL WHERE NOME = (SELECT IN_PARCHEGGIO FROM AUTO WHERE TARGA = ?)";
        String sqlUpdateCar = "UPDATE AUTO SET IN_PARCHEGGIO = NULL WHERE TARGA = ?";
        String sqlRental = "INSERT INTO NOLEGGI(CLIENTE, TARGA_AUTO, INIZIO, DURATA_ORE, ATTIVO) VALUES(?,?,?,?,?)";

        try (Connection connect = DriverManager.getConnection(protocol)) {
            connect.setAutoCommit(false);

            try (PreparedStatement stmt1 = connect.prepareStatement(sqlClearPark);
                 PreparedStatement stmt2 = connect.prepareStatement(sqlUpdateCar);
                 PreparedStatement stmt3 = connect.prepareStatement(sqlRental)) {

                stmt1.setString(1, t);
                stmt1.executeUpdate();

                stmt2.setString(1, t);
                stmt2.executeUpdate();

                stmt3.setString(1, u);
                stmt3.setString(2, t);
                stmt3.setLong(3, System.currentTimeMillis());
                stmt3.setInt(4, d);
                stmt3.setBoolean(5, true);
                stmt3.executeUpdate();

                connect.commit();
            } catch (SQLException e) {
                connect.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore InsertRental: " + e.getMessage());
        }
    }

    public static void ReturnCar(String t, String p) {
        String sqlRental = "UPDATE NOLEGGI SET ATTIVO=0 WHERE TARGA_AUTO=? AND ATTIVO=1";
        String sqlReturnCar = "UPDATE AUTO SET IN_PARCHEGGIO=? WHERE TARGA=?";
        String sqlUpdatePark = "UPDATE PARCHEGGIO SET AUTO_PRESENTE=? WHERE NOME=?";

        try (Connection connect = DriverManager.getConnection(protocol)) {
            connect.setAutoCommit(false);
            try (PreparedStatement stmt1 = connect.prepareStatement(sqlRental);
                    PreparedStatement stmt2 = connect.prepareStatement(sqlReturnCar);
                    PreparedStatement stmt3 = connect.prepareStatement(sqlUpdatePark)) {

                stmt1.setString(1, t);
                stmt1.executeUpdate();

                stmt2.setString(1, p);
                stmt2.setString(2, t);
                stmt2.executeUpdate();

                stmt3.setString(1, t);
                stmt3.setString(2, p);
                stmt3.executeUpdate();

                connect.commit();
            } catch (SQLException e) {
                connect.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Errore ReturnCar: " + e.getMessage());
        }
    }

    // --- METODI STANDARD ---

    public static void InsertUser(String n, Boolean a) {
        String sql = "INSERT INTO USER(NOME, ABBONATO) VALUES(?,?)";
        try (Connection connect = DriverManager.getConnection(protocol)) {
            PreparedStatement stmt = connect.prepareStatement(sql);
            stmt.setString(1, n);
            stmt.setBoolean(2, a);
            stmt.execute();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void InsertParking(String n) {
        String sql = "INSERT INTO PARCHEGGIO(NOME) VALUES(?)";
        try (Connection connect = DriverManager.getConnection(protocol)) {
            PreparedStatement stmt = connect.prepareStatement(sql);
            stmt.setString(1, n);
            stmt.execute();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void InsertPayment(String m, String c, Double i, String u) {
        String sql = "INSERT INTO PAGAMENTI(METODO, CODICE, IMPORTO, EFFETTUATO_DA) VALUES(?,?,?,?)";
        try (Connection connect = DriverManager.getConnection(protocol)) {
            PreparedStatement stmt = connect.prepareStatement(sql);
            stmt.setString(1, m);
            stmt.setString(2, c);
            stmt.setDouble(3, i);
            stmt.setString(4, u);
            stmt.execute();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // UTILITY PER VEDERE IL DB
    public static void ShowDatabase() {
        System.out.println("\n--- CONTENUTO DATABASE ---");
        String[] tables = { "PARCHEGGIO", "AUTO", "USER", "NOLEGGI", "PAGAMENTI" };

        try (Connection connect = DriverManager.getConnection(protocol)) {
            Statement stmt = connect.createStatement();

            for (String table : tables) {
                System.out.println("\nTABELLA: " + table);
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
                    int colCount = rs.getMetaData().getColumnCount();
                    if (!rs.isBeforeFirst())
                        System.out.println("  [Tabella vuota]");

                    while (rs.next()) {
                        System.out.print("  RIGA: ");
                        for (int i = 1; i <= colCount; i++) {
                            System.out.print(rs.getMetaData().getColumnName(i) + "=" + rs.getString(i) + " | ");
                        }
                        System.out.println();
                    }
                } catch (SQLException e) {
                    System.out.println("  Tabella non trovata.");
                }
            }
            System.out.println("\n--------------------------\n");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void ClearDatabase() {
        System.out.println("--- PULIZIA DB ---");
        String[] tables = { "PAGAMENTI", "NOLEGGI", "AUTO", "USER", "PARCHEGGIO" };
        try (Connection connect = DriverManager.getConnection(protocol)) {
            Statement stmt = connect.createStatement();
            stmt.execute("PRAGMA foreign_keys = OFF;");
            for (String table : tables) {
                stmt.executeUpdate("DELETE FROM " + table);
                stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='" + table + "'");
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}