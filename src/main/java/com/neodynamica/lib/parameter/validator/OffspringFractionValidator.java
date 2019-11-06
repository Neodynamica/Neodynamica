/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate offspringFraction value
 *
 * @version 1.0
 * @since 1.0
 */
public final class OffspringFractionValidator {

    /**
     * Validator to validate offspringFraction value
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        //throw exception if parameter is null
        if (value == null) {
            throw new SearchParameterException("OffspringFraction can not be null");
        }

        // If it was left blank, then throw an exception so it doesn't override a valid value.
        if (value.isEmpty()) {
            throw new SearchParameterException("OffspringFraction not set");
        }

        // Make sure the value is an integer
        try {
            double fraction = Double.parseDouble(value);
            if (fraction < 0) {
                throw new SearchParameterException(
                        "OffspringFraction must be greater than or equal to 0");
            } else if (fraction > 1) {
                throw new SearchParameterException(
                        "OffspringFraction must be less than or equal to 1");
            }
        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid OffspringFraction. Only double is accepted");
        }
    }
}
