package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User extends Model {

    @Id
    public String username;
    public String password;
    public String name;
    public String type;

    
    public User(String username, String password, String type, String name) {
      this.username = username;
      this.password = password;
      this.type     = type;
      this.name     = name;
    }

    public static Finder<String,User> find = new Finder<>(String.class, User.class);

    public static void create(User user) { user.save(); }

    /* This will authenticate our user */
     public static boolean authenticate(String username, String password, String type) {

        return (find.where().eq("username", username).eq("password", password).eq("type", type).findUnique() != null);
    }
}