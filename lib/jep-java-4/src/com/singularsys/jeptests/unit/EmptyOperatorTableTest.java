/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.singularsys.jep.EmptyOperatorTable;
import com.singularsys.jep.EmptyOperatorTable.OperatorKey;
import com.singularsys.jep.Operator;
class EmptyOperatorTableTest {

	private static final Operator OP_A = new Operator("A", null, 0);
	private static final Operator OP_B = new Operator("B", null, 0);
	private static final Operator OP_C = new Operator("C", null, 0);
	private static final Operator OP_D = new Operator("D", null, 0);
	private static final Operator OP_E = new Operator("E", null, 0);
	private static final Operator OP_F = new Operator("F", null, 0);
	private static final Operator OP_G = new Operator("G", null, 0);

	EmptyOperatorTable eot;
	
    public enum MyKeys implements EmptyOperatorTable.OperatorKey {
    	A, B, C, D, E, F, G
    }

	@BeforeEach
	void setUp() throws Exception {
		eot = new EmptyOperatorTable();
		eot.addOperator(MyKeys.A, OP_A);
		eot.addOperator(MyKeys.B, OP_B);
		eot.addOperator(MyKeys.C, OP_C);
		eot.addOperator(MyKeys.D, OP_D);
		eot.addOperator(MyKeys.E, OP_E);
		eot.setPrecedenceTable(new OperatorKey[][] {
			{MyKeys.A},
			{MyKeys.B},
			{MyKeys.C,MyKeys.D},
			{MyKeys.E}
		});
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testAddOperator_Key_Operator_Operator() {
		eot.addOperator(MyKeys.F, OP_F, OP_B);
		Operator op = eot.getOperator(MyKeys.F);
		assertEquals(OP_F,op);
		assertEquals(OP_B.getPrecedence(), OP_F.getPrecedence());
		assertTrue(OP_A.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_C.getPrecedence() > OP_F.getPrecedence());
		
		
	}

	@Test
	void testAddOperator_Operator_Operator() {
		eot.addOperator(OP_F, OP_B);
		assertEquals(OP_B.getPrecedence(), OP_F.getPrecedence());
		assertTrue(OP_A.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_C.getPrecedence() > OP_F.getPrecedence());
		
		OperatorKey key = eot.getKey(OP_F);
		assertNotEquals(MyKeys.A,key);
		assertNotEquals(MyKeys.B,key);
		assertNotEquals(MyKeys.C,key);
		assertNotEquals(MyKeys.D,key);
		assertNotEquals(MyKeys.E,key);

		eot.addOperator(OP_G, OP_C);
		OperatorKey keyG = eot.getKey(OP_G);
		assertNotEquals(key,keyG);
		Operator op = eot.getOperator(keyG);
		assertSame(OP_G,op);
	}

	@Test
	void testInsertOperator_Key_Operator_Operator() {
		eot.insertOperator(MyKeys.F, OP_F, OP_B);
		Operator op = eot.getOperator(MyKeys.F);
		assertEquals(OP_F,op);
		assertTrue(OP_F.getPrecedence() < OP_B.getPrecedence());
		assertTrue(OP_A.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_C.getPrecedence() > OP_F.getPrecedence());
	}

	@Test
	void testInsertOperator_Operator_Operator() {
		eot.insertOperator(OP_F, OP_B);
		assertTrue(OP_F.getPrecedence() < OP_B.getPrecedence());
		assertTrue(OP_A.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_C.getPrecedence() > OP_F.getPrecedence());
	}

	@Test
	void testAppendOperator_Key_Operator_Operator() {
		eot.appendOperator(MyKeys.F, OP_F, OP_B);
		Operator op = eot.getOperator(MyKeys.F);
		assertEquals(OP_F,op);
		assertTrue(OP_B.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_A.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_C.getPrecedence() > OP_F.getPrecedence());
	}

	@Test
	void testAppendOperator_Operator_Operator() {
		eot.appendOperator(OP_F, OP_B);
		assertTrue(OP_B.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_A.getPrecedence() < OP_F.getPrecedence());
		assertTrue(OP_C.getPrecedence() > OP_F.getPrecedence());
	}

	@Disabled
	@Test
	void testGetKey() {
		fail("Not yet implemented");
	}

	@Disabled
	@Test
	void testRemoveOperator_Operator() {
		fail("Not yet implemented");
	}

	@Disabled
	@Test
	void testRemoveOperator_Key() {
		fail("Not yet implemented");
	}

	@Disabled
	@Test
	void testReplaceOperator_Operator_Operator() {
		fail("Not yet implemented");
	}

	@Disabled
	@Test
	void testReplaceOperator_Key_Operator() {
		fail("Not yet implemented");
	}

}
