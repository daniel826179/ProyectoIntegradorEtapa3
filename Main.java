package proyectointegradore3;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazGestorTareas interfaz = new InterfazGestorTareas();
            interfaz.setVisible(true);
        });
    }
}