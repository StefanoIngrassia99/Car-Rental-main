import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) throws InterruptedException{
        // Disabilita l'accelerazione hardware Direct3D/DirectDraw
        // Questo risolve i "buchi neri" o le "scie" grafiche tipiche di Java su Windows
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CarRentalGUI();
            }
        });
    }
}
