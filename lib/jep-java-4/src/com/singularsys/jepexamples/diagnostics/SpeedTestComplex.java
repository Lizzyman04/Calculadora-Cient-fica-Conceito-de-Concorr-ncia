/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /* @author rich
 * Created on 26-Feb-2004
 */

package com.singularsys.jepexamples.diagnostics;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.Variable;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeEvaluator;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.reals.RealEvaluator;
import com.singularsys.jep.standard.Complex;
import com.singularsys.jep.standard.FastEvaluator;
import com.singularsys.jep.standard.StandardEvaluator;
import com.singularsys.jep.standard.UncheckedEvaluator;
import com.singularsys.jep.walkers.PostfixEvaluator;
import com.singularsys.jep.walkers.TreeAnalyzer;

/**
 * Compares the speed of evaluation between different evaluation schemes.
 * The standard class compares BigDecimal, Jep (with default Fast evaluator), 
 * the old StandardEvaluator, and RealEvaluator.
 * <p>
 * If you have some nice complicated examples, I'd love to
 * hear about them to see if we can tune things up. - rich
 */
public class SpeedTestComplex {
	public int num_itts = 100000; // for normal use
	public int num_warmup = 100000; // number of iterations for warm up 
	//	static int num_itts = 100;	  // for use with profiler
	public int num_vals = 1000; // number of random numbers selected
	public int nDeriv = 20;
	static MathContext MC = MathContext.DECIMAL64;

	long seed; // seed for random number generator
	Random generator;


	protected final List<EvaluationConfig> configs = new ArrayList<>();
	protected Outputter outputter;
	protected long[] totalTimes;

	/**
	 * @param outputter
	 */
	public SpeedTestComplex(Outputter outputter) {
		this.outputter = outputter;
		seed = System.currentTimeMillis();
	}

	public void init() {
		generator = new Random(seed);
		totalTimes = new long[configs.size()];
		outputter.printHeader(this);
	}

	public void fini() {
		outputter.printFooter(this, totalTimes);
	}

	/*** Different output methods ****************/

	public static class Outputter {
		Jep globalJep = new Jep();
		{
			commonSetup(globalJep);
		}
		public void printHeader(SpeedTestComplex st) {
			out.println("Performing "+st.num_itts+" iterations, "+st.num_warmup+" warm up iterations.");
			for(EvaluationConfig c :st.configs) {
				out.println(c.name + "\t" + c.description());
			}
		}

		/**
		 * @param st  
		 * @param varNames 
		 */
		public void printOutputHeader(SpeedTestComplex st,String eqn, String varNames[]) {
			out.println("\nTesting speed for \"" + eqn + "\"");
			try {
				Node node = globalJep.parse(eqn);
				TreeAnalyzer ta = new TreeAnalyzer(node);
				out.println(ta.summary());
			} catch (Exception e) {
				out.println(e.getMessage());
			}

		}

		/**
		 * @param eqn 
		 * @param varNames  
		 */
		public void printOutputTimes(SpeedTestComplex st,String eqn, String varNames[],long[] times) {
			for(int i=0;i<st.configs.size();++i) {
				out.println(st.configs.get(i).name+"\t"+times[i]);
			}
		}

		public void printOutputHeader(SpeedTestComplex st,String eqns[], String varNames[]) {
			out.print("\nTesting speed for ");
			for(int i=0;i<eqns.length;++i) {
				if(i>0) System.out.print(", ");
				out.print("\""+eqns[i] + "\"");
			}
			out.println(".");
			try {
				TreeAnalyzer ta = new TreeAnalyzer();
				for(int i=0;i<eqns.length;++i) {
					Node node = globalJep.parse(eqns[i]);
					ta.analyze(node);
				}
				out.println(ta.summary());
			} catch (Exception e) {
				out.println(e.getMessage());
			}
		}

		/**
		 * @param eqns  
		 * @param varNames 
		 */
		public void printOutputTimes(SpeedTestComplex st,String eqns[], String varNames[],long[] times) {

			for(int i=0;i<st.configs.size();++i) {
				out.println(st.configs.get(i).name+"\t"+times[i]);
			}
		}

