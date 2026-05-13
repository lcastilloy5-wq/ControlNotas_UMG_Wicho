import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    // Requisito: La base de datos debe llamarse BDNotas 
    private static final String URL = "jdbc:mysql://localhost:3306/BDNotas";
    private static final String USER = "root"; 
    private static final String PASSWORD = "2408"; // <-- COLOCA TU CLAVE DE WORKBENCH

    public static Connection conectar() {
        Connection cn = null;
        try {
            cn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return cn;
    }
}