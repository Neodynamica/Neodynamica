package com.neodynamica.lib.sample.io;

import io.jenetics.prog.op.Var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaIdentifierConverterTest {
    @Test
    public void testBOMCharacterRemoved(){
        assertEquals("a",JavaIdentifierConverter.convertToJavaValidIdentifier("\uFEFFa"));
    }

    @Test
    public void testIllegalFirstCharacterGetsUnderscorePrepended(){
        assertEquals("_3",JavaIdentifierConverter.convertToJavaValidIdentifier("3"));
    }

    @Test
    public void testIllegalCharactersReplacedWithUnderscores(){
        assertEquals("a___3",JavaIdentifierConverter.convertToJavaValidIdentifier("a-.^3"));
    }

    @Test
    public void testComboOfTheAbove(){
        String safeString = JavaIdentifierConverter.convertToJavaValidIdentifier("\uFEFF3$#`'j");
        assertEquals("_3$___j",safeString);
        assertDoesNotThrow(()-> Var.of(safeString,1));
    }
}