		public void printFooter(SpeedTestComplex st,long[] totalTimes) {
			out.println();
			out.println("======= Totals =======");
			for(int i=0;i<st.configs.size();++i) {
				EvaluationConfig c = st.configs.get(i);
				out.println(c.name+"\t"+totalTimes[i]+"\t"+c.description());
			}
			this.printRatios(st, totalTimes);
		}

		public void printRatios(SpeedTestComplex st, long[] time) {
			out.println();
			out.println("======= Ratios =======");
			out.print("\t");
			for(int i=0;i<time.length;++i) 
				out.print(st.configs.get(i).name+"\t");
			out.println();

			for(int i=0;i<time.length;++i) {
				out.print(st.configs.get(i).name+"\t");
				for(int j=0;j<time.length;++j) {
					long t1 = time[i];
					long t2 = time[j];
					if(t2 != 0) {
						Double ratio = ((double) t1) / t2;
						out.printf("%.2f\t", new Object[] { ratio });
					} else
						out.print("" + t1 + "/0\t");
				}
				out.println();
			}
		}

	}
	/**
	 * Print detailed ratios for each run.
	 */
	public static class RatioOutputter extends Outputter {
		@Override
		public void printOutputTimes(SpeedTestComplex st,String eqn, String varNames[],long[] times) {
			super.printOutputTimes(st,eqn,varNames,times);
			printRatios(st,times);
		}
		@Override
		public void printOutputTimes(SpeedTestComplex st,String eqns[], String varNames[],long[] times) {
			super.printOutputTimes(st, eqns, varNames, times);
			printRatios(st,times);
		}
		@Override
		public void printFooter(SpeedTestComplex st, long[] totalTimes) {
			super.printFooter(st, totalTimes);
		}
	}
	/**
	 * Print output tab separated.
	 * One line of headers
	 * One line per test
	 * One line of totals
	 */
	public static class TabOutputter extends Outputter {
		@Override
		public void printOutputTimes(SpeedTestComplex st,String eqn, String varNames[],long[] times) {
			out.print(eqn);

			for(int i=0;i<st.configs.size();++i) {
				out.print("\t");
				out.print(times[i]);
			}
			out.println();
		}
		@Override
		public void printOutputTimes(SpeedTestComplex st,String eqns[], String varNames[],long[] times) {
			for(int i=0;i<eqns.length;++i) {
				out.print(eqns[i] + ";");
			}

			for(int i=0;i<st.configs.size();++i) {
				out.print("\t");
				out.print(times[i]);
			}
			out.println();
		}
		@Override
		public void printFooter(SpeedTestComplex st, long[] totalTimes) {
			out.print("Total");
			for(int i=0;i<st.configs.size();++i) {
				out.print("\t");
				out.print(totalTimes[i]);
			}
			out.println();
		}
		@Override
		public void printHeader(SpeedTestComplex st) {
			for(int i=0;i<st.configs.size();++i) {
				out.print("\t");
				out.print(st.configs.get(i).name);
			}
			out.println();
		}


	}

	/**
	 * Run speed comparison for a single equation.
	 * 
	 * @param eqn
	 *            The equation to test
	 * @param varNames
	 *            an array of variable names which will be set to random values.
	 */
	public void doAll(String eqn, String varNames[]) {

		outputter.printOutputHeader(this,eqn, varNames);

		Complex varVals[][] = new Complex[varNames.length][num_vals];

		for (int i = 0; i < varNames.length; ++i) {
			for (int j = 0; j < num_vals; ++j)
				varVals[i][j] = new Complex(generator.nextDouble(),generator.nextDouble());
		}

		long times[] = new long[configs.size()];
		for (int i=0; i < configs.size(); ++i) {
			EvaluationConfig c = configs.get(i);
			times[i] = c.doEval(eqn, varNames, varVals);
			totalTimes[i] += times[i];
		}
		outputter.printOutputTimes(this,eqn, varNames, times);
		//		Runtime rt = Runtime.getRuntime();
		//		out.format("free %d max %d total %d%n",rt.freeMemory(),rt.maxMemory(),rt.totalMemory());
	}

