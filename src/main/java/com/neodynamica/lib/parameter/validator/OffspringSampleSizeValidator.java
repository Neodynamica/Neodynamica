/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate number of offspring in a sample size
 *
 * @version 1.0
 * @since 1.0
 */
public final class OffspringSampleSizeValidator {

    /**
     * Validator to validate number of offspring in a sample size
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        //throw exception if parameter is null
        if (value == null) {
            throw new SearchParameterException("offspringSampleSize can not be null");
        }

        // If it was left blank, then throw an exception so it doesn't override a valid value.
        if (value.isEmpty()) {
            throw new SearchParameterException("offspringSampleSize not set");
        }

        // Make sure the value is an integer
        try {
            int size = Integer.parseInt(value);
            if (size <= 1) {
                throw new SearchParameterException("offspringSampleSize must be greater than 1");
            }
        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid offspringSampleSize. Only integer is accepted");
        }
    }
}
