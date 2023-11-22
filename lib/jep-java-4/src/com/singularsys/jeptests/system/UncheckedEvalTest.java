/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.misc.functions.Case;
import com.singularsys.jep.misc.functions.IsNull;
import com.singularsys.jep.standard.UncheckedEvaluator;

public class UncheckedEvalTest extends JepTest {


    @Override
    @Before
    public void setUp() {
        this.jep = new Jep();
        jep.setComponent(new StandardConfigurableParser());
        jep.setComponent(new UncheckedEvaluator());
        jep.setAllowAssignment(true);
        jep.setAllowUndeclared(true);
        jep.setImplicitMul(true);
    }

    @Test
    public void testNull() throws Exception {
	// check if null trapping is on by default
	printTestHeader("Testing for null values");
	jep.addFunction("isNull", new IsNull());
	jep.addConstant("mynull", null);

	// check if isNull(5) returns false as expected
	valueTest("isNull(5)", myFalse);
	// try calling setTrapNullValues(true) with reflection

	    try {
		valueTest("isNull(mynull)", myTrue);
		valueTestNull("nnn=mynull");
		valueTest("isNull(nnn)", myTrue);
		// valueTest("isNull(null)",myTrue);
	    } catch (Exception e) {
		fail("With TrapNullValues=false " + e.getMessage());
		e.printStackTrace();
	    }
    }

    @Test
    public void testSetAllowUndeclared() throws Exception {
	printTestHeader("Testing AllowedUndeclared options...");

	// test whether setAllowUndeclared(true) works
	jep.getVariableTable().clear(); // clear the Variable Table
	jep.setAllowUndeclared(true);
	try {
	    jep.parse("x");
	} catch (ParseException e) {
	    fail("Exception occurred " + e.getMessage());
	}
	VariableTable st = jep.getVariableTable();

	// should only contain a single variable x
	assertTrue(st.size() == 1);
	assertTrue(st.getVariable("x") != null);

	Object res =  jep.evaluate();
	assertNull(res);

	jep.setDefaultValue(Double.valueOf(0.0));
	jep.parse("y");
	Object val = jep.evaluate();
	assertEquals("Value of y using default value", 0.0, val);

	// test whether setAllowUndeclared(false) works
	jep.getVariableTable().clear();
	jep.addVariable("x", Double.valueOf(1));
	jep.setAllowUndeclared(false);
	try {
	    jep.parse("p");
	    // since p is not declared, an error should occur
	    fail("A ParseException should have been thrown creating variable 'p'.");
	} catch (ParseException e) {
	    // exception was thrown, so all is well
	}
    }

    protected Object negativeZero() {
	return -0.0;
    }
    
    @Test
    public void testCaseNull() throws Exception {
	jep.addFunction("cased", new Case(-1.0, Case.NullBehaviour.TEST_ARG));
	jep.addConstant("null", null);

	valueTest("cased(null,\"a\",5,null,6,\"c\",7)", 6.0);
	valueTest("cased(null,\"a\",5,\"b\",6,\"c\",7)", -1.0);
    }


}
