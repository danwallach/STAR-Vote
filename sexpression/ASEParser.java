package sexpression;


import org.objenesis.ObjenesisHelper;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

/**
 * A static utility to automate the process of creating toASE and fromASE methods. This can create a
 * relatively compact and human+machine-readable s-expression and convert it back to a copy of the
 * original class instance.
 *
 * Created by Matthew Kindy II on 11/21/2014.
 */
public class ASEParser {

    /**
     * Creates an object of class c from ASExpression exp.
     *
     * @param exp       the expression to convert into an object of class c
     * @param c         the class into which the expression will be converted
     * @param <T>       the type
     * @return
     */
    private static <T> T convertFromASE(ListExpression exp, Class<T> c) throws ConversionException {

        if (c==null) return null;
        if (exp==null) return null;

        ObjectInstantiator<T> tInstantiator = ObjenesisHelper.getInstantiatorOf(c);

        /* Convert to a list expression */
        /* Parse through each element of the list expression */
        /* (object [classname] ([fieldname] [classname] ([param]) (...) (...)) ([fieldname] [classname] ([param])))*/
        /*    type c           |-------------------------------- class field ASEs ------------------------------ | */



        /* Use Objenesis to construct a new (hopefully blank) instance of this class */
        T newObj = tInstantiator.newInstance();

        if (newObj instanceof Number || newObj instanceof String)
            return convertBasicType(exp,c);

        if (newObj instanceof ASExpression)
            return (T)convertASE(exp);

        try {
            if (newObj instanceof Collection) {
                T result = (T) convertCollection(exp, (Collection) newObj);
                return result;
            }
            if (newObj instanceof Map) {
                T result = convertMap(exp, (Map<?,?>) newObj);
                return result;
            }
        }
        catch (Exception e) {
            throw new ConversionException("Error during reconstruction of collection: " + exp +"\n\t" + e.getMessage());
        }

        for (int i=2; i<exp.size(); i++) {

            /* Get the ASExpression */
            ASExpression cur = exp.get(i);

            /* If this is an instance of ListExpression, convert to object */
            if (cur instanceof ListExpression) {

                //System.out.println("This ListExpression: " + cur);

                /* There ought to be no primitives here since they were autoboxed */
                Object value = ASEParser.convertFromASE((ListExpression) cur);

                //System.out.println("The value that got converted: " + value);

                /* Get the name of this field */
                String fieldName = ((ListExpression) cur).get(0).toString();
                //System.out.println("Field name: " + fieldName);

                /* Set the field in the blank object to the value in the ASE using reflection */
                Field f = null;
                Class curClass = c;

                /* Get the field with this name */
                try { f = c.getDeclaredField(fieldName); }
                catch (NoSuchFieldException ignored) {

                    try { f=c.getSuperclass().getDeclaredField(fieldName); }
                    catch (NoSuchFieldException e) {

                        throw new ConversionException("Could not find the field with specified name '"+ fieldName +
                                                      "' in " + c.getName() + " or " + c.getSuperclass().getName());
                    }
                }

                try {
                    /* This does not cause worry because it is automatically reset in Java */
                    f.setAccessible(true);

                    /* Set the new value */
                    f.set(newObj, value);
                }
                catch (IllegalAccessException e) { throw new ConversionException("Could not access the field for some reason during conversion."); }

            }
            else throw new ConversionException("Found an unexpected ASExpression in '" + exp + "'");

        }

        return newObj;
    }

