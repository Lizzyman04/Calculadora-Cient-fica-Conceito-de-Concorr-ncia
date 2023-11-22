/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.walkers;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.parser.Node.HookKey;
import com.singularsys.jep.walkers.HookRemover;;

public class HookRemoverTest {
	Jep jep;
	@Before
	public void setUp() throws Exception {
		jep = new Jep();
	}

	@SuppressWarnings("serial")
	@Test
	public void testRemoveHook() throws JepException {
		HookKey myHook = new HookKey() {};
		Node node = jep.parse("x*(y-1)");

		node.jjtGetChild(0).setHook(myHook, "x");
		node.jjtGetChild(1).jjtGetChild(0).setHook(myHook, "y");

		HookRemover hr = new HookRemover(myHook);
		hr.removeHooks(node);
		assertEquals(0,node.jjtGetChild(0).hookKeys().size());
		assertEquals(0,node.jjtGetChild(1).jjtGetChild(0).hookKeys().size());
	}

	@SuppressWarnings("serial")
	@Test
	public void testRemoveHookwithValue() throws JepException {
		HookKey myHook = new HookKey() {};
		Node node = jep.parse("x*(y-1)");

		node.jjtGetChild(0).setHook(myHook, "x");
		node.jjtGetChild(1).jjtGetChild(0).setHook(myHook, "y");

		HookRemover hr = new HookRemover(myHook,"x");
		hr.removeHooks(node);
		assertEquals(0,node.jjtGetChild(0).hookKeys().size());
		assertEquals(1,node.jjtGetChild(1).jjtGetChild(0).hookKeys().size());
	}

	@SuppressWarnings("serial")
	@Test
	public void testRemoveAllHook() throws JepException {
		HookKey myHook = new HookKey() {};
		Node node = jep.parse("x*(y-1)");

		node.jjtGetChild(0).setHook(myHook, "x");
		node.jjtGetChild(1).jjtGetChild(0).setHook(myHook, "y");

		HookRemover hr = new HookRemover();
		hr.removeHooks(node);
		assertEquals(0,node.jjtGetChild(0).hookKeys().size());
		assertEquals(0,node.jjtGetChild(1).jjtGetChild(0).hookKeys().size());
	}

}
