/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.singularsys.jep.JepException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableFactory;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.parser.Node.HookKey;

class VariableTableTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Nested 
	class Test_copyVariablesFrom {
	
	@Test
	void dont_create_a_new_variable_if_one_exists() throws JepException {
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		
		Variable vt2_v1 = vt2.addVariable("x", 11.0);
		assertNotSame(vt2_v1,vt1_v1,"Variable in second table is different to that in first");
		Variable vt2_v2 = vt2.getVariable("x");
		assertSame(vt2_v1,vt2_v2,"Stable reference to variable");
		
		vt2.copyVariablesFrom(vt1);
		Variable vt2_v3 = vt2.getVariable("x");
		
		assertSame(vt2_v1,vt2_v3,"Should be same variable after copy variable");		
		assertEquals(5.0,vt2_v1.getValue());
	}

	@Test
	void existing_variable_has_copied_value() throws JepException {
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		@SuppressWarnings("unused")
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		@SuppressWarnings("unused")
		Variable vt1_v2 = vt1.addVariable("y", null);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		
		Variable vt2_v1 = vt2.addVariable("x", 11.0);
		Variable vt2_v2 = vt2.addVariable("y", 11.0);
		
		vt2.copyVariablesFrom(vt1);
		
		assertEquals(5.0,vt2_v1.getValue());
		assertNull(vt2_v2.getValue());
	}

	@Test
	void existing_variable_has_same_constant_flag() throws JepException {
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		vt1_v1.setIsConstant(false);
		Variable vt1_v2 = vt1.addVariable("y", 7.0);
		vt1_v2.setIsConstant(true);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		Variable vt2_v1 = vt2.addVariable("x", 9.0);
		vt2_v1.setIsConstant(true);
		Variable vt2_v2 = vt2.addVariable("y", 11.0);
		vt2_v2.setIsConstant(false);

		vt2.copyVariablesFrom(vt1);
		assertFalse(vt2_v1.isConstant());
		assertTrue( vt2_v2.isConstant());
	}

	@Test
	void existing_variable_has_same_valid_value_flag() throws JepException {
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		vt1_v1.setValidValue(true);
		Variable vt1_v2 = vt1.addVariable("y", 7.0);
		vt1_v2.setValidValue(false);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		Variable vt2_v1 = vt2.addVariable("x", 9.0);
		vt2_v1.setIsConstant(false);
		Variable vt2_v2 = vt2.addVariable("y", 11.0);
		vt2_v2.setIsConstant(true);
				
		vt2.copyVariablesFrom(vt1);

		assertTrue( vt2_v1.hasValidValue());
		assertFalse(vt2_v2.hasValidValue());
	}

	@SuppressWarnings("serial")
	@Test
	void existing_variable_has_same_hooks() throws JepException {
		HookKey myhook = new HookKey() {};
		
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		vt1_v1.setHook(myhook, 2.0);
		Variable vt1_v2 = vt1.addVariable("y", 7.0);
		vt1_v2.setValidValue(false);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		Variable vt2_v1 = vt2.addVariable("x", 9.0);
		Variable vt2_v2 = vt2.addVariable("y", 11.0);
		vt2_v2.setHook(myhook, 2.0);
				
		vt2.copyVariablesFrom(vt1);

		assertEquals(2.0, vt2_v1.getHook(myhook));
		assertNull(vt2_v2.getHook(myhook));
	}

	@Test
	void new_variable_has_copied_value() throws JepException {
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		@SuppressWarnings("unused")
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		@SuppressWarnings("unused")
		Variable vt1_v2 = vt1.addVariable("y", null);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		vt2.copyVariablesFrom(vt1);
		Variable vt2_v1 = vt2.getVariable("x");
		Variable vt2_v2 = vt2.getVariable("y");
				
		assertEquals(5.0,vt2_v1.getValue());
		assertNull(vt2_v2.getValue());
	}

	@Test
	void new_variable_has_same_constant_flag() throws JepException {
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		vt1_v1.setIsConstant(false);
		Variable vt1_v2 = vt1.addVariable("y", 7.0);
		vt1_v2.setIsConstant(true);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		vt2.copyVariablesFrom(vt1);
		Variable vt2_v1 = vt2.getVariable("x");
		Variable vt2_v2 = vt2.getVariable("y");

		assertFalse(vt2_v1.isConstant());
		assertTrue( vt2_v2.isConstant());
	}

	@Test
	void new_variable_has_same_valid_value_flag() throws JepException {
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		vt1_v1.setValidValue(true);
		Variable vt1_v2 = vt1.addVariable("y", 7.0);
		vt1_v2.setValidValue(false);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		vt2.copyVariablesFrom(vt1);
		Variable vt2_v1 = vt2.getVariable("x");
		Variable vt2_v2 = vt2.getVariable("y");
				
		assertTrue( vt2_v1.hasValidValue());
		assertFalse(vt2_v2.hasValidValue());
	}

	@SuppressWarnings("serial")
	@Test
	void new_variable_has_same_hooks() throws JepException {
		HookKey myhook = new HookKey() {};
		
		VariableFactory vf = new VariableFactory();
		
		VariableTable vt1 = new VariableTable();
		vt1.setVariableFactory(vf);
		Variable vt1_v1 = vt1.addVariable("x", 5.0);
		vt1_v1.setHook(myhook, 2.0);
		Variable vt1_v2 = vt1.addVariable("y", 7.0);
		vt1_v2.setValidValue(false);
		
		VariableTable vt2 = new VariableTable();
		vt2.setVariableFactory(vf);
		vt2.copyVariablesFrom(vt1);
		Variable vt2_v1 = vt2.getVariable("x");
		Variable vt2_v2 = vt2.getVariable("y");
		
		assertEquals(2.0, vt2_v1.getHook(myhook));
		assertNull(vt2_v2.getHook(myhook));
	}
	
	}
}
