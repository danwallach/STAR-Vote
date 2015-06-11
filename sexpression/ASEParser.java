package sexpression;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Matthew Kindy II on 11/21/2014.
 */
public class ASEParser {

    private static Objenesis o = new ObjenesisStd();
    /**
     * Creates an object of class c from ASExpression exp.
     * @param exp       the expression to convert into an object of class c
     * @param c         the class into which the expression will be converted
     * @param <T>       the type
     * @return
     */
    private static <T> T convert(ListExpression exp, Class<T> c) throws ConversionException {

        ObjectInstantiator<T> tInstantiator = o.getInstantiatorOf(c);

        /* Convert to a list expression */
        /* Parse through each element of the list expression */
        /* (object [classname] ([fieldname] [classname] ([param]) (...) (...)) ([fieldname] [classname] ([param])))*/
        /*    type c           |-------------------------------- field  params --------------------------------- | */



        /* Use Objenesis to construct a new (hopefully blank) instance of this class */
        Objenesis o = new ObjenesisStd();
        T newObj = o.newInstance(c);

        if (newObj instanceof Number || newObj instanceof String)
            return convertBasicType(exp,c);

        try {
            if (newObj instanceof Collection)
                return convertCollection(exp, (Collection) newObj);

            if (newObj instanceof Map) {
                //System.out.println("IT'S A MAP!");
                return convertMap(exp, (Map) newObj);
            }
        }
        catch (Exception e) {
            throw new ConversionException("Error during reconstruction of collection: " + e.getMessage());
        }

        for (int i=2; i<exp.size(); i++) {

            /* Get the ASExpression */
            ASExpression cur = exp.get(i);

            /* If this is an instance of ListExpression, convert to object */
            if (cur instanceof ListExpression) {

                //System.out.println("This ListExpression: " + cur);

                /* There ought to be no primitives here since they were autoboxed */
                Object value = ASEParser.convert((ListExpression)cur);

                //System.out.println("The value that got converted: " + value);

                /* Get the name of this field */
                String fieldName = ((ListExpression) cur).get(0).toString();
                //System.out.println("Field name: " + fieldName);

                /* Set the field in the blank object to the value in the ASE using reflection */
                try {

                    /* Get the field with this name */
                    Field f = c.getDeclaredField(fieldName);

                    /* This does not cause worry because it is automatically reset in Java */
                    f.setAccessible(true);

                    /* Set the new value */
                    f.set(newObj, value);
                }
                catch (NoSuchFieldException | IllegalAccessException e){ System.err.println("Could not convert: " + e.getClass()); }

            }
            else{/*error off*/}

        }

        return newObj;
    }

    /**
     * Creates an object of the type specified by the first string in a ListExpression.
     * @param exp
     * @param <T>
     * @return
     */
    public static <T> T convert(ListExpression exp) {

        try { return convert(exp, getClass(exp)); }
        catch (ConversionException e){ e.printStackTrace(); }

        return null;
    }

    /**
     * Returns the class associated with the first string in exp
     * @param exp
     * @param <T>
     * @return
     */
    private static <T> Class<T> getClass(ListExpression exp) throws ConversionException{
        try {
            return (Class<T>)(Class.forName(exp.get(1).toString()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConversionException("Error during casting inferred type: " + e.getMessage());
        }
    }

    /**
     * Converts the given object into an ASExpression, used for primary object
     * @param obj
     * @return
     */
    public static ListExpression convert(Object obj)  {

        try { return ASEParser.convert(obj, "object"); }
        catch (ConversionException e) {e.printStackTrace();}

        return null;
    }

    /**
     * Converts the given object (field-derived) into an ASExpression
     * @param obj
     * @return
     */
    private static ListExpression convert(Object obj, String fieldName) throws ConversionException {

        if (obj instanceof Collection) {
            return convertCollection((Collection) obj, fieldName);
        }

        if (obj instanceof Map) {
            return convertMap((Map) obj, fieldName);
        }

        if(obj instanceof Number || obj instanceof String) {
            return convertBasicType(obj, fieldName);
        }

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(obj.getClass().getName()));

        /* Get the list of fields */
        Field[] fields = obj.getClass().getDeclaredFields();

        /* Get each field object and convert */
        for (Field f : fields) {

            /* Convert this field to an ASE */
            try {
                f.setAccessible(true);

                /* This ought to autobox any primitives */
                expList.add(ASEParser.convert(f.get(obj), f.getName()));
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        return new ListExpression(expList);
    }

    private static <T extends Collection> ListExpression convertCollection(T col, String fieldName) throws ConversionException {

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(col.getClass().getName()));

        /* Convert the data in particular */
        for (Object o : col.toArray())
            expList.add(ASEParser.convert(o));

        return new ListExpression(expList);
    }

    private static <T extends Collection> T convertCollection(ListExpression exp, Collection col) throws ConversionException {

        try {

            for (int i=2; i<exp.size(); i++) {

                /* Get the ASExpression */
                ASExpression cur = exp.get(i);

                /* If this is an instance of ListExpression, convert to object */
                if (cur instanceof ListExpression) {

                    /* There ought to be no primitives here since they were autoboxed */
                    Object value = ASEParser.convert((ListExpression)cur);

                    /* Add this object to the collection */
                    col.add(value);
                }
            }

            return (T)col;

        }
        catch (Exception e) { throw new ConversionException("Error during reconstruction of collection: " + e.getMessage()); }
    }

    private static <T extends Map> ListExpression convertMap(T m, String fieldName) throws ConversionException {

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(m.getClass().getName()));

        Set<Map.Entry> entrySet = m.entrySet();

        /* Convert the data in particular */
        for (Map.Entry e : entrySet) {
            KeyValue kv = new KeyValue<>(e.getKey(), e.getValue());
            expList.add(ASEParser.convert(kv));
        }

        return new ListExpression(expList);
    }

    private static <T extends Map> T convertMap (ListExpression exp, Map m) throws ConversionException {

        try {

            for (int i=2; i<exp.size(); i++) {

                /* Get the ASExpression */
                ASExpression cur = exp.get(i);

                //System.out.println("Current checked in map: " + cur);

                /* If this is an instance of ListExpression, convert to object */
                if (cur instanceof ListExpression) {

                    /* There ought to be no primitives here since they were autoboxed */
                    KeyValue kv = ASEParser.convert((ListExpression)cur);

                    /* Add this object to the collection */
                    m.put(kv.getKey(),kv.getValue());
                }
            }

            return (T)m;

        }
        catch (Exception e) { throw new ConversionException("Error during reconstruction of collection: " + e.getMessage()); }
    }

    private static ListExpression convertBasicType(Object obj, String fieldName) throws ConversionException {

        List<ASExpression> expList = new ArrayList<>();

        /* Write the name of the Object */
        expList.add(StringExpression.make(fieldName));
        expList.add(StringExpression.make(obj.getClass().getName()));

        expList.add(StringExpression.makeString(obj.toString()));
        return new ListExpression(expList);
    }

    private static <T> T convertBasicType(ListExpression exp, Class<T> c) throws ConversionException{
        try {
            return c.getConstructor(String.class).newInstance(exp.get(2).toString());
        } catch (Exception e) {
            throw new ConversionException("Error during construction of a basic class: " + e.getMessage());
        }
    }

}
