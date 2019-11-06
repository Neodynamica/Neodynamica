/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.io;

import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Parser to parser search parameter config files.
 *
 * @version 1.0
 * @since 1.0
 */
public class SearchParameterParser {

    private static final String DEFAULT_CONFIG_FILE_PATH = "default.config";

    private String configFilePath;
    private SearchParameter searchParameter;

    /**
     * Initialise default value
     */
    public SearchParameterParser() {
        this.configFilePath = null;
        this.searchParameter = new SearchParameter();
    }

    /**
     * Initiate with config file path
     */
    public SearchParameterParser(String configFilePath) {
        this.configFilePath = configFilePath;
        this.searchParameter = new SearchParameter();
    }

    /**
     * Parse search parameter config file
     *
     * @return This object
     */
    public SearchParameterParser parse() throws IOException, SearchParameterException {
        Properties defaultConfig = new Properties(); // Default properties file

        // load default config file from resources
        InputStream in = getClass().getResourceAsStream("/" + DEFAULT_CONFIG_FILE_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        //set searchParameters initially to those from the default config file
        defaultConfig.load(reader);
        this.searchParameter = propertiesToSearchParameters(defaultConfig);

        // override defaults values with those in specified config file if it exists
        if (configFilePath != null) {
            Properties config = new Properties(defaultConfig);
            config.load(new FileReader(configFilePath)); // Load parameters from file
            this.searchParameter.overrideFrom(propertiesToSearchParameters(config));
        }

        return this;
    }

    /**
     * Get search parameter object
     *
     * @return Search parameter object
     */
    public SearchParameter getSearchParameterObject() {
        return this.searchParameter;
    }

    private static SearchParameter propertiesToSearchParameters(Properties config)
            throws IllegalArgumentException {
        SearchParameter sp = new SearchParameter();

        for (Field p : SearchParameter.class.getDeclaredFields()) {
            try {
                //Get the method with the name identical to the property name, with 'set' prepended and 1
                //argument of the appropriate type
                Method m = sp.getClass()
                        .getMethod(ParserUtils.setterFromPropertyName(p.getName()), p.getType());
                //Get the property value from the properties file
                String propertyFromFile = config.getProperty(p.getName());

                //If the property value from file is not present or left blank - skip and go to next parameter
                if (propertyFromFile == null || propertyFromFile.isEmpty()) {
                    continue;
                }
                //Invoke the setter method with the correct type
                if (p.getType().toString().equals("int")) {
                    m.invoke(sp, Integer.parseInt(propertyFromFile));
                } else if (p.getType().toString().equals("double")) {
                    m.invoke(sp, Double.parseDouble(propertyFromFile));
                } else if (p.getType().toString().equals("long")) {
                    m.invoke(sp, Long.parseLong(propertyFromFile));
                } else if (p.getType().toString().equals("class java.lang.Boolean")) {
                    m.invoke(sp, Boolean.parseBoolean(propertyFromFile));
                } else {
                    m.invoke(sp, propertyFromFile);
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }

        return sp;
    }
}
