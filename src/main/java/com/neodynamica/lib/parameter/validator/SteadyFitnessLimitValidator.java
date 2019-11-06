/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate the steadyFitnessLimit value
 *
 * @version 1.0
 * @since 1.0
 */
public final class SteadyFitnessLimitValidator {

    /**
     * Validator to validate the steadyFitnessLimit value
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        //throw exception if parameter is null
        if (value == null) {
            throw new SearchParameterException("steadyFitnessLimit can not be null");
        }

        // If it was left blank, then throw an exception so it doesn't override a valid value.
        if (value.isEmpty()) {
            throw new SearchParameterException("steadyFitnessLimit not set");
        }

        // Make sure the value is an integer
        try {
            int size = Integer.parseInt(value);
            if (size <= 1) {
                throw new SearchParameterException("steadyFitnessLimit must be greater than 1");
            }
        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid steadyFitnessLimit. Only integer is accepted");
        }
    }
}
