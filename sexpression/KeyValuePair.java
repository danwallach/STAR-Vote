package sexpression;

import com.sun.istack.internal.NotNull;

/**
 * Created by Matthew Kindy II on 6/10/2015.
 */
public class KeyValuePair<K,V> {

    @NotNull
    K key;

    V value;

    public KeyValuePair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @NotNull
    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public String toString(){
       return "Key: " + key + ", Value: " + value;
    }
}
