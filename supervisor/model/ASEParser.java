package supervisor.model;

import sexpression.ASExpression;
import sexpression.ListExpression;

import java.lang.reflect.Constructor;

/**
 * Created by Matthew Kindy II on 11/21/2014.
 */
public class ASEParser {

    /**
     * Creates an object of class c from ASExpression exp.
     * @param exp
     * @param c
     * @param <T>
     * @return
     */
    public static <T> T convert(ASExpression exp, Class<T> c) {


        /* Figure out parameter types from exp and parse into proper objects */
        Class[] paramTypes;
        Object[] params;

        try{
            Constructor<T> constructor = c.getConstructor(paramTypes);
            return constructor.newInstance(params);}
        catch(Exception e) { return null; }
    }

    /**
     * Creates an object of the type specified as the first string in a ListExpression.
     * @param exp
     * @param <T>
     * @return
     */
    public static <T> T convert(ListExpression exp) {
        return convert(exp, getClass(exp));
    }

    /**
     * Returns the class associated with the first string in exp
     * @param exp
     * @param <T>
     * @return
     */
    private static <T> Class<T> getClass(ListExpression exp) {

    }

    /**
     * Converts the given object into an ASExpression
     * @param c
     * @param <T>
     * @return
     */
    public static <T> ASExpression convert(T c) {
        return null;
    }



}
