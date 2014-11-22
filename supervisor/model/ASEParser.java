package supervisor.model;

import sexpression.ASExpression;

import java.lang.reflect.Constructor;

/**
 * Created by Matthew Kindy II on 11/21/2014.
 */
public class ASEParser {

    public static <T> T convert(ASExpression exp, Class<T> c) {


        /* Figure out parameter types and parse into proper objects */
        Class[] paramTypes;
        Object[] params;


        try{
            Constructor<T> constructor = c.getConstructor(paramTypes);
            return (T) constructor.newInstance(params);}
        catch(Exception e) { return null; }
    }

    public static <T> ASExpression convert(T c) {

        return null;
    }



}
