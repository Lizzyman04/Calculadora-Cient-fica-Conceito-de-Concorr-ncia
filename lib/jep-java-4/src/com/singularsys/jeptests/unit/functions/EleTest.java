/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.functions;

import org.junit.Assert;
import org.junit.Test;
import com.singularsys.jep.*;

public class EleTest {

    Jep jep;

    public EleTest() {
        jep = new Jep();
    }


    @Test
    public void testEle() {

        try {
            jep.parse("x = [1, 2, 3, 4, 5]");
            jep.evaluate();

            // get element values
            ensureValue("x[1]", Double.valueOf(1.0));
            ensureValue("x[2]", Double.valueOf(2.0));
            ensureValue("x[3]", Double.valueOf(3.0));
            ensureValue("x[4]", Double.valueOf(4.0));
            ensureValue("x[5]", Double.valueOf(5.0));
            ensureValue("x[3-1]", Double.valueOf(2.0));

            // set elements
            jep.parse("x[1] = 100");
            jep.evaluate();
            ensureValue("x[1]", Double.valueOf(100.0));

        } catch (Exception e) {
            Assert.fail("Exception thrown:" + e.getMessage());
        }

        // ensure out of bound exceptions
        try {
            jep.parse("x[0]");
        } catch (ParseException e) {
            Assert.fail();
            e.printStackTrace();
        }

        try {
            jep.evaluate();
            Assert.fail("Expected exception not received");
        } catch (Exception e) {
        }

        // ensure out of bound exceptions
        try {
            jep.parse("x[100]");
        } catch (ParseException e) {
            Assert.fail();
            e.printStackTrace();
        }

        try {
            jep.evaluate();
            Assert.fail("Expected exception not received");
        } catch (Exception e) {
        }
    }
    
	private void ensureValue(String expr, double expectedValIn) throws Exception {
    	Double expectedVal = expectedValIn;
        jep.parse(expr);
        Object retval = jep.evaluate();
        
        if (expectedVal instanceof Double) {
            Assert.assertEquals(expectedVal, retval);
            System.out.println(expr + " = " + expectedVal);
        } else {
            Assert.fail();
        }
    }
}
