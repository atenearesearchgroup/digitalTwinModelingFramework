package util;

public class StringUtil {
    public static String toCamel(String s){
        return (s.substring(0,1).toUpperCase() + s.substring(1));
    }
}
