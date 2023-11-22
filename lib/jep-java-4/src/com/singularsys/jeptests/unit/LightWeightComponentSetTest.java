/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.math.MathContext;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.singularsys.jep.EmptyOperatorTable;
import com.singularsys.jep.EmptyOperatorTable.OperatorKey;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.bigdecimal.BigDecFunctionTable;
import com.singularsys.jep.bigdecimal.BigDecOperatorTable;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.functions.Add;
import com.singularsys.jep.functions.If;
import com.singularsys.jep.functions.NullaryFunction;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.misc.CaseInsensitiveFunctionTable;
import com.singularsys.jep.misc.ExtendedOperatorSet;
import com.singularsys.jep.misc.LightWeightComponentSet;
import com.singularsys.jep.misc.MacroFunction;
import com.singularsys.jep.misc.MediumWeightComponentSet;
import com.singularsys.jep.misc.bitwise.BitwiseOperatorTable;
import com.singularsys.jep.misc.javaops.JavaOperatorTable;
import com.singularsys.jep.misc.nullwrapper.NullWrappedFunctionTable;
import com.singularsys.jep.misc.nullwrapper.NullWrappedOperatorTable;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.reals.RealFunctionTable;
import com.singularsys.jep.standard.StandardFunctionTable;
import com.singularsys.jep.standard.StandardOperatorTable2;
import com.singularsys.jep.walkers.ImportationVisitor;
import com.singularsys.jep.walkers.SerializableExpression;

/**
 * See https://ar.trac.cloudforge.com/jep/ticket/175
 */
public class LightWeightComponentSetTest {

	/**
	 * A function implementing JepComponent which requires different instances for each thread.
	 */
	public static class NonThreadSafeFunction extends NullaryFunction implements JepComponent {
		private static final long serialVersionUID = 1L;

		int hitCount=0;
		@Override
		public void init(Jep jep) {
		}

		@Override
		public JepComponent getLightWeightInstance() {
			return new NonThreadSafeFunction();
		}

		@Override
		public Object eval() throws EvaluationException {
			++hitCount;
			return Double.valueOf(hitCount);
		}
		
		@Override
		public String toString() {
			return "nfsf "+this.hashCode();
		}
	}

	public static class NonThreadSafeOpFunction extends UnaryFunction implements JepComponent {
		private static final long serialVersionUID = 1L;

		int hitCount=0;
		@Override
		public void init(Jep jep) {
		}

		@Override
		public JepComponent getLightWeightInstance() {
			return new NonThreadSafeOpFunction();
		}

		@Override
		public Object eval(Object l) throws EvaluationException {
			++hitCount;
			double val = (Double) l;
			return Math.pow(val, hitCount);
		}

		@Override
		public String toString() {
			return "nfsof "+this.hashCode();
		}

	}

	/**
	 * Basic version, all functions should be the same in both instances
	 * @throws Exception
	 */
	@Test
	public void testLightWeightComponentSetBasic() throws Exception {
		Jep baseJep = new Jep();
		baseJep.addFunction("nfsf", new NonThreadSafeFunction());
		OperatorTable2 opTab = (OperatorTable2)baseJep.getOperatorTable();
		OperatorKey opKey = new OperatorKey() {};
		NonThreadSafeOpFunction nfsopPfmc = new NonThreadSafeOpFunction();
		Operator nfsop = new Operator("!!",nfsopPfmc,Operator.SUFFIX+Operator.UNARY);
		opTab.addOperator(opKey, nfsop,opTab.getNot());
		Jep lwj = new  Jep(new LightWeightComponentSet(baseJep));
		
		OperatorTable2 lwOpTab = (OperatorTable2) lwj.getOperatorTable();
		assertEquals(opTab,lwOpTab);
		assertEquals(nfsop,lwOpTab.getOperator(opKey));
		assertEquals(nfsopPfmc,lwOpTab.getOperator(opKey).getPFMC());
		Set<String> ks1 = baseJep.getFunctionTable().keySet();
		Set<String> ks2 = lwj.getFunctionTable().keySet();
		assertEquals(ks1,ks2);
		for(Entry<String, PostfixMathCommandI> ent:baseJep.getFunctionTable().entrySet()) {
			PostfixMathCommandI fOld = ent.getValue();
			PostfixMathCommandI fNew = lwj.getFunctionTable().getFunction(ent.getKey());
			if(fOld instanceof JepComponent) {
				assertEquals(fOld,fNew);				
//				fail("Should be no JepComponent functions here");
			} else {
				assertEquals(fOld,fNew);				
				//System.out.println(ent.getKey() + " " + fOld + " " + fNew);
			}
		}
		
		Node eqn = baseJep.parse("cos(x)");
		Node eqn2 = (new SerializableExpression(eqn)).toNode(lwj);
		baseJep.setVariable("x", 0);
		lwj.setVariable("x", Math.PI);
		
		double v1 = (double) baseJep.evaluate(eqn);
		double v2 = (double) lwj.evaluate(eqn2);
		assertEquals(1.0,v1,1e-9);
		assertEquals(-1.0,v2,1e-9);
	}

