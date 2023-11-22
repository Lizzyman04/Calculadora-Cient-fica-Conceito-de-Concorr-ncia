/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.walkers;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.walkers.SubstitutionVisitor;

public class SubstitutionVisitorTest {

	Jep jep;
	SubstitutionVisitor sv;
	
	@Before
	public void setUp() throws Exception {
		jep = new Jep();
		sv = new SubstitutionVisitor(jep);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSubstituteNodeStringNode() throws ParseException {
		Node base = jep.parse("x^2+y^2");
		Node sub = jep.parse("z+1");
		Node res = sv.substitute(base, "x", sub);
		assertEquals("(z+1.0)^2.0+y^2.0",jep.toString(res));
	}

	@Test
	public void testSubstituteNodeNode() throws ParseException {
		Node base = jep.parse("x^2+y^2");
		Node sub = jep.parse("x=z+1");
		Node res = sv.substitute(base, sub);
		assertEquals("(z+1.0)^2.0+y^2.0",jep.toString(res));
	}

	@Test
	public void testSubstituteNodeNodeArray() throws ParseException {
		Node base = jep.parse("x^2+y^2");
		Node sub1 = jep.parse("x=y+2");
		Node sub2 = jep.parse("y=z+3");
		Node res = sv.substitute(base, new Node[] {sub1,sub2});
		assertEquals("(y+2.0)^2.0+(z+3.0)^2.0",jep.toString(res));
	}

	@Test
	public void testSubstitute_in_loop() throws ParseException {
		{
		Node base = jep.parse("x^2+y^2");
		Node sub1 = jep.parse("x=y+2");
		Node sub2 = jep.parse("y=z+3");
		Node subs[] = new Node[] {sub1,sub2};
		for(Node sub:subs) {
			base = sv.substitute(base, sub);
		}
		assertEquals("(z+3.0+2.0)^2.0+(z+3.0)^2.0",jep.toString(base));
		}
		// Note order matters
		{
		Node base = jep.parse("x^2+y^2");
		Node sub1 = jep.parse("x=y+2");
		Node sub2 = jep.parse("y=z+3");
		Node subs[] = new Node[] {sub2,sub1};
		for(Node sub:subs) {
			base = sv.substitute(base, sub);
		}
		assertEquals("(y+2.0)^2.0+(z+3.0)^2.0",jep.toString(base));
		}
		
	}

	@Test
	public void testSubstituteNodeStringArrayNodeArray() throws ParseException {
		Node base = jep.parse("x^2+y^2");
		Node sub1 = jep.parse("y+2");
		Node sub2 = jep.parse("z+3");
		Node res = sv.substitute(base, new String[] {"x","y"},new Node[] {sub1,sub2});
		assertEquals("(y+2.0)^2.0+(z+3.0)^2.0",jep.toString(res));
	}

	@Test
	public void testSubstituteNodeStringArrayObjectArray() throws ParseException {
		Node base = jep.parse("x^2+y^2");
		Node res = sv.substitute(base, new String[] {"x","y"},new Object[] {2.0,3.0});
		assertEquals("2.0^2.0+3.0^2.0",jep.toString(res));
	}

	@Test
	public void testSubstituteNodeStringObject() throws ParseException {
		Node base = jep.parse("x^2+y^2");
		Node res = sv.substitute(base, "x",2.0);
		assertEquals("2.0^2.0+y^2.0",jep.toString(res));
	}

	@Test
	public void testSubstituteNodeStringArrayStringArray() throws ParseException {
		Node base = jep.parse("x^2+y^2");
		Node res = sv.substitute(base, new String[]{"x","y"},new String[] {"z","w"});
		assertEquals("z^2.0+w^2.0",jep.toString(res));
	}

}
