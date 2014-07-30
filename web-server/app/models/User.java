package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;

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

    public static Finder<String,User> find = new Finder<String,User>(
        String.class, User.class
    ); 
    
    /* This will authenticate our user */
     public static User authenticate(String username, String password) {
        return find.where().eq("email", username)
            .eq("password", password).findUnique();
    }
}