	/**
	 * Run speed comparison for a set of equations.
	 * @param eqns
	 * @param varNames
	 */
	public void doAll(String eqns[], String varNames[]) {

		outputter.printOutputHeader(this,eqns, varNames);
		Complex varVals[][] = new Complex[varNames.length][num_vals];

		for (int i = 0; i < varNames.length; ++i) {
			for (int j = 0; j < num_vals; ++j)
				varVals[i][j] = new Complex(generator.nextDouble(),generator.nextDouble());
		}

		long times[] = new long[configs.size()];
		for(int i=0;i<configs.size();++i) {
			EvaluationConfig c = configs.get(i);
			times[i] = c.doEval(eqns, varNames, varVals);
			totalTimes[i] += times[i];
		}
		outputter.printOutputTimes(this,eqns, varNames, times);
		//		Runtime rt = Runtime.getRuntime();
		//		out.format("free %d max %d total %d%n",rt.freeMemory(),rt.maxMemory(),rt.totalMemory());
	}
	/** Basic class to set the evaluation context **/


	public static abstract class EvaluationConfig {
		protected String name;
		protected Jep jep;
		/** Factor to reduce the number of iterations for this evaluator. Useful for very slow evaluators. */ 
		protected int div=1;
		protected SpeedTestComplex st;
		/**
		 * @param name
		 */
		public EvaluationConfig(String name) {
			this.name = name;
		}
		/**
		 * For slow 
		 * @param name
		 * @param div
		 */
		public EvaluationConfig(String name, int div) {
			super();
			this.name = name;
			this.div = div;
		}

		public Object getValue(Complex d) {
			return d;
		}

		public abstract String description();

		public long doEval(String eqn, String varNames[], Complex vals[][])
		{
			return doEval(new String[]{eqn}, varNames, vals);
		}

		public long doEval(String eqns[], String varNames[], Complex vals[][])
		{
			long tdiff = 0;
			try {
				// add all variables listed in the varNames array
				final Variable vars[] = new Variable[varNames.length];
				for (int i=0; i < varNames.length; ++i)
					vars[i] = jep.addVariable(varNames[i]);

				// create a 2d array of values to set the variables to
				Object bdvals[][] = new Object[vals.length][st.num_vals];
				for (int i=0; i < vals.length; ++i)
					for (int j=0; j < st.num_vals; ++j)
						bdvals[i][j] = getValue(vals[i][j]);

				// parse all equations in the eqns array and store the root nodes in an array
				final Node nodes[] = new Node[eqns.length];
				for (int i=0; i < eqns.length; ++i)
					nodes[i] = jep.parse(eqns[i]);

				// Warm up run
				for (int i = 0; i < st.num_warmup/div; ++i) {
					// set each variable value
					for (int j = 0; j < vars.length; ++j)
						vars[j].setValue(bdvals[j][i % st.num_vals]);
					for (int j=0; j < eqns.length; ++j)
						jep.evaluate(nodes[j]);
				}



				Runnable jogger = new Runnable() {
					public void run() {
						try {
							for (int i = 0; i < st.num_itts/div; ++i) {
								// set each variable value
								for (int j = 0; j < vars.length; ++j)
									vars[j].setValue(bdvals[j][i % st.num_vals]);
								for (int j=0; j < eqns.length; ++j)
									jep.evaluate(nodes[j]);
							}
						} catch (EvaluationException e) {
							System.out.println(e);
						}
					}};

					// get current time
					long t1 = System.currentTimeMillis();
					// perform iterations
					jogger.run();
					// get current time
					long t2 = System.currentTimeMillis();
					// calc time elapsed
					tdiff = t2 - t1;
				} catch (Exception e) {
					out.println("Error: " + this.name + "\t"+  e.toString());
					//e.printStackTrace();
					tdiff = -1;
				}
				return tdiff * div;
			}
		}

