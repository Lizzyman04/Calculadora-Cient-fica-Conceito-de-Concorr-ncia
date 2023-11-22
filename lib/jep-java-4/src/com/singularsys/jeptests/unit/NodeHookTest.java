/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.parser.Node.HookKey;

public class NodeHookTest {

	NodeFactory vf = new NodeFactory();
	@SuppressWarnings("serial")
	HookKey key1 = new HookKey() {};
	@SuppressWarnings("serial")
	HookKey key2 = new HookKey() {};
	@SuppressWarnings("serial")
	HookKey key3 = new HookKey() {};
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void set_of_hooks_is_initially_empty() throws ParseException {
		Node var = vf.buildConstantNode("x");
		Collection<HookKey> keys = var.hookKeys();
		assertTrue(keys.isEmpty());
	}

	@Test
	public void getting_an_unset_HookKey_returns_null() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object val1 = var.getHook(key1);
		assertNull(val1);
	}
	
	@Test
	public void setting_and_getting_a_HookKey_returns_same_value() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data = Integer.valueOf(5);
		var.setHook(key1, data);
		Object val1 = var.getHook(key1);
		assertEquals(data,val1);
		Collection<HookKey> keys = var.hookKeys();
		assertEquals(1,keys.size());
	}

	@Test
	public void changing_a_hook_returns_same_value() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data1 = Integer.valueOf(5);
		Object data2 = Integer.valueOf(7);
		var.setHook(key1, data1);
		Object val1 = var.getHook(key1);
		assertEquals(data1,val1);
		
		var.setHook(key1, data2);
		Object val2 = var.getHook(key1);
		assertEquals(data2,val2);
	}

	@Test
	public void setting_and_getting_two_hooks_returns_same_value() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data1 = Integer.valueOf(5);
		Object data2 = Integer.valueOf(7);
		var.setHook(key1, data1);
		var.setHook(key2, data2);
		
		Object val1 = var.getHook(key1);
		Object val2 = var.getHook(key2);
		assertEquals(data1,val1);
		assertEquals(data2,val2);

		Collection<HookKey> keys = var.hookKeys();
		assertEquals(2,keys.size());
	}

	@Test
	public void setting__and_removing_a_hook_returns_null() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data = Integer.valueOf(5);
		var.setHook(key1, data);
		Object val1 = var.getHook(key1);
		assertEquals(data,val1);
		var.removeHook(key1);
		Object val2 = var.getHook(key1);
		assertNull(val2);

		Collection<HookKey> keys = var.hookKeys();
		assertTrue(keys.isEmpty());
	}

	@Test
	public void setting_and_removing_and_setting_same_hook() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data = Integer.valueOf(5);
		Object data2 = Integer.valueOf(7);
		
		var.setHook(key1, data);
		Object val1 = var.getHook(key1);
		assertEquals(data,val1);
		
		var.removeHook(key1);
		
		var.setHook(key1, data2);
		Object val2 = var.getHook(key1);
		assertEquals(data2,val2);
	}

	@Test
	public void setting_and_removing_and_setting_different_hook() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data = Integer.valueOf(5);
		Object data2 = Integer.valueOf(7);
		
		var.setHook(key1, data);
		Object val1 = var.getHook(key1);
		assertEquals(data,val1);
		
		var.removeHook(key1);
		Object val2 = var.getHook(key1);
		assertNull(val2);
		
		var.setHook(key2, data2);
		Object val3 = var.getHook(key2);
		assertEquals(data2,val3);

		Collection<HookKey> keys = var.hookKeys();
		assertEquals(1,keys.size());
	}

	@Test
	public void setting_two_hooks_and_removing_one() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data = Integer.valueOf(5);
		Object data2 = Integer.valueOf(7);
		
		var.setHook(key1, data);
		Object val1 = var.getHook(key1);
		assertEquals(data,val1);
				
		var.setHook(key2, data2);

		var.removeHook(key1);
		Object val2 = var.getHook(key1);
		assertNull(val2);

		Object val3 = var.getHook(key2);
		assertEquals(data2,val3);
	
		Collection<HookKey> keys = var.hookKeys();
		assertEquals(1,keys.size());
	}

	@Test
	public void setting_the_hooks_and_removing__middle_one() throws ParseException  {
		Node var = vf.buildConstantNode("x");
		Object data = Integer.valueOf(5);
		Object data2 = Integer.valueOf(7);
		Object data3 = Integer.valueOf(11);
		
		var.setHook(key1, data);
				
		var.setHook(key2, data2);
		var.setHook(key3, data3);

		var.removeHook(key2);

		Object val1 = var.getHook(key1);
		assertEquals(data,val1);
		Object val2 = var.getHook(key2);
		assertNull(val2);
		Object val3 = var.getHook(key3);
		assertEquals(data3,val3);
	
		Collection<HookKey> keys = var.hookKeys();
		assertEquals(2,keys.size());
	}

	
}
