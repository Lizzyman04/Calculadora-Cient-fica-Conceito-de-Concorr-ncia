/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.singularsys.jep.reals.RealEvaluator;

public class RealEvalTest extends CPTest {

	@Override
	@Before
	public void setUp() {
		super.setUp();
		jep.setComponent(new RealEvaluator());
		this.myFalse = Double.valueOf(0.0);
		this.myTrue = Double.valueOf(1.0);
	}

	@Override
	protected void valueTest(String expr, Object expected) throws Exception {
		if (expected instanceof Integer)
			super.valueTest(expr, ((Integer) expected).doubleValue());
		else
			super.valueTest(expr, expected);
	}

	@Test
	@Ignore("Complex tests not run for RealEvaluator")
	@Override
	public void testEvaluateComplex() throws Exception {
	}

	@Test
	@Ignore("String tests not run for RealEvaluator")
	@Override
	public void testEvaluateString() throws Exception {
	}

	@Test
	@Ignore("Complex tests not run for RealEvaluator")
	@Override
	public void testComplex() throws Exception {
	}

	@Ignore("Complex tests not run for RealEvaluator")
	@Override
	@Test
	public void testChangeVariableComplex() {
	}

	@Test
	@Ignore("Array tests not run for RealEvaluator")
	@Override
	public void testListAccess() throws Exception {
	}

	@Test
	@Ignore("Array tests not run for RealEvaluator")
	@Override
	public void testListAccessShiftZero() throws Exception {
	}

	@Ignore("Array tests not run for RealEvaluator")
	@Override
	@Test
	public void testListFunctions() throws Exception {
	}

	@Test
	@Ignore("Array tests not run for RealEvaluator")
	@Override
	public void testListExtra() throws Exception {
	}

	@Ignore("Array tests not run for RealEvaluator")
	@Override
	@Test
	public void testMultiDimArray() throws Exception {
	}

	@Ignore("Array tests not run for RealEvaluator")
	@Override
	@Test
	public void testMultiDimArrayAccess() throws Exception {
	}

	@Ignore("Array tests not run for RealEvaluator")
	@Test
	@Override
	public void testDepth3ArrayAccess() throws Exception {
	}

	@Override
	@Ignore("Array tests not run for RealEvaluator")
	@Test
	public void testMultiDimArrayShiftZero() throws Exception {
	}

	@Override
	@Ignore("Array tests not run for RealEvaluator")
	@Test
	public void testMultiDimArrayAccessShiftZero() throws Exception {
	}

	@Ignore("Array tests not run for RealEvaluator")
	@Override
	@Test
	public void testDepth3ArrayAccessShiftZero() throws Exception {
	}

	@Ignore("String tests not run for RealEvaluator")
	@Override
	@Test
	public void testStrings() throws Exception {
	}

	@Ignore("String tests not run for RealEvaluator")
	@Override
	@Test
	public void testStringsFun() throws Exception {
	}

	@Ignore("String tests not run for RealEvaluator")
	@Override
	@Test
	public void testCPStrings() throws Exception {
	}

	@Ignore("String tests not run for RealEvaluator")
	@Override
	@Test
	public void testCaseString() throws Exception {
	}

	@Ignore("Null tests not run for RealEvaluator")
	@Override
	@Test
	public void testCaseNull() throws Exception {
	}

	@Override
	public void testSpecialFunctions() throws Exception {
		testSpecialFunctions(true, true);
	}

	@Ignore("Complex tests not run for RealEvaluator")
	@Override
	@Test
	public void testComplexListAccess() throws Exception {
	}

}
