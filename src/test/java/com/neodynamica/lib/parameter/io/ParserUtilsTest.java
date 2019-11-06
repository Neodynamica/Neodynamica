package com.neodynamica.lib.parameter.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserUtilsTest {

    @Test
    public void testGetterFromPropertyName() {
        assertEquals("getMaxGenerations", ParserUtils.getterFromPropertyName("maxGenerations"));
    }

    @Test
    public void testSetterFromPropertyName() {
        assertEquals("setMaxGenerations", ParserUtils.setterFromPropertyName("maxGenerations"));
    }
}
