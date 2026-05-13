import java.util.Scanner;
import java.sql.*;

public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Connection con = Conexion.conectar();
        int opcion = 0;

        if (con == null) {
            System.out.println("Error: No se pudo conectar a la base de datos. Verifica Conexion.java");
            return;
        }

        do {
            System.out.println("\n===========================================");
            System.out.println("     CONTROL DE NOTAS - UMG SISTEMAS      ");
            System.out.println("===========================================");
            System.out.println("1. Ingreso de Alumnos");
            System.out.println("2. Ingreso de Notas");
            System.out.println("3. Eliminar Alumnos");
            System.out.println("4. Actualizar datos y notas");
            System.out.println("5. Buscar alumnos por Carnet o Nombre");
            System.out.println("6. Obtener Promedios por Sección");
            System.out.println("7. Listar Alumnos");
            System.out.println("8. Salir");
            System.out.println("===========================================");
            System.out.print("Elija una opción: ");
            
            while (!sc.hasNextInt()) {
                System.out.println("Error: Ingrese un número del 1 al 8.");
                sc.next();
            }
            opcion = sc.nextInt();
            sc.nextLine(); 

            switch(opcion) {
                case 1: ingresarAlumno(con, sc); break;
                case 2: ingresarNota(con, sc); break;
                case 3: eliminarAlumno(con, sc); break;
                case 4: actualizarAlumno(con, sc); break;
                case 5: buscarAlumno(con, sc); break;
                case 6: obtenerPromedios(con); break;
                case 7: listarAlumnos(con, sc); break;
                case 8: System.out.println("Cerrando sistema..."); break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 8);
    }

    // OPCIÓN 1: Ingreso de Alumnos [cite: 14, 23]
    public static void ingresarAlumno(Connection con, Scanner sc) {
        System.out.print("Carnet: "); String carnet = sc.nextLine();
        System.out.print("Nombres: "); String nom = sc.nextLine();
        System.out.print("Apellidos: "); String ape = sc.nextLine();
        System.out.print("Sección (A/B): "); String sec = sc.nextLine().toUpperCase();

        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO alumnos VALUES (?, ?, ?, ?)");
            ps.setString(1, carnet); ps.setString(2, nom);
            ps.setString(3, ape); ps.setString(4, sec);
            ps.executeUpdate();
            System.out.println("Alumno registrado con éxito.");
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // OPCIÓN 2: Ingreso de Notas [cite: 15, 25]
    public static void ingresarNota(Connection con, Scanner sc) {
        System.out.print("Carnet del alumno: "); String carnet = sc.nextLine();
        try {
            PreparedStatement psV = con.prepareStatement("SELECT nombres FROM alumnos WHERE carnet=?");
            psV.setString(1, carnet);
            ResultSet rs = psV.executeQuery();
            if (rs.next()) {
                System.out.print("Ingrese nota para " + rs.getString("nombres") + ": ");
                double nota = sc.nextDouble();
                PreparedStatement psN = con.prepareStatement("INSERT INTO notas (carnet_alumno, nota) VALUES (?,?) ON DUPLICATE KEY UPDATE nota=?");
                psN.setString(1, carnet); psN.setDouble(2, nota); psN.setDouble(3, nota);
                psN.executeUpdate();
                System.out.println("Nota guardada correctamente.");
            } else { System.out.println("Error: El alumno no existe."); }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // OPCIÓN 3: Eliminar Alumno con confirmación [cite: 16, 26]
    public static void eliminarAlumno(Connection con, Scanner sc) {
        System.out.print("Carnet del alumno a eliminar: "); String carnet = sc.nextLine();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT nombres FROM alumnos WHERE carnet=?");
            ps.setString(1, carnet);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.print("¿Seguro que desea eliminar a " + rs.getString("nombres") + "? (S/N): ");
                if (sc.nextLine().equalsIgnoreCase("S")) {
                    PreparedStatement psD = con.prepareStatement("DELETE FROM alumnos WHERE carnet=?");
                    psD.setString(1, carnet);
                    psD.executeUpdate();
                    System.out.println("Registro eliminado.");
                }
            } else { System.out.println("Alumno no encontrado."); }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // OPCIÓN 4: Actualizar Datos [cite: 17, 27]
    public static void actualizarAlumno(Connection con, Scanner sc) {
        System.out.print("Carnet del alumno a actualizar: "); String carnet = sc.nextLine();
        try {
            System.out.print("Nuevos Nombres: "); String nom = sc.nextLine();
            System.out.print("Nuevos Apellidos: "); String ape = sc.nextLine();
            PreparedStatement ps = con.prepareStatement("UPDATE alumnos SET nombres=?, apellidos=? WHERE carnet=?");
            ps.setString(1, nom); ps.setString(2, ape); ps.setString(3, carnet);
            ps.executeUpdate();
            System.out.println("Datos actualizados.");
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // OPCIÓN 5: Buscar Alumno [cite: 18, 28]
    public static void buscarAlumno(Connection con, Scanner sc) {
        System.out.print("Ingrese Carnet o Nombre a buscar: "); String dato = sc.nextLine();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT a.*, n.nota FROM alumnos a LEFT JOIN notas n ON a.carnet = n.carnet_alumno WHERE a.carnet=? OR a.nombres LIKE ?");
            ps.setString(1, dato); ps.setString(2, "%" + dato + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("Carnet: " + rs.getString("carnet") + " | Alumno: " + rs.getString("nombres") + " " + rs.getString("apellidos") + " | Sección: " + rs.getString("seccion") + " | Nota: " + rs.getDouble("nota"));
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // OPCIÓN 6: Promedios por Sección [cite: 19, 29]
    public static void obtenerPromedios(Connection con) {
        try {
            ResultSet rs = con.createStatement().executeQuery("SELECT seccion, AVG(nota) as promedio FROM alumnos a JOIN notas n ON a.carnet = n.carnet_alumno GROUP BY seccion");
            System.out.println("\n--- PROMEDIOS CALCULADOS ---");
            while (rs.next()) {
                System.out.println("Sección " + rs.getString("seccion") + ": " + rs.getDouble("promedio"));
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    // OPCIÓN 7: Listar Alumnos por Sección [cite: 20, 30]
    public static void listarAlumnos(Connection con, Scanner sc) {
        System.out.print("Sección a listar (A/B): "); String sec = sc.nextLine().toUpperCase();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT a.carnet, a.nombres, a.apellidos, n.nota FROM alumnos a LEFT JOIN notas n ON a.carnet = n.carnet_alumno WHERE a.seccion=?");
            ps.setString(1, sec);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n--- LISTADO SECCIÓN " + sec + " ---");
            while (rs.next()) {
                System.out.println(rs.getString("carnet") + " - " + rs.getString("nombres") + " " + rs.getString("apellidos") + " | Nota: " + rs.getDouble("nota"));
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }
}