    /**
     * Creates an object of the type specified by the first string in a ListExpression.
     *
     * @param exp       the expression to convert into an object of class c
     * @param <T>       the type
     *
     * @return          an instance of type T
     */
    public static <T> T convertFromASE(ListExpression exp) {

        try {
            T result = convertFromASE(exp, getClass(exp));
            return result; }
        catch (ConversionException e){
            System.err.println("Error converting from ASE: " + e.getClass() + " for " + exp);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns the class associated with the first string in exp
     *
     * @param exp       the expression to convert into an object of class c
     * @param <T>       the type
     *
     * @return          the class type associated with this ListExpression
     */
    private static <T> Class<T> getClass(ListExpression exp) throws ConversionException{
        String className = exp.get(1).toString();

        try {
            return className.equals("NULL") ? null : (Class<T>)(Class.forName(className));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConversionException("Error during casting inferred type: " + e.getMessage());
        }
    }

    /**
     * Converts the given object into an ASExpression, used for primary object
     *
     * @param obj       the object to be converted into an s-expression
     *
     * @return          the ListExpression representation of this object
     */
    public static ListExpression convertToASE(Object obj)  {

        try { return ASEParser.convertToASE(obj, "object"); }
        catch (ConversionException e) {e.printStackTrace();}

        return null;
    }

    /**
     * Converts the given object (field-derived) into an ASExpression
     *
     * @param obj       the object to be converted into an s-expression
     *
     * @return          the ListExpression representation of this object
     *
     * NOTE: This is currently set to only check for declared fields, up to one inheritance deep
     */
    private static ListExpression convertToASE(Object obj, String fieldName) throws ConversionException {

        if (obj instanceof Collection) {
            return convertCollection((Collection) obj, fieldName);
        }

        if (obj instanceof Map) {
            return convertMap((Map) obj, fieldName);
        }

        if (obj instanceof Number || obj instanceof String) {
            return convertBasicType(obj, fieldName);
        }

        if (obj instanceof ASExpression) {
            return convertASE((ASExpression)obj, fieldName);
        }

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        //System.out.println(fieldName);
        expList.add(StringExpression.make(fieldName));

        if (obj == null) {
            expList.add(StringExpression.make("NULL"));
            return new ListExpression(expList);
        }

        Class curClass = obj.getClass();
        expList.add(StringExpression.make(curClass.getName()));

        /* Get the list of fields */
        Field[] fields = curClass.getDeclaredFields();

        /* Concatenate those fields in the direct superclass */
        fields = Stream.concat(Arrays.stream(fields), Arrays.stream(curClass.getSuperclass().getDeclaredFields())).toArray(Field[]::new);

        /* Get each field object and convert */
        for (Field f : fields) {

            /* Skip if static */
            if (Modifier.isStatic(f.getModifiers()))
                continue;

            /* Convert this field to an ASE */
            try {
                f.setAccessible(true);

                /* This ought to autobox any primitives */
                expList.add(ASEParser.convertToASE(f.get(obj), f.getName()));
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        return new ListExpression(expList);
    }

    private static <T extends ASExpression> ListExpression convertASE(T ase, String fieldName){

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(ase.getClass().getName()));
        expList.add(ase);

        return new ListExpression(expList);

    }

    private static ASExpression convertASE(ListExpression exp){

        return exp.get(2);
    }


    /**
     * Converts a Collection into an s-expression
     *
     * @param col           the collection to be converted
     * @param fieldName     the name of this object as a field in a larger object (if applicable)
     * @param <T>           the type of the collection
     *
     * @return              the collection as a ListExpression
     */
    private static <T extends Collection> ListExpression convertCollection(T col, String fieldName) {

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(col.getClass().getName()));

        /* Convert the data in particular */
        for (Object o : col.toArray())
            expList.add(ASEParser.convertToASE(o));

        return new ListExpression(expList);
    }

    /**
     * Converts a ListExpression for a Collection (Set/ArrayList) into a class instance
     *
     * @param exp           the expression to convert into an object of class c
     * @param col           the collection created by Objenesis to be filled
     *
     * @return              a class instance of type T formed from the ListExpression
     *
     * @throws ConversionException if a bad cast occurs
     */
    private static <T> Collection<T> convertCollection(ListExpression exp, Collection<T> col) throws ConversionException {
        Collection<T> collection = Collections.synchronizedCollection(col);
        try {

            for (int i=2; exp != null && i<exp.size(); i++) {

                /* Get the ASExpression */
                ASExpression cur = exp.get(i);

                /* If this is an instance of ListExpression, convert to object */
                if (cur instanceof ListExpression) {
                    /* There ought to be no primitives here since they were autoboxed */
                    collection.add(ASEParser.convertFromASE((ListExpression) cur));
                }
            }

            collection = Collections.synchronizedCollection(collection);
            return collection;

        }
        catch (Exception e) { throw new ConversionException("Error during reconstruction of collection: " + e.getClass()
                                                            + "(" + e.getMessage() + ")" + " for " + exp); }
    }

    /**
     * Converts a Map (HashMap/TreeMap/etc.) into an s-expression
     *
     * @param m             the map to be converted into a ListExpression
     * @param fieldName     the name of this object as a field in a larger object (if applicable)
     * @param <T>           the type of the Map
     *
     * @return              the ListExpression formed from the converted map
     */
    private static <T extends Map<K,V>, K, V> ListExpression convertMap(T m, String fieldName) {

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(m.getClass().getName()));

        Set<Map.Entry<K,V>> entrySet = m.entrySet();

        /* Convert the data in particular */
        for (Map.Entry e : entrySet) {
            KeyValuePair kv = new KeyValuePair<>(e.getKey(), e.getValue());
            expList.add(ASEParser.convertToASE(kv));
        }

        return new ListExpression(expList);
    }

    /**
     * Converts a ListExpression for a Map into a class instance.
     *
     * @param exp       the expression to convert into an object of class c
     * @param m         the blank Map instance created using Objenesis
     * @param <T>       the type to be returned
     *
     * @return          a filled Map (type T)
     *
     * @throws ConversionException  if there is a casting exception thrown in the method
     */
    private static <T extends Map<K,V>, K, V> T convertMap (ListExpression exp, Map<K,V> m) throws ConversionException {

        Map<K,V> map = Collections.synchronizedMap(m);

        try {

            for (int i=2; i<exp.size(); i++) {

                /* Get the ASExpression */
                ASExpression cur = exp.get(i);

                //System.out.println("Current checked in map: " + cur);

                /* If this is an instance of ListExpression, convert to object */
                if (cur instanceof ListExpression) {

                    /* There ought to be no primitives here since they were autoboxed */
                    KeyValuePair<K,V> kv = ASEParser.convertFromASE((ListExpression) cur);

                    /* Add this object to the collection */
                    map.put(kv.getKey(),kv.getValue());
                }
            }

            map = Collections.synchronizedMap(map);

            return (T)map;

        }
        catch (ClassCastException e) { throw new ConversionException("Error during reconstruction of map: " + e.getMessage() + " for " + exp); }
    }

    /**
     * Converts a class instance of a basic type (Long/Integer/Float/Byte/Double/etc.) into an s-expression
     *
     * @param obj           the object to be converted into an s-expression
     * @param fieldName     the name of this object as a field in a larger object (if applicable)
     *                      "object" is the default value
     *
     * @return              a ListExpression of obj
     */
    private static ListExpression convertBasicType(Object obj, String fieldName) {

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(obj.getClass().getName()));

        expList.add(StringExpression.makeString(obj.toString()));
        return new ListExpression(expList);
    }

    /**
     * Converts a ListExpression for a basic type (Long/Integer/Float/Byte/Double/etc.) into a class instance
     *
     * @param exp       the expression to convert into an object of class c
     * @param c         the class into which the expression will be converted
     * @param <T>       the type
     * @return          an instance of type T.
     *
     * @throws ConversionException if a constructor can't be found that takes a String argument
     */
    private static <T> T convertBasicType(ListExpression exp, Class<T> c) throws ConversionException{
        try {
            return c.getConstructor(String.class).newInstance(exp.get(2).toString());
        } catch (Exception e) {
            throw new ConversionException("Error during construction of a basic class: " + e.getMessage());
        }
    }

}
