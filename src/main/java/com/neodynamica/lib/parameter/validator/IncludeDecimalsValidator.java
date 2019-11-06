/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate include decimals
 *
 * @version 1.0
 * @since 1.0
 */
public class IncludeDecimalsValidator {

    /**
     * Validate include decimals value
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        try {
            //throw exception if parameter is null
            if (value == null) {
                throw new SearchParameterException("IncludeDecimals can not be null");
            }

            // If it was left blank, then throw an exception so it doesn't override a valid value.
            if (value.isEmpty()) {
                throw new SearchParameterException("IncludeDecimals not set");
            }

            // Indices are separated by commas
            String[] indices = value.split(",");
            if (indices.length != 2) {
                throw new SearchParameterException(
                        "Invalid number of includeDecimals given. 2 required and " + indices.length
                                + " were given.");
            }

            // Make sure the all indices integer
            for (String index : indices) {
                try {
                    Integer.parseInt(index);
                } catch (NumberFormatException e) {
                    throw new SearchParameterException(
                            "Invalid input index: '" + index
                                    + "' has been detected. Only integers are accepted");
                }
            }
        } catch (NullPointerException n) {
            throw new SearchParameterException("Include Decimals value is null.");
        }
    }
}
