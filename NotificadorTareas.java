package proyectointegradore3;

import javax.swing.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NotificadorTareas implements Runnable {
    private final ManejadorBaseDeDatos manejadorBD;
    private volatile boolean activo;

    public NotificadorTareas(ManejadorBaseDeDatos manejadorBD) {
        this.manejadorBD = manejadorBD;
        this.activo = true;
    }

    public void detener() {
        this.activo = false;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                List<Tarea> tareasProximas = manejadorBD.obtenerTareasProximasAVencer(1);
                for (Tarea tarea : tareasProximas) {
                    mostrarNotificacion(tarea);
                }
                Thread.sleep(30000); // Revisar cada 30 segundos
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                    JOptionPane.showMessageDialog(null,
                            "Error al verificar tareas: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void mostrarNotificacion(Tarea tarea) {
        SwingUtilities.invokeLater(() -> {
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), tarea.getFechaLimite());
            String mensaje = "La tarea '" + tarea.getTitulo() + "' está próxima a vencer (" + diasRestantes + " días restantes).";
            UIManager.put("OptionPane.okButtonText", "ACEPTAR");
            JOptionPane.showMessageDialog(null,
                    mensaje,
                    "Notificación de Tarea",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }
}