	/**
	 * Advanced version using functions which implement JepComponent different instances should be used in each thread.
	 * @throws Exception
	 */
	@Test
	public void testMediumWeightComponentSet() throws Exception {
		Jep baseJep = new Jep();
		baseJep.addFunction("nfsf", new NonThreadSafeFunction());
		baseJep.addFunction("mySec", new MacroFunction(baseJep, "mySec", "x", "1/cos(x)"));

		Jep lwj = new Jep(new MediumWeightComponentSet(baseJep));
		baseJep.reinitializeComponents();
		lwj.reinitializeComponents();
		Set<String> ks1 = baseJep.getFunctionTable().keySet();
		Set<String> ks2 = lwj.getFunctionTable().keySet();
		assertEquals(ks1,ks2);
		for(Entry<String, PostfixMathCommandI> ent:baseJep.getFunctionTable().entrySet()) {
			PostfixMathCommandI fOld = ent.getValue();
			PostfixMathCommandI fNew = lwj.getFunctionTable().getFunction(ent.getKey());
			if(fOld instanceof JepComponent) {
				System.out.println(ent.getKey() + " hashcodes " + fOld.hashCode() + " " + fNew.hashCode());
				assertNotEquals(fOld,fNew);
			} else {
				assertEquals(fOld,fNew);				
				//System.out.println(ent.getKey() + " " + fOld + " " + fNew);
			}
		}
		
		
		Node eqn = baseJep.parse("nfsf()");
		double v1 = baseJep.evaluateD();
		assertEquals(1.0,v1,1e-9);
		double v2 = baseJep.evaluateD();
		assertEquals(2.0,v2,1e-9);
		Node eqn2 = baseJep.parse("nfsf()");
		double v3 = (double) baseJep.evaluate(eqn2);
		assertEquals(3.0,v3,1e-9);
				
		Node eqn3 = (new ImportationVisitor(lwj)).deepCopy(eqn);
		double v4 = (double) lwj.evaluate(eqn3);
		assertEquals(1.0,v4,1e-9);

		Node eqn5 = baseJep.parse("mySec(0)");
		double v5 = (double) baseJep.evaluate(eqn5);
		assertEquals(1.0,v5,1e-9);
		
		Node eqn6 = (new ImportationVisitor(lwj)).deepCopy(eqn5);
		double v6 = (double) lwj.evaluate(eqn6);
		assertEquals(1.0,v6,1e-9);

		Node eqn7 = (new SerializableExpression(eqn)).toNode(lwj);
		double v7 = (double) lwj.evaluate(eqn7);
		assertEquals(2.0,v7,1e-9);

	}

	@Test
	public void testShallowCopy() throws Exception {
		Jep baseJep = new Jep();
		FunctionTable ft1 = baseJep.getFunctionTable();
		baseJep.addFunction("nfsf", new NonThreadSafeFunction());
		FunctionTable ft2 = ft1.shallowCopy();
		
		Set<String> ks1 = ft1.keySet();
		Set<String> ks2 = ft2.keySet();
		assertEquals(ks1,ks2);
		for(Entry<String, PostfixMathCommandI> ent:ft1.entrySet()) {
			PostfixMathCommandI fOld = ent.getValue();
			PostfixMathCommandI fNew = ft2.getFunction(ent.getKey());
			if(fOld instanceof NonThreadSafeFunction) {
				assertNotEquals(fOld,fNew);
			} else {
				assertEquals(fOld,fNew);				
				//System.out.println(ent.getKey() + " " + fOld + " " + fNew);
			}
		}

	}

