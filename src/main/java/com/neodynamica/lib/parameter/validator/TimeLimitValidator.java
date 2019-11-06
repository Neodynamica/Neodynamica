/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate the timeLimit value
 *
 * @version 1.0
 * @since 1.0
 */
public final class TimeLimitValidator {

    /**
     * Validator to validate the timeLimit value
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        //throw exception if parameter is null
        if (value == null) {
            throw new SearchParameterException("timeLimit can not be null");
        }

        // If it was left blank, then throw an exception so it doesn't override a valid value.
        if (value.isEmpty()) {
            throw new SearchParameterException("timeLimit not set");
        }

        // Make sure the value is an integer
        try {
            int size = Integer.parseInt(value);
            if (size <= 0) {
                throw new SearchParameterException("timeLimit must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid timeLimit. Only integer is accepted");
        }
    }

}
