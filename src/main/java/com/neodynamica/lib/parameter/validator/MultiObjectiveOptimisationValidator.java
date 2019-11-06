package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;

public class MultiObjectiveOptimisationValidator {

    public static void validate(Boolean value) throws SearchParameterException {
        if (value == null) {
            throw new SearchParameterException(
                    "multiObjectiveOptimisation must be a boolean 'true/false'");
        }
    }
}