	class ThreadSafeOperatorTable extends StandardOperatorTable2 {
		private static final long serialVersionUID = 1L;

		public ThreadSafeOperatorTable() {
			super();
		}

		public ThreadSafeOperatorTable(Map<OperatorKey, Operator> map) {
			super(map);
		}
		
		@Override
		public OperatorTableI shallowCopy() {
			Map<OperatorKey, Operator> map = this.threadSafeMapCopy();
			return new ThreadSafeOperatorTable(map);
		}

		@Override
		public JepComponent getLightWeightInstance() {
			return shallowCopy();
		}
	}

	/**
	 * Advanced version using functions which implement JepComponent these should 
	 * @throws Exception
	 */
	@Test
	public void testLightWeightComponentOperators() throws Exception {
		Jep baseJep = new Jep(new StandardConfigurableParser(),new ThreadSafeOperatorTable());
		Operator quest = new Operator("?",new NonThreadSafeOpFunction(),Operator.UNARY+Operator.SUFFIX+Operator.LEFT);
		OperatorKey key = new EmptyOperatorTable.OperatorKey() {};
		((OperatorTable2) baseJep.getOperatorTable()).addOperator(key, quest);
		baseJep.reinitializeComponents();
		
		Jep lwj = new Jep(new LightWeightComponentSet(baseJep));
		Operator oldOp = baseJep.getOperatorTable().getOperatorsBySymbol("?").get(0);
		Operator newOp = lwj.getOperatorTable().getOperatorsBySymbol("?").get(0);
		assertTrue(newOp.getPFMC() instanceof NonThreadSafeOpFunction);
		assertEquals(baseJep.getOperatorTable().getAdd(),lwj.getOperatorTable().getAdd());
		
		assertEquals(oldOp,quest);
		assertNotEquals(oldOp,newOp);
		
		Node eqn  = baseJep.parse("2?");
		Node eqn2 = (new ImportationVisitor(lwj)).deepCopy(eqn);
		Node eqn3 = (new SerializableExpression(eqn)).toNode(lwj);

		assertEquals(oldOp,eqn.getOperator());
		assertEquals(newOp,eqn2.getOperator());
		assertEquals(newOp,eqn2.getOperator());
		
		double v1 = (double) baseJep.evaluate(eqn);
		assertEquals(2.0,v1,1e-9);
		double v2 = (double) baseJep.evaluate(eqn);
		assertEquals(4.0,v2,1e-9);
		double v3 = (double) baseJep.evaluate(eqn);
		assertEquals(8.0,v3,1e-9);
		
		double v4 = (double) lwj.evaluate(eqn2);
		assertEquals(2.0,v4,1e-9);
		double v5 = (double) lwj.evaluate(eqn3);
		assertEquals(4.0,v5,1e-9);
	}

	public void checkFunctionTable(FunctionTable ft) {
		// First check the Shalow Copy works
		NonThreadSafeFunction pfmc = new NonThreadSafeFunction();
		ft.addFunction("ntsf", pfmc);
		FunctionTable shallow = ft.shallowCopy();
		assertEquals(ft.getClass(),shallow.getClass());
		assertNotEquals(ft,shallow);
		PostfixMathCommandI funcopy = shallow.getFunction("ntsf");
		assertNotEquals(pfmc,funcopy);
		
		PostfixMathCommandI ifFun = ft.getFunction("if");
		PostfixMathCommandI ifFun2 = shallow.getFunction("if");
		assertEquals(ifFun,ifFun2);
		
		// Now check lightweight instance vanilla
		FunctionTable lw = (FunctionTable) ft.getLightWeightInstance();
		assertEquals(ft,lw);
		assertEquals(ft.getClass(),lw.getClass());
		PostfixMathCommandI lwfun = lw.getFunction("ntsf");
		assertEquals(pfmc,lwfun);
		PostfixMathCommandI lwif = shallow.getFunction("if");
		assertEquals(ifFun,lwif);
	}

