package pl.coderslab.workshopDAO;

import org.mindrot.jbcrypt.BCrypt;

public class User {
    private int id;
    private String userName;
    private String email;
    private String password;

    public User(int id, String userName, String email, String password) {
        this.id = id;
        this.userName = userName;
        this.email = email;
//        this.password = password;
        setPassword(password);
    }

    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
//        this.password = password;
        setPassword(password);
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    void setId(int id) {
        this.id = id;
    }

    public void setUserName(User user) {
        UserDao.update(user);
    }

    public boolean setEmail(User user) {
        return UserDao.isEmailUnique(getEmail())? UserDao.update(user) : null; // zwróci nam true lub false
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
//        UserDao.update(user);
    }
}
