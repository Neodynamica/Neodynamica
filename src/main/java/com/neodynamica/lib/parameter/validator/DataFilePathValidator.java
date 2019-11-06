/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

import java.io.File;

/**
 * Validator to validate data file path
 *
 * @version 1.0
 * @since 1.0
 */
public final class DataFilePathValidator {

    /**
     * Validate data file path
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {

        try {

            File file = new File(value);

            // Check if file exists
            if (!file.exists()) {
                throw new SearchParameterException("Data file: '" + value + "' does not exist");
            }

            // Check if value is directory
            if (file.isDirectory()) {
                throw new SearchParameterException("Data file: '" + value + "' is a directory");
            }

        } catch (NullPointerException e) {
            throw new SearchParameterException("Data file path is not set.");
        }

    }

}
