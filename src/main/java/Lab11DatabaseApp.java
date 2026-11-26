import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Lab11DatabaseApp {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            initDatabase(conn);

            System.out.println("=== 1. Список усіх співробітників ===");
            printAllEmployees(conn);

            System.out.println("\n=== 2. Список усіх завдань ===");
            printAllTasks(conn);

            System.out.println("\n=== 3. Співробітники відділу №1 (IT-відділ) ===");
            printEmployeesByDepartment(conn, 1);

            System.out.println("\n=== 4. Додавання нового завдання для співробітника №2 ===");
            addTaskForEmployee(conn, 2, "Налаштувати резервне копіювання БД");

            System.out.println("\n=== 5. Список завдань співробітника №2 ===");
            printTasksByEmployee(conn, 2);

            System.out.println("\n=== 6. Видалення співробітника №3 ===");
            deleteEmployee(conn, 3);

            System.out.println("\n=== Оновлений список усіх співробітників ===");
            printAllEmployees(conn);

        } catch (Exception e) {
            System.out.println("Помилка роботи з БД: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS tasks;");
            stmt.execute("DROP TABLE IF EXISTS employees;");
            stmt.execute("DROP TABLE IF EXISTS departments;");

            stmt.execute("""
                CREATE TABLE departments (
                    dept_id INTEGER PRIMARY KEY,
                    name CHAR(30) NOT NULL,
                    phone CHAR(15)
                );
            """);

            stmt.execute("""
                CREATE TABLE employees (
                    emp_id INTEGER PRIMARY KEY,
                    last_name VARCHAR(20) NOT NULL,
                    first_name VARCHAR(10) NOT NULL,
                    position VARCHAR(20),
                    dept_id INTEGER,
                    FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE tasks (
                    task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description VARCHAR(50) NOT NULL,
                    emp_id INTEGER,
                    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
                );
            """);

            stmt.execute("""
                INSERT INTO departments (dept_id, name, phone) VALUES
                (1, 'IT-відділ', '044-111-22-33'),
                (2, 'Відділ аналітики', '044-444-55-66');
            """);

            stmt.execute("""
                INSERT INTO employees (emp_id, last_name, first_name, position, dept_id) VALUES
                (1, 'Коваленко', 'Олександр', 'Java Developer', 1),
                (2, 'Мельник', 'Ірина', 'QA Engineer', 1),
                (3, 'Шевченко', 'Дмитро', 'Data Analyst', 2),
                (4, 'Бондар', 'Анна', 'Business Analyst', 2);
            """);

            stmt.execute("""
                INSERT INTO tasks (description, emp_id) VALUES
                ('Розробити REST API', 1),
                ('Провести тестування модуля авторизації', 2),
                ('Підготувати місячний звіт', 3),
                ('Оновити документацію проєкту', 4);
            """);
        }
    }

    private static void printAllEmployees(Connection conn) throws SQLException {
        String sql = "SELECT emp_id, last_name, first_name, position, dept_id FROM employees";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("ID: %d | %s %s | Посада: %s | Відділ №%d%n",
                        rs.getInt("emp_id"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getString("position"),
                        rs.getInt("dept_id"));
            }
        }
    }

    private static void printAllTasks(Connection conn) throws SQLException {
        String sql = "SELECT task_id, description, emp_id FROM tasks";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("Завдання #%d | %s | Співробітник ID: %d%n",
                        rs.getInt("task_id"),
                        rs.getString("description"),
                        rs.getInt("emp_id"));
            }
        }
    }

    private static void printEmployeesByDepartment(Connection conn, int deptId) throws SQLException {
        String sql = "SELECT emp_id, last_name, first_name, position FROM employees WHERE dept_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, deptId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID: %d | %s %s | Посада: %s%n",
                            rs.getInt("emp_id"),
                            rs.getString("last_name"),
                            rs.getString("first_name"),
                            rs.getString("position"));
                }
            }
        }
    }

    private static void addTaskForEmployee(Connection conn, int empId, String description) throws SQLException {
        String sql = "INSERT INTO tasks (description, emp_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, description);
            pstmt.setInt(2, empId);
            pstmt.executeUpdate();
            System.out.println("Додано завдання: \"" + description + "\" для співробітника ID=" + empId);
        }
    }

    private static void printTasksByEmployee(Connection conn, int empId) throws SQLException {
        String sql = "SELECT task_id, description FROM tasks WHERE emp_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, empId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Завдання #%d | %s%n",
                            rs.getInt("task_id"),
                            rs.getString("description"));
                }
            }
        }
    }

    private static void deleteEmployee(Connection conn, int empId) throws SQLException {
        String sql = "DELETE FROM employees WHERE emp_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, empId);
            int rows = pstmt.executeUpdate();
            System.out.println("Видалено співробітника з ID=" + empId + " (записів видалено: " + rows + ")");
        }
    }
}