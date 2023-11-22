/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.EmptyOperatorTable.OperatorKey;
import com.singularsys.jep.Jep;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.configurableparser.ConfigurableParser;
import com.singularsys.jep.configurableparser.matchers.BracketedSequenceGrammarMatcher;
import com.singularsys.jep.configurableparser.matchers.FunctionSequenceGrammarMatcher;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.standard.StandardOperatorTable2;

/**
 * Tests for the configurable parser.
 */
public class CPSeqOpTest extends CPTest {

    @Override
    @Before
    public void setUp() {
		OperatorTable2 optab = new StandardOperatorTable2();
		Operator seqOp = new Operator(",", null, Operator.BINARY+Operator.RIGHT);
		optab.appendOperator(new OperatorKey() {}, seqOp, optab.getAssign());

		ConfigurableParser cp = new ConfigurableParser();
        cp.addHashComments();
        cp.addSlashComments();
        cp.addSingleQuoteStrings();
        cp.addDoubleQuoteStrings();
        cp.addWhiteSpace();
//        cp.addTokenMatcher(
//        new NumberTokenMatcher("((\\d+\\.\\d+)|(\\d+\\.(?!\\.))|(\\.\\d+)|(\\d+))(?:[eE][+-]?\\d+)?")); //$NON-NLS-1$
        cp.addExponentNumbers();
        cp.addSymbols("(",")","[","]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        cp.setImplicitMultiplicationSymbols("(","["); //$NON-NLS-1$ //$NON-NLS-2$
        cp.addOperatorTokenMatcher();
        cp.addIdentifiers();
        cp.addSemiColonTerminator();
        cp.addWhiteSpaceCommentFilter();
        cp.addBracketMatcher("(",")"); //$NON-NLS-1$ //$NON-NLS-2$
        cp.addGrammarMatcher(new FunctionSequenceGrammarMatcher(
        		cp.getSymbolToken("("),
        		cp.getSymbolToken(")"),
        		seqOp));
//        cp.addFunctionMatcher("(",")",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //cp.addListMatcher("[","]",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        cp.addGrammarMatcher(new BracketedSequenceGrammarMatcher(
        		cp.getSymbolToken("["),
        		cp.getSymbolToken("]"),
        		seqOp
        		)); 
        cp.addArrayAccessMatcher("[","]"); //$NON-NLS-1$ //$NON-NLS-2$

		jep = new Jep(optab,cp);
    }

    @Test
    public void testListAndFun() throws ParseException {
	    Node n1 = jep.parse("rand()");
	    jep.println(n1);
	    assertEquals("rand()",jep.toString(n1));
	    Node n2 = jep.parse("floor(3.3)");
	    jep.println(n2);
	    assertEquals("floor(3.3)",jep.toString(n2));
	    Node n3 = jep.parse("atan2(1.0,1.0)");
	    jep.println(n3);
	    assertEquals("atan2(1.0,1.0)",jep.toString(n3));
	    Node n3a = jep.parse("if(1.0,2.0,3.0)");
	    jep.println(n3a);
	    assertEquals("if(1.0,2.0,3.0)",jep.toString(n3a));
	    Node n4 = jep.parse("[]");
	    jep.println(n4);
	    assertEquals("[]",jep.toString(n4));
	    Node n5 = jep.parse("[1]");
	    jep.println(n5);
	    assertEquals("[1.0]",jep.toString(n5));
	    Node n6 = jep.parse("[1,2]");
	    jep.println(n6);
	    assertEquals("[1.0,2.0]",jep.toString(n6));
	    Node n7 = jep.parse("[1,2,3]");
	    jep.println(n7);
	    assertEquals("[1.0,2.0,3.0]",jep.toString(n7));
	    Node n8 = jep.parse("if(1.0,[2,3],[4,5,6])");
	    jep.println(n8);
	    assertEquals("if(1.0,[2.0,3.0],[4.0,5.0,6.0])",jep.toString(n8));
	    Node n9 = jep.parse("[floor(3.3),[2,3],(4+5)]");
	    jep.println(n9);
	    assertEquals("[floor(3.3),[2.0,3.0],4.0+5.0]",jep.toString(n9));
    }
    
}
