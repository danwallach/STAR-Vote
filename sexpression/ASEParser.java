package sexpression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

        /* Convert to a list expression */
        /* Parse through each element of the list expression */
        /* Add them to params after they are converted into objects */
        /* (object [classname] (object [classname] [param] [param] [param]) (object [classname] [param]))*/
        /*      type c                         field                                  field              */

        /* Figure out parameter types from exp and parse into proper objects */
        List<Class> paramTypes = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        /* Convert this expression to a ListExpression */
        ListExpression object = (ListExpression) exp;

        /* Make sure that this is in fact an "object" */
        if ((object.get(0)).toString().equals("object")){

            for (int i=2; i<object.size(); i++) {

                /* Get the ASExpression */
                ASExpression cur = object.get(i);

                /* If this is an instance of ListExpression, convert to object */
                if (cur instanceof ListExpression) {

                    Object field = ASEParser.convert(cur);

                    /* Add the field to parameters and type to paramtypes*/
                    paramTypes.add(field.getClass());
                    params.add(field);
                }
            }
        }

        try{
            Constructor<T> constructor = c.getConstructor(paramTypes.toArray(new Class[paramTypes.size()]));
            return constructor.newInstance(params);}
        catch(Exception e) { return null; }
    }

    /**
     * Creates an object of the type specified by the first string in a ListExpression.
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
        try {
            return (Class<T>)(Class.forName(exp.get(1).toString()));
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    /**
     * Converts the given object into an ASExpression
     * @param obj
     * @return
     */
    public static ASExpression convert(Object obj) {
        
        List<ASExpression> expList = new ArrayList<>();
        
        /* Write the name of the Object */
        expList.add(StringExpression.make("object"));
        expList.add(StringExpression.make(obj.getClass().getName()));

        /* Get the list of parameters*/
        Field[] fields = obj.getClass().getDeclaredFields();

        /* Get each field object and convert */
        for (Field f : fields) {
            f.setAccessible(true);

            /* Convert this field to an ASE */
            try { expList.add(ASEParser.convert(f.get(obj))); }
            catch (Exception e) { e.printStackTrace(); }
        }


        return new ListExpression(expList);
    }

}
