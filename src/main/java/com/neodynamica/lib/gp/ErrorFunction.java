/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.gp;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ErrorFunction {

    private static String errorFunction;

    static void setErrorType(String error) {
        errorFunction = error;
    }

    static double mse(Double[] calculated, Double[] expected) {
        if (expected.length != calculated.length) {
            throw new IllegalArgumentException(String
                    .format("Expected result and calculated results have different length: %d != %d",
                            expected.length, calculated.length));
        } else {
            double result = 0.0D;

            for (int i = 0; i < expected.length; ++i) {
                result += (expected[i] - calculated[i]) * (expected[i] - calculated[i]);
            }

            if (expected.length > 0) {
                result /= expected.length;
            }

            return result;
        }
    }

    static double rmse(Double[] calculated, Double[] expected) {
        return Math.sqrt(mse(calculated, expected));
    }

    static double mae(Double[] calculated, Double[] expected) {
        if (expected.length != calculated.length) {
            throw new IllegalArgumentException(String
                    .format("Expected result and calculated results have different length: %d != %d",
                            expected.length, calculated.length));
        } else {
            double result = 0.0D;

            for (int i = 0; i < expected.length; ++i) {
                result += Math.abs(expected[i] - calculated[i]);
            }

            if (expected.length > 0) {
                result /= expected.length;
            }

            return result;
        }
    }

    static double rgf(Double[] calculated, Double[] expected) {
        if (expected.length != calculated.length) {
            throw new IllegalArgumentException(String
                    .format("Expected result and calculated results have different length: %d != %d",
                            expected.length, calculated.length));
        } else {

            double averageExpectedError = Arrays.stream(expected)
                    .mapToDouble(
                            targetValue -> targetValue
                    ).average().orElse(Double.NaN);

            double SStot = Arrays.stream(expected)
                    .mapToDouble(
                            targetValue -> Math.pow(targetValue - averageExpectedError, 2)
                    ).sum();

            double SSres = Arrays.stream(expected)
                    .mapToDouble(
                            targetValue -> Math.pow(targetValue - calculated[1], 2)
                    ).sum();

            return 1 - (SSres / SStot);
        }
    }

    static double wc(Double[] calculated, Double[] expected) {
        if (expected.length != calculated.length) {
            throw new IllegalArgumentException(String
                    .format("Expected result and calculated results have different length: %d != %d",
                            expected.length, calculated.length));
        } else {

            double result = 0.0D;

            for (int i = 0; i < expected.length; ++i) {
                if (Math.abs(calculated[i] - expected[i]) > result) {
                    result = Math.abs(calculated[i] - expected[i]);
                }
            }

            return result;
        }
    }

    static double pcc(Double[] calculated, Double[] expected) {
        if (expected.length != calculated.length) {
            throw new IllegalArgumentException(String
                    .format("Expected result and calculated results have different length: %d != %d",
                            expected.length, calculated.length));
        } else {
            PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

            return pearsonsCorrelation.correlation(
                    Arrays.stream(expected).mapToDouble(r -> r).toArray(),
                    Arrays.stream(calculated).mapToDouble(r -> r).toArray()
            );
        }
    }

    static double med(Double[] calculated, Double[] expected) {
        if (expected.length != calculated.length) {
            throw new IllegalArgumentException(String
                    .format("Expected result and calculated results have different length: %d != %d",
                            expected.length, calculated.length));
        } else {

            // Calculate all absolute errors
            double[] absoluteErrors = new double[expected.length];
            for (int i = 0; i < expected.length; ++i) {
                absoluteErrors[i] = Math.abs(calculated[i] - expected[i]);
            }

            // Sort ASC order
            Arrays.sort(absoluteErrors);

            return absoluteErrors[expected.length / 2];
        }
    }

    static double iqae(Double[] calculated, Double[] expected) {
        if (expected.length != calculated.length) {
            throw new IllegalArgumentException(String
                    .format("Expected result and calculated results have different length: %d != %d",
                            expected.length, calculated.length));
        } else {

            // Calculate all absolute errors
            double[] absoluteErrors = new double[expected.length];
            for (int i = 0; i < expected.length; ++i) {
                absoluteErrors[i] = Math.abs(calculated[i] - expected[i]);
            }

            // Sort ASC order
            Arrays.sort(absoluteErrors);

            return Arrays.stream(
                    Arrays.copyOfRange(
                            absoluteErrors,
                            absoluteErrors.length / 4,
                            absoluteErrors.length - (absoluteErrors.length / 4)
                    )
            ).boxed().mapToDouble(val -> val).average().orElse(Double.NaN);
        }
    }

    static double calculateError(Double[] calculated, Double[] expected) {
        switch (errorFunction) {
            case "MAE":
            case "MeanAbsoluteError":
                return mae(calculated, expected);
            case "MSE":
            case "MeanSquaredError":
                return mse(calculated, expected);
            case "RMSE":
            case "RootMeanSquaredError":
                return rmse(calculated, expected);
            case "RGF":
            case "R2GoodnessOfFit":
                return rgf(calculated, expected);
            case "WC":
            case "WorstCase":
                return wc(calculated, expected);
            case "PCC":
            case "PearsonsCorrelationCoefficient":
                return pcc(calculated, expected);
            case "MED":
            case "MedianAbsoluteError":
                return med(calculated, expected);
            case "IQAE":
            case "InterquartileAbsoluteError":
                return iqae(calculated, expected);
            default:
                throw new IllegalArgumentException("Invalid error function name: " + errorFunction);
        }
    }

    /**
     * Returns a list of all currently supported error function names. Users can enter either the
     * full name or corresponding abbreviation in brackets (case sensitive) to refer to each error
     * function.
     *
     * @return list of Strings and corresponding abbreviations which can be entered as
     * errorFunctionName into ErrorFunction.factory(errorFunctionName)
     */
    public static List<String> supportedErrors() {
        List<String> errors = new LinkedList<>();
        errors.add("MeanAbsoluteError");
        errors.add("MeanSquaredError");
        errors.add("RootMeanSquaredError");
        errors.add("R2GoodnessOfFit");
        errors.add("WorstCase");
        errors.add("PearsonsCorrelationCoefficient");
        errors.add("MedianAbsoluteError");
        errors.add("InterquartileAbsoluteError");

        return errors;

    }
}
