/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate initial solution depth.
 *
 * @version 1.0
 * @since 1.0
 */
public final class InitialSolutionDepthValidator {

    /**
     * Validate initia solution depth.
     *
     * @param value Value to be validated.
     */
    public static void validate(String value) throws SearchParameterException {

        // Make sure the initialSolutionDepth is integer
        try {

            int initialSolutionDepth = Integer.parseInt(value);

            if (initialSolutionDepth < 2) {
                throw new SearchParameterException("initialSolutionDepth must be at least 2");
            }

        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid initialSolutionDepth. Only integer is accepted");
        }

    }

}
