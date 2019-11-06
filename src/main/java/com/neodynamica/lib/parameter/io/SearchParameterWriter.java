/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.io;

import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * SearchParameterWriter writes search parameters into config file
 *
 * @version 1.0
 * @since 1.0
 */
public final class SearchParameterWriter {

    /**
     * Write SearchParameter into file
     */
    public static void toFile(SearchParameter searchParameter, String configFilePath)
            throws IOException, SearchParameterException {
        Properties config = new Properties();

        for (Field p : SearchParameter.class.getDeclaredFields()) {
            try {
                //Get the method with the name identical to the property name, with "get" prepended
                Method m = searchParameter.getClass()
                        .getMethod(ParserUtils.getterFromPropertyName(p.getName()));
                //Get the value of the property to save to Properties file
                String propertyToSave = String.valueOf(m.invoke(searchParameter));
                //Write the property to file
                config.setProperty(p.getName(), propertyToSave);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }

        config.store(new FileOutputStream(configFilePath), null);
    }

}
