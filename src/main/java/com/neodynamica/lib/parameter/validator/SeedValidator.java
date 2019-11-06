package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;

public class SeedValidator {

    public static void validate(String value) throws SearchParameterException {
        try {
            if (Long.parseLong(value) == 0) {
                throw new SearchParameterException("No seed was specified");
            }
        } catch (NumberFormatException nfe) {
            throw new SearchParameterException("Seed must be a whole number");
        }
    }
}
