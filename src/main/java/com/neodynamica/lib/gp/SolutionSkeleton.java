/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.gp;

import com.neodynamica.lib.parameter.SearchParameterException;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.prog.op.Op;

import java.util.regex.Pattern;

/**
 * @version 1.0
 * @since 1.0
 */
public class SolutionSkeleton {

    /**
     * Takes a user entered string for a solution skeleton, and converts it into a custom Op object
     * to be passed to the OPERATIONS ISeq used by the Jenetics Engine. The string must conform to
     * the syntax of strings parsed by io.jenetics.prog.op.MathExpr, can use any operations defined
     * in io.jenetics.prog.op.MathOp,
     * <p>
     * A '?' is used in place of unknown inputs to the skeleton. Alternatively, any series of
     * charcters beginning with a letter will be interpreted as a variable name, which can be useful
     * if you want the same input to be used multiple times within the skeleton.
     * <p>
     * Valid examples: 4*? + 83 - sin(?)/? 2*var1 + 6*a / 7*var1 - foo
     *
     * @param expr - a String representation of a solutionSkeleton
     * @return - A custom Op, which can be added to OPERATIONS
     * @throws NullPointerException - if expr == null
     * @throws IllegalArgumentException - if expr is not valid MathExpr syntax, such as -empty
     * string -contains unsupported characters e.g. ']', -contains invalid math syntax e.g. 2?
     * instead of 2*?
     */
    public static Op<Double> createOpFromString(String expr) {
        try {
            expr = convertQMsToVarNames(expr);
            MathExpr m = MathExpr.parse(expr);
            return Op.of(expr, m.vars().length(), v -> m.apply(v));
        } catch (IllegalArgumentException e) {
            //throw different error depending on whether expr was invalid or simply not provided
            if (expr == null || expr.equals("")) {
                throw new NullPointerException("No skeleton provided");
            } else {
                throw new IllegalArgumentException(
                        "Invalid MathExpr syntax in skeleton \"" + expr
                                + "\" - could not convert to Op.");
            }
        }

    }

    /**
     * Takes a string which will be later be parsed to instantiate a MathExpr, replacing any
     * instances of the '?' character with unique, sequential variable names x1, x2, x3,... etc, so
     * that a user entered string with '?' in place of unknowns can be parsed by the MathExpr class.
     * e.g. 4*? + 83 - sin(?)/? ->  4*x1 + 83 - sin(x2)/x3
     *
     * @param expr - a String reperesenting a user entered mathematical expression
     * @return - that string, but with unique variable names in place of each '?'
     */
    private static String convertQMsToVarNames(String expr) {
        int nextVarNo = 1;
        while (expr.contains("?")) {
            expr = expr.replaceFirst(Pattern.quote("?"), "x" + nextVarNo);
            nextVarNo++;
        }
        return expr;
    }

}
