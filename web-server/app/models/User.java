package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User extends Model {

    @Id
    public String username;
    public String name;
    public String password;
    
    public User(String username, String name, String password) {
      this.username = username;
      this.name = name;
      this.password = password;
    }

    public static Finder<String,User> find = new Finder<>(
        String.class, User.class
    ); 

    public static void create(User user) { user.save(); }

    /* This will authenticate our user */
     public static boolean authenticate(String username, String password) {
        return (find.where().eq("username", username)
            .eq("password", password).findUnique() != null);
    }
}