package util;

import org.tzi.use.uml.ocl.type.Type;

public class OCLUtil {
    /**
     * Method to transform Strings stored in the database into valid OCL expressions for USE.
     *
     * @param type  Type of the parameter retrieved from the database.
     * @param value Value of the parameter retrieved.
     * @return A String valid for USE.
     */
    public static String setOCLExpression(Type type, String value) {
        if (type.isTypeOfReal() && !value.contains(".")) {
            value += ".0";
        } else if (type.isTypeOfString()) {
            value = "'" + value + "'";
        } else if (type.isTypeOfBoolean()) {
            if (value.equals("0")) {
                value = "false";
            } else {
                value = "true";
            }
        } else if (type.isTypeOfEnum()) {
            value = type + "::" + value;
        } else if(type.isTypeOfSequence()){
            value = "Sequence{" + value + "}";
        }
        return value;
    }
}
