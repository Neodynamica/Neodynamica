package com.neodynamica.lib.parameter.io;

public class ParserUtils {

    /**
     * Returns properly camel-cased name of getter relative to the property name
     *
     * @param property the name of the property (eg. firstProperty)
     * @return the name of the getter (eg. getFirstProperty)
     */
    public static String getterFromPropertyName(String property) {
        return "get" + String.valueOf(property.charAt(0)).toUpperCase() + property.substring(1);
    }

    /**
     * Returns properly camel-cased name of setter relative to the property name
     *
     * @param property the name of the property (eg. firstProperty)
     * @return the name of the setter (eg. setFirstProperty)
     */
    public static String setterFromPropertyName(String property) {
        return "set" + String.valueOf(property.charAt(0)).toUpperCase() + property.substring(1);
    }
}
