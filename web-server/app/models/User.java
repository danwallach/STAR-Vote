package models;

import crypto.adder.AdderPrivateKeyShare;
import org.apache.commons.codec.binary.Base64;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.*;

@Entity
public class User extends Model {

    @Id
    public String username;
    public String password;
    public String name;
    public String type;

    @Column(columnDefinition = "TEXT")
    public String key;

    
    public User(String username, String password, String type, String name) {
      this.username = username;
      this.password = password;
      this.type     = type;
      this.name     = name;
      this.key      = null;
    }

    public void setKey(AdderPrivateKeyShare key) {

        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(key);
            objectOutputStream.close();

            this.key = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
            save();
        }
        catch (IOException e) { e.printStackTrace(); }

    }

    public AdderPrivateKeyShare getKey() {

        if (key == null) return null;

        try {
            byte[] bytes = Base64.decodeBase64(this.key.getBytes());

            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));

            return (AdderPrivateKeyShare)objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException | ClassCastException e) { e.printStackTrace(); }

        return null;
    }

    public static Finder<String,User> find = new Finder<>(String.class, User.class);

    public static void create(User user) { user.save(); }

    /* This will authenticate our user */
     public static boolean authenticate(String username, String password, String type) {

        return (find.where().eq("username", username).eq("password", password).eq("type", type).findUnique() != null);
    }
}