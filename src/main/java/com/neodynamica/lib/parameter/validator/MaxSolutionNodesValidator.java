/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate maximum number of solution nodes.
 *
 * @version 1.0
 * @since 1.0
 */
public final class MaxSolutionNodesValidator {

    public static void validate(String value) throws SearchParameterException {

        // Make sure the maxSolutionNodes is integer
        try {

            int maxSolutionNodes = Integer.parseInt(value);

            if (maxSolutionNodes <= 0) {
                throw new SearchParameterException("maxSolutionNodes must be greater than 0");
            }

        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid maxSolutionNodes. Only integer is accepted");
        }

    }

}
