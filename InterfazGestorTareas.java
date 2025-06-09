package proyectointegradore3;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class InterfazGestorTareas extends JFrame {
    private final ManejadorBaseDeDatos manejadorBD;
    private DefaultTableModel modeloTabla;
    private JTable tablaTareas;
    private NotificadorTareas notificador;
    private boolean actualizandoTabla = false; // Bandera para evitar bucles en el TableModelListener
    private static final Color COLOR_FONDO = new Color(240, 248, 255);
    private static final Color COLOR_BOTONES = new Color(100, 149, 237);
    private static final Color COLOR_TITULOS = new Color(70, 130, 180);
    private static final Color COLOR_TABLA = Color.WHITE;
    private static final Color COLOR_SELECCION = new Color(173, 216, 230);
    private static final Color COLOR_COMPLETADA = new Color(200, 255, 200);
    private static final Color COLOR_VENCIDA = new Color(255, 200, 200);

    public InterfazGestorTareas() {
        this.manejadorBD = new ManejadorBaseDeDatos();
        configurarVentanaPrincipal();
        initComponents();
        cargarTareasIniciales();
        iniciarNotificador();
    }

    private void configurarVentanaPrincipal() {
        setTitle("Gestor de Tareas con MySQL");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion();
            }
        });
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
    }

    private void iniciarNotificador() {
        notificador = new NotificadorTareas(manejadorBD);
        Thread hiloNotificador = new Thread(notificador, "Hilo-Notificador");
        hiloNotificador.setDaemon(true);
        hiloNotificador.start();
    }

    private void cerrarAplicacion() {
        UIManager.put("OptionPane.yesButtonText", "SÍ");
        UIManager.put("OptionPane.noButtonText", "NO");
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea salir?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirmacion == JOptionPane.YES_OPTION) {
            notificador.detener();
            dispose();
            System.exit(0);
        }
    }

    private void cargarTareasIniciales() {
        try {
            List<Tarea> tareas = manejadorBD.obtenerTodasLasTareas();
            for (Tarea t : tareas) {
                modeloTabla.addRow(new Object[]{
                        t.getId(),
                        t.getTitulo(),
                        t.getDescripcion(),
                        t.getFechaCreacion(),
                        t.getFechaLimite(),
                        calcularEstado(t.getFechaLimite(), t.isCompletada()),
                        t.isCompletada()
                });
            }
        } catch (SQLException e) {
            UIManager.put("OptionPane.okButtonText", "ACEPTAR");
            JOptionPane.showMessageDialog(this,
                    "Error al cargar tareas: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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

    private void initComponents() {
        JPanel panelEntrada = crearPanelEntrada();
        add(panelEntrada, BorderLayout.NORTH);

        JScrollPane panelTabla = crearPanelTabla();
        add(panelTabla, BorderLayout.CENTER);

        JPanel panelBotones = crearPanelBotones();
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel crearPanelEntrada() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(COLOR_FONDO);

        JLabel lblTitulo = crearLabel("Título:");
        JTextField campoTitulo = new JTextField();
        JLabel lblDescripcion = crearLabel("Descripción:");
        JTextArea areaDescripcion = new JTextArea(3, 20);
        JLabel lblFecha = crearLabel("Fecha Límite (AAAA-MM-DD):");
        JTextField campoFecha = new JTextField();
        JButton btnAgregar = crearBoton("Agregar Tarea", COLOR_BOTONES);

        areaDescripcion.setLineWrap(true);
        areaDescripcion.setWrapStyleWord(true);

        panel.add(lblTitulo);
        panel.add(campoTitulo);
        panel.add(lblDescripcion);
        panel.add(new JScrollPane(areaDescripcion));
        panel.add(lblFecha);
        panel.add(campoFecha);
        panel.add(new JLabel());
        panel.add(btnAgregar);

        btnAgregar.addActionListener(e -> {
            try {
                Tarea nuevaTarea = new Tarea(
                        campoTitulo.getText(),
                        areaDescripcion.getText(),
                        LocalDate.parse(campoFecha.getText())
                );
                new Thread(new TrabajadorTarea(manejadorBD, nuevaTarea, modeloTabla, "AGREGAR", this)).start();
                campoTitulo.setText("");
                areaDescripcion.setText("");
                campoFecha.setText("");
            } catch (Exception ex) {
                UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                JOptionPane.showMessageDialog(this,
                        "Formato de fecha inválido. Use AAAA-MM-DD",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private JScrollPane crearPanelTabla() {
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Solo la columna "Completada" es editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 6 ? Boolean.class : super.getColumnClass(columnIndex);
            }
        };
        String[] columnas = {"ID", "Título", "Descripción", "Fecha Creación", "Fecha Límite", "Estado", "Completada"};
        modeloTabla.setColumnIdentifiers(columnas);
        tablaTareas = new JTable(modeloTabla);
        personalizarTabla();

        // Listener para cambios en el checkbox, con bandera para evitar bucles
        modeloTabla.addTableModelListener(e -> {
            if (actualizandoTabla) return; // Evitar procesamiento durante actualizaciones programáticas
            if (e.getColumn() == 6 && e.getFirstRow() >= 0) {
                try {
                    int id = (int) modeloTabla.getValueAt(e.getFirstRow(), 0);
                    boolean completada = (boolean) modeloTabla.getValueAt(e.getFirstRow(), 6);
                    Tarea tarea = new Tarea(
                            id,
                            (String) modeloTabla.getValueAt(e.getFirstRow(), 1),
                            (String) modeloTabla.getValueAt(e.getFirstRow(), 2),
                            LocalDate.parse(modeloTabla.getValueAt(e.getFirstRow(), 3).toString()),
                            LocalDate.parse(modeloTabla.getValueAt(e.getFirstRow(), 4).toString()),
                            completada
                    );
                    new Thread(new TrabajadorTarea(manejadorBD, tarea, modeloTabla, "ACTUALIZAR", this)).start();
                } catch (Exception ex) {
                    UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                    JOptionPane.showMessageDialog(this,
                            "Error al actualizar tarea: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return new JScrollPane(tablaTareas);
    }

    private void personalizarTabla() {
        tablaTareas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row < table.getRowCount()) {
                    boolean completada = (boolean) table.getModel().getValueAt(row, 6);
                    if (completada) {
                        c.setBackground(COLOR_COMPLETADA);
                    } else {
                        try {
                            LocalDate fechaLimite = LocalDate.parse(table.getModel().getValueAt(row, 4).toString());
                            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
                            c.setBackground(diasRestantes < 0 ? COLOR_VENCIDA : COLOR_TABLA);
                        } catch (Exception e) {
                            c.setBackground(COLOR_TABLA);
                        }
                    }
                    if (isSelected) {
                        c.setBackground(COLOR_SELECCION);
                    }
                }
                return c;
            }
        });
        tablaTareas.setRowHeight(30);
        tablaTareas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaTareas.getTableHeader().setBackground(COLOR_TITULOS);
        tablaTareas.getTableHeader().setForeground(Color.WHITE);
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(COLOR_FONDO);
        JButton btnEditar = crearBoton("Editar Tarea", new Color(255, 165, 0));
        JButton btnEliminar = crearBoton("Eliminar Tarea", new Color(220, 80, 80));
        JButton btnMarcar = crearBoton("Marcar Completadas", new Color(144, 238, 144));
        btnEditar.addActionListener(e -> editarTarea());
        btnEliminar.addActionListener(e -> eliminarTarea());
        btnMarcar.addActionListener(e -> marcarTareasSeleccionadas());
        panel.add(btnEditar);
        panel.add(btnEliminar);
        panel.add(btnMarcar);
        return panel;
    }

    private void editarTarea() {
        int filaSeleccionada = tablaTareas.getSelectedRow();
        if (filaSeleccionada >= 0) {
            try {
                Tarea tarea = new Tarea(
                        (int) modeloTabla.getValueAt(filaSeleccionada, 0),
                        (String) modeloTabla.getValueAt(filaSeleccionada, 1),
                        (String) modeloTabla.getValueAt(filaSeleccionada, 2),
                        LocalDate.parse(modeloTabla.getValueAt(filaSeleccionada, 3).toString()),
                        LocalDate.parse(modeloTabla.getValueAt(filaSeleccionada, 4).toString()),
                        (boolean) modeloTabla.getValueAt(filaSeleccionada, 6)
                );
                JTextField campoTitulo = new JTextField(tarea.getTitulo());
                JTextArea areaDescripcion = new JTextArea(tarea.getDescripcion(), 3, 20);
                areaDescripcion.setLineWrap(true);
                areaDescripcion.setWrapStyleWord(true);
                JTextField campoFecha = new JTextField(tarea.getFechaLimite().toString());

                JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
                panel.add(crearLabel("Título:"));
                panel.add(campoTitulo);
                panel.add(crearLabel("Descripción:"));
                panel.add(new JScrollPane(areaDescripcion));
                panel.add(crearLabel("Fecha Límite (AAAA-MM-DD):"));
                panel.add(campoFecha);

                UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                UIManager.put("OptionPane.cancelButtonText", "CANCELAR");
                int result = JOptionPane.showConfirmDialog(this, panel, "Editar Tarea", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    tarea.setTitulo(campoTitulo.getText());
                    tarea.setDescripcion(areaDescripcion.getText());
                    tarea.setFechaLimite(LocalDate.parse(campoFecha.getText()));
                    new Thread(new TrabajadorTarea(manejadorBD, tarea, modeloTabla, "ACTUALIZAR", this)).start();
                }
            } catch (Exception ex) {
                UIManager.put("OptionPane.okButtonText", "ACEPTAR");
                JOptionPane.showMessageDialog(this,
                        "Formato de fecha inválido. Use AAAA-MM-DD",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            UIManager.put("OptionPane.okButtonText", "ACEPTAR");
            JOptionPane.showMessageDialog(this,
                    "Seleccione una tarea para editar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarTarea() {
        int filaSeleccionada = tablaTareas.getSelectedRow();
        if (filaSeleccionada >= 0) {
            UIManager.put("OptionPane.yesButtonText", "SÍ");
            UIManager.put("OptionPane.noButtonText", "NO");
            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro que desea eliminar esta tarea?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirmacion == JOptionPane.YES_OPTION) {
                int id = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                new Thread(new TrabajadorTarea(manejadorBD, id, modeloTabla, "ELIMINAR", this)).start();
            }
        } else {
            UIManager.put("OptionPane.okButtonText", "ACEPTAR");
            JOptionPane.showMessageDialog(this,
                    "Seleccione una tarea para eliminar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void marcarTareasSeleccionadas() {
        int[] filasSeleccionadas = tablaTareas.getSelectedRows();
        if (filasSeleccionadas.length > 0) {
            UIManager.put("OptionPane.yesButtonText", "SÍ");
            UIManager.put("OptionPane.noButtonText", "NO");
            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Marcar " + filasSeleccionadas.length + " tarea(s) como completadas?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirmacion == JOptionPane.YES_OPTION) {
                for (int fila : filasSeleccionadas) {
                    int id = (int) modeloTabla.getValueAt(fila, 0);
                    Tarea tarea = new Tarea(
                            id,
                            (String) modeloTabla.getValueAt(fila, 1),
                            (String) modeloTabla.getValueAt(fila, 2),
                            LocalDate.parse(modeloTabla.getValueAt(fila, 3).toString()),
                            LocalDate.parse(modeloTabla.getValueAt(fila, 4).toString()),
                            true
                    );
                    new Thread(new TrabajadorTarea(manejadorBD, tarea, modeloTabla, "ACTUALIZAR", this)).start();
                }
            }
        } else {
            UIManager.put("OptionPane.okButtonText", "ACEPTAR");
            JOptionPane.showMessageDialog(this,
                    "Seleccione al menos una tarea",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(COLOR_TITULOS);
        return label;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return boton;
    }

    public void setActualizandoTabla(boolean actualizando) {
        this.actualizandoTabla = actualizando;
    }
}