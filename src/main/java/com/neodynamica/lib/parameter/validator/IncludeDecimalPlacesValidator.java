/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate number of decimal places used in constants
 *
 * @version 1.0
 * @since 1.0
 */
public final class IncludeDecimalPlacesValidator {

    /**
     * Validator to validate number of decimal places used in constants
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        //throw exception if parameter is null
        if (value == null) {
            throw new SearchParameterException("IncludeDecimals can not be null");
        }

        // If it was left blank, then throw an exception so it doesn't override a valid value.
        if (value.isEmpty()) {
            throw new SearchParameterException("IncludeDecimals not set");
        }

        // Make sure the value is an integer
        try {
            int places = Integer.parseInt(value);
            if (places < 0) {
                throw new SearchParameterException(
                        "includeDecimalPlaces must be greater than or equal to 0");
            } else if (places > 15) {
                throw new SearchParameterException(
                        "includeDecimalPlaces must be less than or equal to 15");
            }
        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid includeDecimalPlaces. Only integer is accepted");
        }
    }
}
