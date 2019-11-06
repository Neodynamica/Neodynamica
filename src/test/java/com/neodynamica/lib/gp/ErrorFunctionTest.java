package com.neodynamica.lib.gp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ErrorFunctionTest {

    private Double[] calculated = {1.0, 2.0, 3.0, 4.0, 5.0};
    private Double[] expected =   {3.0, 5.0, 7.0, 9.0, 11.0};

    @Test
    void mse() {
        assertEquals(18,ErrorFunction.mse(calculated,expected));
    }

    @Test
    void rmse() {
        assertEquals(Math.sqrt(18),ErrorFunction.rmse(calculated,expected));
    }

    @Test
    void mae() {
        assertEquals(4,ErrorFunction.mae(calculated,expected));
    }

    @Test
    void rgf() {
        assertEquals(-3.125,ErrorFunction.rgf(calculated,expected));
    }

    @Test
    void wc() {
        assertEquals(6,ErrorFunction.wc(calculated,expected));
    }

    @Test
    void pcc() {
        assertEquals(1,ErrorFunction.pcc(calculated,expected));
    }

    @Test
    void med() {
        assertEquals(4,ErrorFunction.med(calculated,expected));
    }

    @Test
    void iqae() {
        assertEquals(4,ErrorFunction.iqae(calculated,expected));
    }

    @Test
    void invalidErrorFunction(){
        assertThrows(IllegalArgumentException.class, () ->
        {
            ErrorFunction.setErrorType("badName");
            ErrorFunction.calculateError(calculated,expected);
        });
    }
}