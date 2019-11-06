/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.parameter.validator;

import com.neodynamica.lib.parameter.SearchParameterException;
import io.jenetics.prog.op.MathOp;

/**
 * Validator to validate operators
 *
 * @version 1.0
 * @since 1.0
 */
public final class OperatorsValidator {

    /**
     * Validate operators.
     *
     * @param value Value to be validated.
     */
    public static void validate(String value) throws SearchParameterException {
        try {
            // Chunk the string by splitting ','
            String[] ops = value.split(",");

            // Make sure it is a valid MathOp
            for (int i = 0; i < ops.length; i++) {
                MathOp.valueOf(ops[i].toUpperCase());
            }

        } catch (IllegalArgumentException iae) {
            throw new SearchParameterException("Invalid operator is specified");
        } catch (NullPointerException npe) {
            throw new SearchParameterException("Operator is not specified");
        }

    }

}
