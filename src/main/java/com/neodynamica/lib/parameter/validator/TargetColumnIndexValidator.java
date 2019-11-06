/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate target column index
 *
 * @version 1.0
 * @since 1.0
 */
public class TargetColumnIndexValidator {

    /**
     * Validate target column index
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {

        // Make sure the targetColumnIndex is integer
        try {
            int targetColumnIndex = Integer.parseInt(value);

            if (targetColumnIndex < -1) {
                throw new SearchParameterException("targetColumnIndex must be -1 or greater");
            }
        } catch (NumberFormatException e) {
            throw new SearchParameterException(
                    "Invalid targetColumnIndex. Only integer is accepted");
        }

    }

}
