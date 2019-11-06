/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate population size
 *
 * @version 1.0
 * @since 1.0
 */
public final class PopulationSizeValidator {

    /**
     * Validate population size value
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {

        // Make sure populationSize is integer
        try {

            int populationSize = Integer.parseInt(value);

            if (populationSize <= 0) {
                throw new SearchParameterException("populationSize must be greater than 0");
            }

        } catch (NumberFormatException e) {
            throw new SearchParameterException("Invalid populationSize. Only integer is accepted.");
        }

    }

}
