/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;

import static org.junit.Assert.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableFactory;
import com.singularsys.jep.parser.Node.HookKey;

public class VariableFactoryTest {

	@BeforeEach
	public void setUp() throws Exception {
	}

	class MyVariable extends Variable {
		private static final long serialVersionUID = 1L;

		protected MyVariable(String name) {
			super(name);
		}

		public MyVariable(String name, Object value) {
			super(name, value);
		}

		public MyVariable(Variable var) {
			super(var);
		}
	}

	class MyVariableFactory extends VariableFactory {
		private static final long serialVersionUID = 1L;

		@Override
		public Variable createVariable(String name, Object value) {
			return new MyVariable(name, value);
		}
	}
			
	@Nested
	public class Test_copyVariable {
	@Test
	public void copied_variable_should_create_varibles_of_factories_type() {
		VariableFactory basevf = new VariableFactory();
		Variable basevar = basevf.createVariable("x");
		
		VariableFactory myvf = new MyVariableFactory();
		Variable myvar = myvf.copyVariable(basevar);

		assertTrue(myvar instanceof MyVariable);

		Variable basevar2 = basevf.createVariable("y",5.0);
		Variable myvar2 = myvf.copyVariable(basevar2);

		assertTrue(myvar2 instanceof MyVariable);

	}

	@Test
	public void copied_variable_should_have_same_name_and_value() {
		VariableFactory basevf = new VariableFactory();
		VariableFactory myvf = new MyVariableFactory();

		Variable basevar = basevf.createVariable("x");
		Variable myvar = myvf.copyVariable(basevar);

		assertEquals(basevar.getName(),myvar.getName());
		assertEquals(basevar.getValue(),myvar.getValue());
		
		Variable basevar2 = basevf.createVariable("y",5.0);
		Variable myvar2 = myvf.copyVariable(basevar2);
		
		assertEquals(basevar2.getName(),myvar2.getName());
		assertEquals(basevar2.getValue(),myvar2.getValue());
	}

	@Test
	public void copied_variable_with_null_values_should_use_factories_default_value() {
		VariableFactory basevf = new VariableFactory();
		VariableFactory myvf = new MyVariableFactory();
		myvf.setDefaultValue(5.0);

		Variable basevar = basevf.createVariable("x");
		assertNull(basevar.getValue());
		Variable myvar = myvf.copyVariable(basevar);

		assertEquals(basevar.getName(),myvar.getName());
		assertEquals(myvf.getDefaultValue(),myvar.getValue());		
	}

	@Test
	public void copied_variable_should_copy_hooks() {
		VariableFactory basevf = new VariableFactory();
		VariableFactory myvf = new MyVariableFactory();
		myvf.setDefaultValue(5.0);
		
		@SuppressWarnings("serial")
		HookKey myhook = new HookKey() {};

		Variable basevar = basevf.createVariable("x");
		basevar.setHook(myhook, 7);
		
		Variable myvar = myvf.copyVariable(basevar);

		assertEquals(7,myvar.getHook(myhook));
	}

	@Test
	public void copied_variable_should_copy_constant_flag() {
		VariableFactory basevf = new VariableFactory();
		VariableFactory myvf = new MyVariableFactory();
		myvf.setDefaultValue(5.0);

		Variable basevar = basevf.createVariable("x");
		basevar.setIsConstant(true);
		Variable basevar2 = basevf.createVariable("y",7);
		
		Variable myvar = myvf.copyVariable(basevar);
		Variable myvar2 = myvf.copyVariable(basevar2);
		assertTrue(myvar.isConstant());
		assertFalse(myvar2.isConstant());
	}

	@Test
	public void copied_variable_should_copy_validvalue_flag() {
		VariableFactory basevf = new VariableFactory();
		VariableFactory myvf = new MyVariableFactory();
		
		Variable basevar = basevf.createVariable("x",5);
		basevar.setValidValue(false);
		Variable basevar2 = basevf.createVariable("y",7);
		basevar2.setValidValue(true);
		
		Variable myvar = myvf.copyVariable(basevar);
		Variable myvar2 = myvf.copyVariable(basevar2);

		assertFalse(myvar.hasValidValue());
		assertTrue(myvar2.hasValidValue());
	}

	}
}
