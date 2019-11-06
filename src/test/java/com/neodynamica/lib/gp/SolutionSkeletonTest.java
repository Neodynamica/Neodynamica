package com.neodynamica.lib.gp;

import io.jenetics.prog.op.Op;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolutionSkeletonTest {

    @Test
    void createOpFromStringSubstitutesQuestionMarks() {
        Object myOp = SolutionSkeleton.createOpFromString("?+?+(?/?)+5");
        assertEquals("x1+x2+(x3/x4)+5", myOp.toString());
    }

    @Test
    void skeletonExecutesCorrectlyWithCorrectNumberOfParameters() {
        Op<Double> myOp = SolutionSkeleton.createOpFromString("?+?+(?/?)+5");
        Double[] inputs = {1.0, 2.0, 1.0, 2.0};
        assertEquals(myOp.apply(inputs), 8.5);
    }

    @Test
    void skeletonExecutesWithSpecifiedVariableNames() {
        Op<Double> myOp = SolutionSkeleton.createOpFromString("a+foo+(foo/?)+5");
        Double[] inputs = {1.0, 2.0, 2.0};
        assertEquals(myOp.apply(inputs), 9);
    }

    @Test
    void testEmptyParameterThrowsException() {
        assertAll("Parameter not provided throws NullPointerException",
                () -> assertThrows(NullPointerException.class, () ->
                        SolutionSkeleton.createOpFromString(null)),
                () -> assertThrows(NullPointerException.class, ()
                        -> SolutionSkeleton.createOpFromString(""))
        );
    }

    @Test
    void testInvalidMathExprSyntaxThrowsException() {
        assertAll("Invalid MathExpr syntax",

                () -> assertThrows(IllegalArgumentException.class, ()
                        -> SolutionSkeleton.createOpFromString("2? + 3p")),
                () -> assertThrows(IllegalArgumentException.class, ()
                        -> SolutionSkeleton.createOpFromString("2x[ + 3"))
        );
    }


}