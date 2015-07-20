package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import crypto.adder.AdderPrivateKeyShare;
import org.apache.commons.codec.binary.Base64;
import play.db.ebean.Model;
import security.Admin;
import security.Authority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User extends Model implements Subject {

    @Id
    public String username;
    public String password;
    public String name;

    public String userRole;

    @Column(columnDefinition = "TEXT")
    public String key;

    
    public User(String username, String password, String userRole, String name) {
      this.username = username;
      this.password = password;

      this.userRole = userRole;

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
            byteArrayOutputStream.close();

            save();
        }
        catch (IOException e) { e.printStackTrace(); }

    }

    public AdderPrivateKeyShare getKey() {

        if (key == null) return null;

        try {
            byte[] bytes = Base64.decodeBase64(this.key.getBytes());

            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));

            AdderPrivateKeyShare privateKeyShare = (AdderPrivateKeyShare)objectInputStream.readObject();
            objectInputStream.close();

            return privateKeyShare;
        }
        catch (IOException | ClassNotFoundException | ClassCastException e) { e.printStackTrace(); }

        return null;
    }

    public static Finder<String,User> find = new Finder<>(String.class, User.class);

    public static void create(User user) { user.save(); }

    /* This will authenticate our user */
     public static boolean authenticate(String username, String password, String role) {

        User thisUser = find.where().eq("username", username).eq("password", password).findUnique();
        return (thisUser != null && thisUser.userRole.equals(role));
    }

    @Override
    public List<? extends Role> getRoles() {
        ArrayList<Role> roleList = new ArrayList<>();

        if (userRole.equals("admin")) roleList.add(new Admin());
        if (userRole.equals("authority")) roleList.add(new Authority());

        return roleList;

    }

    public String getUserRole() {
        return this.userRole;
    }

    @Override
    public List<? extends Permission> getPermissions() {
        return new ArrayList<>();
    }

    @Override
    public String getIdentifier() {
        return username;
    }

    public String toString() {
        return "[User] Username: " + username + ", Password: " + password + ", Name: " + name + ", UserRole: " + userRole + ", Key: " + key;
    }
}