/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.IllegalParameterException;
import com.singularsys.jep.functions.NaryBinaryFunction;
import com.singularsys.jep.functions.NullaryFunction;
import com.singularsys.jep.functions.UnaryFunction;

class FunctionFromLambdaTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testBinaryInstanceOf() throws EvaluationException, JepException {
		BinaryFunction sum =  BinaryFunction.instanceOf((x,y) -> ((Number) x).doubleValue() + ((Number) y).doubleValue());
		
		Object res = sum.eval(3, 4);
		assertEquals(7.0,res);
		
		BinaryFunction prod =  BinaryFunction.instanceOf(Integer.class, (x,y) -> x * y);
		
		Object res2 = prod.eval(3, 4);
		assertEquals(12,res2);
		
		try {
			prod.eval(3.0, 4.0);
			fail("Should have thrown IllegalParameterException");
		} catch(IllegalParameterException e) {
			System.out.println("Expected exception caught "+e);
			assertEquals(0,e.getArgumentNumber());
		}

		try {
			prod.eval(3, 4.0);
			fail("Should have thrown IllegalParameterException");
		} catch(IllegalParameterException e) {
			System.out.println("Expected exception caught "+e);
			assertEquals(1,e.getArgumentNumber());
		}

		Jep jep = new Jep();
		jep.addFunction("prod", prod);
		jep.parse("prod(x,y)");
		jep.setVariable("x", Integer.valueOf(3));
		jep.setVariable("y", Integer.valueOf(4));
		assertEquals(12,jep.evaluate());
		
		BinaryFunction diff =  BinaryFunction.instanceOf(Number.class, (x,y) -> x.doubleValue() -  y.doubleValue());
		assertEquals(1.5,diff.eval(5, 3.5));
		
		BinaryFunction hypot =  BinaryFunction.instanceOf(Double.class,Math::hypot); 
		assertEquals(5.0,hypot.eval(3.0, 4.0));
	}

	@Test
	void testUseWithJep() throws JepException {
		Jep jep = new Jep();
		jep.getOperatorTable().getAdd().setPFMC(BinaryFunction.instanceOf(Integer.class, (x,y)->x+y));
		jep.getOperatorTable().getSubtract().setPFMC(BinaryFunction.instanceOf(Integer.class, (x,y)->x-y));
		jep.getOperatorTable().getUMinus().setPFMC(UnaryFunction.instanceOf(Integer.class, x-> -x));
		jep.getOperatorTable().getMultiply().setPFMC(BinaryFunction.instanceOf(Integer.class, (x,y)->x*y));
		jep.getOperatorTable().getDivide().setPFMC(BinaryFunction.instanceOf(Integer.class, (x,y)->x/y));
		jep.addFunction("sqrt", UnaryFunction.instanceOf(Integer.class, x -> (int) Math.sqrt(x)));
		jep.parse("sqrt(x*x+y*y)");
		jep.setVariable("x", 3);
		jep.setVariable("y", 4);
		Object res = jep.evaluate();
		assertEquals(5,res);
		
	}
	@Test
	void testUnaryInstanceOf() throws EvaluationException, JepException {
		UnaryFunction recip = UnaryFunction.instanceOf(x -> 1.0 / ((Number) x).doubleValue());
		UnaryFunction neg = UnaryFunction.instanceOf(Integer.class, x -> -x );
		
		assertEquals(0.5,recip.eval(2.0));
		assertEquals(-5,neg.eval(5));
		
		UnaryFunction lower = UnaryFunction.instanceOf(String.class, String::toLowerCase);
		assertEquals("upper",lower.eval("UPPER"));
	}

	@Test
	void testNullaryInstanceOf() throws EvaluationException, JepException {
		NullaryFunction five = NullaryFunction.instanceOf(() -> 5.0);
		assertEquals(5.0,five.eval());
//		ThreadLocalRandom tlr = ThreadLocalRandom.current();
		NullaryFunction rnd = NullaryFunction.instanceOf(ThreadLocalRandom.current()::nextDouble);
		Double val = (Double) rnd.eval();
		assertTrue(val>=0.0 && val <= 1.0);
		Double val2 = (Double) rnd.eval();
		assertTrue(val2>=0.0 && val2 <= 1.0);
		assertNotEquals(val,val2);
	}

	@Test
	void testNaryBinaryInstanceOf() throws EvaluationException, JepException {
		NaryBinaryFunction sum =  NaryBinaryFunction.instanceOf((x,y) -> ((Number) x).doubleValue() + ((Number) y).doubleValue());
		
		Object res = sum.eval(3, 4);
		assertEquals(7.0,res);

		assertEquals(10.0,sum.eval(new Object[] {1.0,2,3,4}));
		
		NaryBinaryFunction prod =  NaryBinaryFunction.instanceOf(Integer.class, (x,y) -> x * y);
		
		Object res2 = prod.eval(3, 4);
		assertEquals(12,res2);

		assertEquals(24,prod.eval(new Object[] {1,2,3,4}));
		
		try {
			assertEquals(24,prod.eval(new Object[] {1,2,3,4.0}));			
		} catch(IllegalParameterException e) {
			System.out.println("Expected exception caught "+e);
			assertEquals(3,e.getArgumentNumber(),"argument number");
		}
		
		Stack<Object> stack = new Stack<>();
		stack.push(1);
		stack.push(2);
		stack.push(3);
		stack.push(4);

		prod.setCurNumberOfParameters(4);
		prod.run(stack);
		Object val = stack.pop();
		assertEquals(24,val);

		try {
			stack.push(1);
			stack.push(2);
			stack.push(3.0);
			stack.push(4);
			prod.run(stack);
			val = stack.pop();
			assertEquals(24,val);
		} catch(IllegalParameterException e) {
			System.out.println("Expected exception caught "+e);
			assertEquals(2,e.getArgumentNumber(),"argument number");
		}
	}
	
	@Test
	void testNaryBinaryInstanceOfWithMethodReference() throws EvaluationException, JepException {

		UnaryFunction cubrt = UnaryFunction.instanceOf(Double.class,Math::cbrt);
		assertEquals(2.0,cubrt.eval(8.0));
	}

}
