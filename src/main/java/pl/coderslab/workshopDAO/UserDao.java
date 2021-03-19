package pl.coderslab.workshopDAO;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.awt.PageAttributes.MediaType.STATEMENT;

public class UserDao {
    //    crut -> create read update delate
    private static final String EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String CREATE_QUERY = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
    private static final String READ_QUERY = "SELECT * FROM users WHERE email = ?"; //czytamy po unikatowej kolumnie
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, username = ?, password = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String READ_ALL = "SELECT * FROM users";

    public static boolean isEmailUnique(String email) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement prepSt = conn.prepareStatement(EMAIL)) {
            prepSt.setString(1, email);
            ResultSet rs = prepSt.executeQuery();
            if (rs.next()) {
                return false; // nie jest unikalny więc false
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // dajemy false, bo jak wyskoczy błąd to metoda się wykona i będzie błędny mail
        }
        return true;
    }

    public static boolean create(User user) { // robimy boolean bo nie tworzymy reakcji z użytkownikiem i jak będzie błąd to możemy wrócić
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement prepSt = conn.prepareStatement(CREATE_QUERY, PreparedStatement.RETURN_GENERATED_KEYS)) { // z obiektu prepareStatement mogę wyciągnąć nadane klucze; chodzi o to żeby mieć cały czas aktualną bazę i id tego użytkownika
            if (!isEmailUnique(user.getEmail())) { // na początku robimy walidację i jak coś się nie zgadza to kończymy metodę
                return false;
            }
            prepSt.setString(1, user.getEmail());
            prepSt.setString(2, user.getUserName());
            String password = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()); // za#(!tag) hasło
            prepSt.setString(3, password);
            if (prepSt.executeUpdate() == 1) { // zwraca wartość wysokości wierszy (int) - który wiersz został zmieniony
                return true; // zmieniliśmy jeden wiersz
            }
            ResultSet rs = prepSt.getGeneratedKeys(); // będą się id wstawiać; będzie reprezentował klucze wygenerowane przy tworzeniu nowych rekordów (zaciąga Primary Key); można przyporządkować klucz w obiekcie do nowego id
            if (rs.next()) {
                user.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static User read(String email) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(READ_QUERY)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();  // resultset przechowuje wynik zapytania, to takie pudełko i każdy wiersz to jeden wynik zaqpytania wyświetlający się na tablicy
            if (!rs.next()) { // przejdź do następnego wiersza; jeśli wejdzie na pierwszy wiersz zwraca true i pozwala na korzystanie z wartości w tym wierszu, jeśli nie to zwraca false
                System.out.println("Podany email nie isntieje"); // to jest pomocnicze tylko dla programisty
                return null; // w mainie po użyciu tej metody można sprawdzić - jeśli jest null to jest błąd
            } else {// teraz jesteśmy w pierwszej linii rs bo już go wywołaliśmy w linii 56
                System.out.println(rs.getInt(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4));
                return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void readAll () {
        try (Connection conn = DBUtil.getConnection();
        PreparedStatement ps = conn.prepareStatement(READ_ALL)) {
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4));
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean update(User user) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, user.getEmail()); // email wyciągnięty z usera
            ps.setString(2, user.getUserName()); // username z usera
            String password = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            ps.setString(3, password); // username z usera
            ps.setInt(4, user.getId());
            if (ps.executeUpdate() == 1) {
                System.out.println("Zmieniono dane");
                return true;
            } else {
                System.out.println("nie udało się zmienić danych");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete (User user) {
        try (Connection conn = DBUtil.getConnection();
        PreparedStatement ps = conn.prepareStatement(DELETE_QUERY)) {
            ps.setInt(1, user.getId());
            if (ps.executeUpdate() == 1) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