		/**
		 * Standard Jep configuration (with FastEvaluator)
		 */
		public static class JepConfig extends EvaluationConfig {

			public JepConfig(String name) {
				super(name);
				jep = new Jep();
			}

			@Override
			public String description() {
				return "Standard Jep config";
			}
		}

		/**
		 * Standard Jep configuration (with FastEvaluator)
		 */
		public static class JepConfigNoChecks extends EvaluationConfig {

			public JepConfigNoChecks(String name) {
				super(name);
				jep = new Jep(new FastEvaluator());
				((FastEvaluator) jep.getEvaluator()).setTrapUnsetValues(false);
				((FastEvaluator) jep.getEvaluator()).setTrapInfinity(false);
				((FastEvaluator) jep.getEvaluator()).setTrapNullValues(false);
				((FastEvaluator) jep.getEvaluator()).setTrapNaN(false);
			}

			@Override
			public String description() {
				return "Fast evaluator with no checks";
			}
		}

		/**
		 * Standard Jep configuration (with FastEvaluator)
		 */
		public static class JepConfig3_4 extends EvaluationConfig {

			public JepConfig3_4(String name) {
				super(name);
				jep = new Jep(new FastEvaluator3_4());
			}

			@Override
			public String description() {
				return "Jep 3.4 config";
			}
		}

		public static class JepConfig3_5 extends EvaluationConfig {

			public JepConfig3_5(String name) {
				super(name);
				FastEvaluator3_5 fe = new FastEvaluator3_5();
				jep = new Jep(fe);
			}

			@Override
			public String description() {
				return "Jep 3.5 config";
			}
		}

		public static class JepConfig3_5NC extends EvaluationConfig {

			public JepConfig3_5NC(String name) {
				super(name);
				FastEvaluator3_5 fe = new FastEvaluator3_5();
				fe.setTrapInfinity(false);
				fe.setTrapNaN(false);
				fe.setTrapNullValues(false);
				fe.setTrapUnsetValues(false);
				jep = new Jep(fe);
			}

			@Override
			public String description() {
				return "Jep 3.5 config no checks";
			}
		}

		public static class UncheckedConfig extends EvaluationConfig {

			public UncheckedConfig(String name) {
				super(name);
				jep = new Jep(new UncheckedEvaluator());
			}

			@Override
			public String description() {
				return "UncheckedEvaluator";
			}
		}

		/**
		 * Configuration using the StandardEvaluator
		 */
		public static class OldConfig extends EvaluationConfig {

			public OldConfig(String name) {
				super(name);
				final StandardEvaluator standardEvaluator = new StandardEvaluator();
				standardEvaluator.setTrapInfinity(false);
				standardEvaluator.setTrapNaN(false);
				standardEvaluator.setTrapNaN(false);
				jep = new Jep(standardEvaluator);
			}

			@Override
			public String description() {
				return "Old Jep configuration with StandardEvaluator";
			}
		}

		/**
		 * Configuration using the RealEvaluator
		 */
		public static class RealConfig extends EvaluationConfig {

			public RealConfig(String name) {
				super(name);
				jep = new Jep(new RealEvaluator());
			}

			@Override
			public String description() {
				return "Jep with RealEvaluator";
			}
		}

		/**
		 * Configuration using the RealEvaluator
		 */
		public static class PostfixConfig extends EvaluationConfig {

			public PostfixConfig(String name) {
				super(name);
				jep = new Jep(new PostfixEvaluator());
				div=5;
			}

			@Override
			public String description() {
				return "Jep with PostfixEvaluator";
			}
		}

		/**
		 * Configuration using the RealEvaluator
		 */
		public static class ThreadSafeConfig extends EvaluationConfig {

			public ThreadSafeConfig(String name) {
				super(name);
				jep = new Jep(new ThreadSafeEvaluator());
			}

			@Override
			public String description() {
				return "Jep with ThreadSafeEvaluator";
			}
		}

