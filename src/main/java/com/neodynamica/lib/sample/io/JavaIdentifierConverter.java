package com.neodynamica.lib.sample.io;

public class JavaIdentifierConverter {
    /**
     * Since upgrading to Jenetics 5.0, all Var names have to be valid Java identifiers
     * or the program will crash. Therefore, if we are to supply labels as Var names,
     * we must first check that they fit Java valid identifier standards.
     * This method achieves that standard by
     * - removing the UTF meta character if it's found,
     * - adding an underscore to the beginning if the start character is invalid
     * - replacing any illegal characters with underscores
     * @param s - the String to convert
     * @return a modified version of that String which will pass as a valid Java variable name
     */
    public static String convertToJavaValidIdentifier(String s) {
        //remove meta-character which sometimes appears at the front of a file to label as UTF-8
        String UTF8_BOM = "\uFEFF";
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }

        //if first character is not a valid identifier, add an underscore in front
        if(!Character.isJavaIdentifierStart(s.charAt(0))){
            s = "_" + s;
        }

        //replace any illegal characters with underscores
        for(int i=1; i < s.length(); i++){
            StringBuilder myName = new StringBuilder(s);
            if(!Character.isJavaIdentifierPart(s.charAt(i))){
                myName.setCharAt(i, '_');
            }
            s = myName.toString();
        }
        return s;
    }
}
