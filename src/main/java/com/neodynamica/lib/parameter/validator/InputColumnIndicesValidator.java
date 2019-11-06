/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate input column indices
 *
 * @version 1.0
 * @since 1.0
 */
public class InputColumnIndicesValidator {

    /**
     * Validate target column index
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        try {
            //throw exception if parameter is null
            if (value == null) {
                throw new SearchParameterException("InputColumnIndices can not be null");
            }

            // The 'ALL' string indicates that every column should be included.
            //it is the only non numerical value allowed
            if (value.equalsIgnoreCase("ALL")) {
                return;
            }

            //if it was left blank, then throw an exception so it doesn't override a valid value.
            if (value.isEmpty()) {
                throw new SearchParameterException("InputColumnIndices not set");
            }

            // Indices are separated by commas
            String[] indices = value.split(",");

            for (int i = 0; i < indices.length; i++) {

                // Make sure the all indices integer
                try {

                    Integer.parseInt(indices[i]);

                } catch (NumberFormatException e) {
                    throw new SearchParameterException("Invalid input index: '" + indices[i]
                            + "' has been detected. Only integer is accepted");
                }
            }
        } catch (NullPointerException n) {
            throw new SearchParameterException("Input indices value is null.");
        }

    }

}
