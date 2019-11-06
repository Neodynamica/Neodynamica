/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate error functions
 *
 * @version 1.0
 * @since 1.0
 */
public final class ErrorFunctionValidator {

    /**
     * Validate error function.
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        if (value == null || value.equals("")) {
            throw new SearchParameterException("Error function is not specified");
        }
    }

}