		/** 
		 * Adds a new EvaluationConfig to be be run for comparison.
		 * @param config
		 */
		public void addConfig(EvaluationConfig config) {
			this.configs.add(config);
			config.st = this;
			commonSetup(config.jep);
		}

		/**
		 * @param config
		 */
		private static void commonSetup(Jep jep) {


		}

		/**
		 * Main method, executes all speed tests.
		 * Arguments '-pause' causes the program to wait for keyboard input 
		 * before running test, useful use in profilers.
		 * Or a number specifying number of iterations to run.
		 * @param args Can contain '-pause' or NUM  
		 */
		public static void main(String args[])	{
			SpeedTestComplex st = new SpeedTestComplex(new Outputter());
			boolean doPause = false;
			for(int i=0; i<args.length;++i) {
				if("-pause".equals(args[i])) 
					doPause = true;
				else {
					Scanner scan = new Scanner(args[i]);
					if(scan.hasNextInt())
						st.num_itts = scan.nextInt();
					scan.close();
				}
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			st.addConfig(new JepConfig("Jep"));
			st.addConfig(new JepConfigNoChecks("JepNC"));
			st.addConfig(new UncheckedConfig("Uncheck"));
			st.addConfig(new JepConfig3_4("Jep3.4"));

			st.addConfig(new JepConfig3_5("J3.5CK"));
			st.addConfig(new JepConfig3_5NC("J3.5NC"));

			st.addConfig(new OldConfig("OldJep"));
			st.addConfig(new ThreadSafeConfig("ThrdS"));
			st.addConfig(new PostfixConfig("Postfix"));
			st.init();

			if(doPause) {
				try {
					System.out.println("Press enter to continue");
					br.readLine();
				} catch (IOException e) {
				}
			}

			st.testBasicComplexOperations();
			st.runTests();
			st.fini();

			if(doPause) {
				try {
					System.out.println("Press enter to continue");
					br.readLine();
				} catch (IOException e) {
				}
			}
		}

		private void testBasicComplexOperations() {
			Random rnd = new Random();
			Complex[] vals = new Complex[num_itts]; 
			//Complex[] res = new Complex[num_itts];
			System.out.println("Testing integer power operations.");
			System.out.println("  power(Complex z), fastPower(n), powerI(n) powerD(n)");
			for(int i=0;i<num_itts;++i) {
				vals[i] = new Complex(rnd.nextDouble(),rnd.nextDouble());
			}
			for(int pow = 1;pow<20;++pow) {
				Complex cpow = new Complex(pow);
				long start = System.nanoTime();
				for(int i=0;i<num_itts;++i) {
					Complex c = vals[i];
						c.power(cpow);
				}
				long mid = System.nanoTime();
				for(int i=0;i<num_itts;++i) {
					Complex c = vals[i];
					c.fastPower(pow);
				}
				long mid2 = System.nanoTime();
				for(int i=0;i<num_itts;++i) {
					Complex c = vals[i];
					c.powerI(pow);
				}
				long mid3 = System.nanoTime();
				for(int i=0;i<num_itts;++i) {
					Complex c = vals[i];
					c.powerD(pow);
				}
				long finish = System.nanoTime();
				System.out.format("z^%d power %,d fastPower %,d powerI %,d powerD %,d%n", 
						pow, (mid-start)/1000,(mid2-mid)/1000,(mid3-mid2)/1000,(finish-mid3)/1000);
			}
			
		}

		/** 
		 * A standard set of tests.
		 */
		public  void runTests() {
			doAll("(z+w)/(z-w)", new String[]{"z","w"});
			doAll(Utils.hornerExpression("z", 15), new String[]{"z"});
			doAll(new String[] {
					"z=z^2+c","z=z^2+c","z=z^2+c","z=z^2+c",
					"z=z^2+c","z=z^2+c","z=z^2+c","z=z^2+c"},
					new String[]{"z","c"});
			doAll(new String[] {
					"z=z*z+c","z=z*z+c","z=z*z+c","z=z*z+c",
					"z=z*z+c","z=z*z+c","z=z*z+c","z=z*z+c"},
					new String[]{"z","c"});
		}
	}
