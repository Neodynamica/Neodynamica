/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate maximum generation
 *
 * @version 1.0
 * @since 1.0
 */
public final class MaxGenerationsValidator {

    /**
     * Validate maximum generation value.
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {

        // Make sure the maxGenerations is integer
        try {

            int maxGenerations = Integer.parseInt(value);

            if (maxGenerations <= 0) {
                throw new SearchParameterException("maxGenerations must be greater than 0");
            }

        } catch (NumberFormatException e) {
            throw new SearchParameterException("Invalid maxGenerations. Only integer is accepted");
        }

    }

}
