/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.gp.SolutionSkeleton;
import com.neodynamica.lib.parameter.SearchParameter;
import com.neodynamica.lib.parameter.SearchParameterException;

/**
 * Validator to validate skeleton template
 *
 * @version 1.0
 * @since 1.0
 */
public final class SkeletonValidator {

    /**
     * Validate skeleton template
     *
     * @param value Value to be validated
     */
    public static void validate(String value) throws SearchParameterException {
        try {
            // null is valid to indicate no skeleton
            if (value != null) {
                //attempting to create if value is invalid syntax will throw an exception explaining why
                SolutionSkeleton.createOpFromString(value);
            }
        } catch (IllegalArgumentException e) {
            throw new SearchParameterException(e.getMessage());
        } catch (NullPointerException n) {
            throw new SearchParameterException("Skeleton is null.");
        }
    }

}
