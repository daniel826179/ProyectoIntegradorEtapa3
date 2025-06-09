package proyectointegradore3;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManejadorBaseDeDatos {
    private static final String URL = "jdbc:mysql://localhost:3306/gestor_tareas";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "Whitelamp1";

    public Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC no encontrado", e);
        }
    }

    public void crearTablaSiNoExiste() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS tareas (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "titulo VARCHAR(100) NOT NULL, " +
                "descripcion TEXT, " +
                "fecha_creacion DATE NOT NULL, " +
                "fecha_limite DATE NOT NULL, " +
                "completada BOOLEAN DEFAULT FALSE)";
        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public int agregarTarea(Tarea tarea) throws SQLException {
        String sql = "INSERT INTO tareas (titulo, descripcion, fecha_creacion, fecha_limite, completada) VALUES (?,?,?,?,?)";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tarea.getTitulo());
            pstmt.setString(2, tarea.getDescripcion());
            pstmt.setDate(3, Date.valueOf(tarea.getFechaCreacion()));
            pstmt.setDate(4, Date.valueOf(tarea.getFechaLimite()));
            pstmt.setBoolean(5, tarea.isCompletada());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<Tarea> obtenerTodasLasTareas() throws SQLException {
        List<Tarea> tareas = new ArrayList<>();
        String sql = "SELECT * FROM tareas";
        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tareas.add(new Tarea(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getDate("fecha_creacion").toLocalDate(),
                        rs.getDate("fecha_limite").toLocalDate(),
                        rs.getBoolean("completada")
                ));
            }
        }
        return tareas;
    }

    public boolean eliminarTarea(int id) throws SQLException {
        String sql = "DELETE FROM tareas WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean actualizarTarea(Tarea tarea) throws SQLException {
        String sql = "UPDATE tareas SET titulo = ?, descripcion = ?, fecha_limite = ?, completada = ? WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tarea.getTitulo());
            pstmt.setString(2, tarea.getDescripcion());
            pstmt.setDate(3, Date.valueOf(tarea.getFechaLimite()));
            pstmt.setBoolean(4, tarea.isCompletada());
            pstmt.setInt(5, tarea.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Tarea> obtenerTareasProximasAVencer(int dias) throws SQLException {
        List<Tarea> tareas = new ArrayList<>();
        String sql = "SELECT * FROM tareas WHERE fecha_limite BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) AND completada = FALSE";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dias);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tareas.add(new Tarea(
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getDate("fecha_creacion").toLocalDate(),
                            rs.getDate("fecha_limite").toLocalDate(),
                            rs.getBoolean("completada")
                    ));
                }
            }
        }
        return tareas;
    }
}