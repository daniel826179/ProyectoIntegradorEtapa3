package proyectointegradore3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TrabajadorTarea implements Runnable {
    private final ManejadorBaseDeDatos manejadorBD;
    private final Tarea tarea;
    private final Integer idEliminar;
    private final DefaultTableModel modeloTabla;
    private final String operacion;
    private final InterfazGestorTareas interfaz; // Referencia a la interfaz

    public TrabajadorTarea(ManejadorBaseDeDatos manejadorBD, Tarea tarea, DefaultTableModel modeloTabla, String operacion, InterfazGestorTareas interfaz) {
        this.manejadorBD = manejadorBD;
        this.tarea = tarea;
        this.idEliminar = null;
        this.modeloTabla = modeloTabla;
        this.operacion = operacion;
        this.interfaz = interfaz;
    }

    public TrabajadorTarea(ManejadorBaseDeDatos manejadorBD, int idEliminar, DefaultTableModel modeloTabla, String operacion, InterfazGestorTareas interfaz) {
        this.manejadorBD = manejadorBD;
        this.tarea = null;
        this.idEliminar = idEliminar;
        this.modeloTabla = modeloTabla;
        this.operacion = operacion;
        this.interfaz = interfaz;
    }

    @Override
    public void run() {
        try {
            switch (operacion) {
                case "AGREGAR":
                    int id = manejadorBD.agregarTarea(tarea);
                    tarea.setId(id);
                    SwingUtilities.invokeLater(() -> {
                        synchronized (modeloTabla) {
                            interfaz.setActualizandoTabla(true);
                            modeloTabla.addRow(new Object[]{
                                    tarea.getId(),
                                    tarea.getTitulo(),
                                    tarea.getDescripcion(),
                                    tarea.getFechaCreacion(),
                                    tarea.getFechaLimite(),
                                    calcularEstado(tarea.getFechaLimite(), tarea.isCompletada()),
                                    tarea.isCompletada()
                            });
                            interfaz.setActualizandoTabla(false);
                        }
                        UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                        JOptionPane.showMessageDialog(null,
                                "Tarea agregada con éxito",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                    break;
                case "ACTUALIZAR":
                    boolean actualizado = manejadorBD.actualizarTarea(tarea);
                    if (actualizado) {
                        SwingUtilities.invokeLater(() -> {
                            synchronized (modeloTabla) {
                                interfaz.setActualizandoTabla(true);
                                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                                    if ((int) modeloTabla.getValueAt(i, 0) == tarea.getId()) {
                                        modeloTabla.setValueAt(tarea.getTitulo(), i, 1);
                                        modeloTabla.setValueAt(tarea.getDescripcion(), i, 2);
                                        modeloTabla.setValueAt(tarea.getFechaLimite(), i, 4);
                                        modeloTabla.setValueAt(calcularEstado(tarea.getFechaLimite(), tarea.isCompletada()), i, 5);
                                        modeloTabla.setValueAt(tarea.isCompletada(), i, 6);
                                        break;
                                    }
                                }
                                interfaz.setActualizandoTabla(false);
                            }
                            UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                            JOptionPane.showMessageDialog(null,
                                    "Tarea actualizada con éxito",
                                    "Éxito",
                                    JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                    break;
                case "ELIMINAR":
                    boolean eliminado = idEliminar != null ? manejadorBD.eliminarTarea(idEliminar) : manejadorBD.eliminarTarea(tarea.getId());
                    if (eliminado) {
                        SwingUtilities.invokeLater(() -> {
                            synchronized (modeloTabla) {
                                interfaz.setActualizandoTabla(true);
                                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                                    if ((int) modeloTabla.getValueAt(i, 0) == (idEliminar != null ? idEliminar : tarea.getId())) {
                                        modeloTabla.removeRow(i);
                                        break;
                                    }
                                }
                                interfaz.setActualizandoTabla(false);
                            }
                            UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                            JOptionPane.showMessageDialog(null,
                                    "Tarea eliminada con éxito",
                                    "Éxito",
                                    JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                    break;
            }
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                JOptionPane.showMessageDialog(null,
                        "Error en la base de datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private String calcularEstado(LocalDate fechaLimite, boolean completada) {
        if (completada) return "Completada";
        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
        if (diasRestantes > 1) return "Pendiente";
        if (diasRestantes == 1) return "Por vencer";
        if (diasRestantes == 0) return "Vence hoy";
        return "Vencida";
    }
}