	public  void checkOperatorTable(EmptyOperatorTable ot) {
		// First check the Shalow Copy works
		Class<?> clas = ot.getClass();
		for(Constructor<?> ent:clas.getConstructors()) {
			System.out.println(ent);
		}
		System.out.println();
		Operator quest = new Operator("?",new NonThreadSafeOpFunction(),Operator.UNARY+Operator.SUFFIX+Operator.LEFT);
		OperatorKey key = new EmptyOperatorTable.OperatorKey() {};
		ot.addOperator(key, quest);

		EmptyOperatorTable shallow = (EmptyOperatorTable) ot.shallowCopy();
		assertEquals(ot.getClass(),shallow.getClass());
		assertNotEquals(ot,shallow);
		Operator opcopy = shallow.getOperator(key);
		assertNotEquals(quest,opcopy);
		assertNotEquals(quest.getPFMC(),opcopy.getPFMC());
		
		Operator addOp = ot.getOperator(OperatorTable2.BasicOperators.ADD);
		Operator shallowOp = shallow.getOperator(OperatorTable2.BasicOperators.ADD);
		assertEquals(addOp,shallowOp);
		
		// Now check lightweight instance vanilla
		EmptyOperatorTable lw = (EmptyOperatorTable) ot.getLightWeightInstance();
		assertEquals(ot,lw);
		Operator lwop = lw.getOperator(key);
		assertEquals(quest,lwop);
	}

	@Test
	public void testFunctionTables() {
		checkFunctionTable(new StandardFunctionTable());
		checkFunctionTable(new BigDecFunctionTable(MathContext.DECIMAL32));
		checkFunctionTable(new CaseInsensitiveFunctionTable());
		checkFunctionTable(new NullWrappedFunctionTable(new StandardFunctionTable()));
		checkFunctionTable(new RealFunctionTable());
		FunctionTable ft = new FunctionTable();
		ft.addFunction("if", new If());
		checkFunctionTable(ft);
	}
	
	@Test
	public void testOperatorTables() {
		checkOperatorTable(new StandardOperatorTable2());
		checkOperatorTable(new BigDecOperatorTable(MathContext.DECIMAL32));
		checkOperatorTable(new NullWrappedOperatorTable(new StandardOperatorTable2(),true));
		checkOperatorTable(new BitwiseOperatorTable("**", "^"));
		checkOperatorTable(new JavaOperatorTable());
		checkOperatorTable(new ExtendedOperatorSet());
		
		EmptyOperatorTable ot = new EmptyOperatorTable();
		ot.addOperator(OperatorTable2.BasicOperators.ADD, 
				new Operator("&&",new Add(), //$NON-NLS-1$
						Operator.BINARY+Operator.LEFT+Operator.COMMUTATIVE+Operator.ASSOCIATIVE+Operator.USE_BINDING_FOR_PRINT));
		checkOperatorTable(ot);
		
		OperatorTable2 ot2 = new OperatorTable2();
		ot2.getAdd().setPFMC(new Add());
		checkOperatorTable(ot2);

	}
	
	static class CompWithNullInstance implements JepComponent {
		private static final long serialVersionUID = 1L;

		@Override
		public void init(Jep jep) {
		}

		@Override
		public JepComponent getLightWeightInstance() {
			return null;
		}
		
	}

	static class CompWithThisInstance implements JepComponent {
		private static final long serialVersionUID = 1L;

		@Override
		public void init(Jep jep) {
		}

		@Override
		public JepComponent getLightWeightInstance() {
			return this;
		}
		
	}

	static class CompWithNewInstance implements JepComponent {
		private static final long serialVersionUID = 1L;

		@Override
		public void init(Jep jep) {
		}

		@Override
		public JepComponent getLightWeightInstance() {
			return new CompWithNewInstance();
		}
		
	}
	@Test
	public void uses_lightweight_instances_of_additional_components() {
		Jep baseJep = new Jep();
		baseJep.setComponent(new CompWithNewInstance());
		baseJep.setComponent(new CompWithNullInstance());
		baseJep.setComponent(new CompWithThisInstance());
		
		assertNotNull(baseJep.getAdditionalComponent(CompWithNewInstance.class));
		assertNotNull(baseJep.getAdditionalComponent(CompWithNullInstance.class));
		assertNotNull(baseJep.getAdditionalComponent(CompWithThisInstance.class));
		
		LightWeightComponentSet lwcs = new LightWeightComponentSet(baseJep);
		Jep lwj = new Jep(lwcs);
		assertNotNull(lwj.getAdditionalComponent(CompWithNewInstance.class));
		assertNull(lwj.getAdditionalComponent(CompWithNullInstance.class));
		assertNotNull(lwj.getAdditionalComponent(CompWithThisInstance.class));
		
		assertEquals(2,lwcs.getAuxComponents().length);

	}
}
