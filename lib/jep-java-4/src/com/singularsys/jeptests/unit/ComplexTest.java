/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;

import com.singularsys.jep.standard.Complex;
import com.singularsys.jep.standard.Complex.NonPropagatingImmutableComplex;
import com.singularsys.jep.standard.ImmutableComplex;

public class ComplexTest {

	static final boolean PRINT_ALL = false;
	/**
	 * Tests the power method
	 */
	@Test
	public void testPower() {
		Complex one = new Complex(1, 0);
		Complex negOne = new Complex(-1, 0);
		Complex negi = new Complex(0,-1);
		Complex i = new Complex(0, 1);
		Complex two = new Complex(2, 0);
		
		BiFunction<Integer,Integer,String> formula1 = (n,d) -> "(e^("+(d<0?"-":"")+"2 pi i "+n+"/"+Math.abs(d)+"))^"+d;
		BiFunction<Integer,Integer,String> formula3 = (n,d) -> "(3e^("+(d<0?"-":"")+"2 pi i "+n+"/"+Math.abs(d)+"))^"+d;

		// power
		assertTrue((one.power(one)).equals(one,0));
		assertTrue((one.power(-1)).equals(one,0));
		assertTrue((one.power(negOne)).equals(one,0));
		assertTrue((negOne.power(two)).equals(one,0));
		assertTrue((i.power(two)).equals(negOne, 0));
		//assertTrue((negEight.power(1.0/3)).equals(negTwo,0));

		double worse_error = 0.0;
		int worse_n = 0; int worse_d = 0;
		for(int denom=-20;denom<=20;++denom) {
			if(denom==0) continue;
			final double expected_d = Math.pow(3.0, denom);
			Complex expected = new Complex(expected_d);
			double tol = expected_d * 1e-13;
			final double ulp = Math.ulp(expected_d);
			if(PRINT_ALL)
			System.out.printf("Expected 3^%d = %4.3e tol %3.2e ulp %3.2e%n",denom,expected_d,tol,ulp);

			for(int n=0;n<=Math.abs(denom);++n) {
				Complex root_of_unity = Complex.polarValueOf(3.0, n*2*Math.PI / denom);
				Complex power = new Complex(denom);
				String formula = formula3.apply(n, denom);
				if(PRINT_ALL)
				System.out.printf(formula+"\t");

				{
					Complex res = root_of_unity.power(power);
					Complex diff = res.sub(expected);
					final double error = diff.abs()/expected_d;
					if(error>worse_error) {
						worse_error = error;
						worse_n = n;
						worse_d = denom;
					}
					if(PRINT_ALL)
					System.out.printf(" C  %3.0f %3.2g",diff.abs()/ulp,error);
//					System.out.printf(" C %3.0f",diff.abs()/ulp);
					assertTrue(formula+res+"!="+expected,res.equals(expected, tol));
				}
				{
					Complex res = root_of_unity.fastPower(denom);
					Complex diff = res.sub(expected);
					if(PRINT_ALL)
					System.out.printf(" I %3.0f",diff.abs()/ulp);
					assertTrue(formula+res+"!="+expected,res.equals(expected, tol));
				}
				{
					Complex res2 = root_of_unity.powerI(denom);
					Complex diff = res2.sub(expected);
					if(PRINT_ALL)
					System.out.printf(" I2 %3.0f",diff.abs()/ulp);
					assertTrue(formula+res2+"!="+expected,res2.equals(expected, tol));
				}
				{
					Complex res3 = root_of_unity.powerD(denom);
					Complex diff = res3.sub(expected);
					if(PRINT_ALL)
					System.out.printf(" D %3.0f",diff.abs()/ulp);
					assertTrue(formula+res3+"!="+expected,res3.equals(expected, tol));
				}
				// there can be rounding errors if numbers lie on a coordinate axis
				double re = root_of_unity.re();
				double im = root_of_unity.im();
				if(Math.abs(re)<1e-9) re=0.0;
				if(Math.abs(im)<1e-9) im=0.0;
				if(re==0.0 || im == 0.0) {
					root_of_unity.set(re, im);
					{
						Complex res = root_of_unity.power(power);
						Complex diff = res.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" C %3.0f",diff.abs()/ulp);
						assertTrue(formula+res+"!="+expected,res.equals(expected, tol));
					}
					{
						Complex res = root_of_unity.fastPower(denom);
						Complex diff = res.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" I  %3.0f %3.2g",diff.abs()/ulp,diff.abs()/expected_d);
						assertTrue(formula+res+"!="+expected,res.equals(expected, tol));
					}
					{
						Complex res3 = root_of_unity.powerI(denom);
						Complex diff = res3.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" I2 %3.0f",diff.abs()/ulp);
						assertTrue(formula+res3+"!="+expected,res3.equals(expected, tol));
					}
					{
						Complex res3 = root_of_unity.powerD(denom);
						Complex diff = res3.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" D %3.0f",diff.abs()/ulp);
						assertTrue(formula+res3+"!="+expected,res3.equals(expected, tol));
					}
				}
				if(PRINT_ALL)
				System.out.println();

			}
		}

		{
			String formula = formula3.apply(worse_n, worse_d);
			System.out.println("Worse result "+formula);
			final double expected_d = Math.pow(3.0, worse_d);
			Complex expected = new Complex(expected_d);
			final double ulp = Math.ulp(expected_d);
			System.out.printf("Expected 3^%d = %4.3e%n",worse_d,expected_d);
			Complex root_of_unity = Complex.polarValueOf(3.0, worse_n*2*Math.PI / worse_d);
			Complex power = new Complex(worse_d);

			{
				Complex res = root_of_unity.power(power);
				Complex diff = res.sub(expected);
				
				System.out.printf(" C  3^%d+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						worse_d,res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_of_unity.fastPower(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" FP 3^%d+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						worse_d,res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_of_unity.powerI(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" I  3^%d+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						worse_d,res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_of_unity.powerD(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" D  3^%d+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						worse_d,res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}

		}

		worse_error = 0.0;
		worse_n = 0; worse_d = 0;

		for(int pow=-20;pow<=20;++pow) {
			if(pow==0) continue;
			final double expected_d = 1.0; //Math.pow(3.0, pow);
			Complex expected = new Complex(expected_d);
			final double ulp = Math.ulp(expected_d);
			double tol = ulp * 128; //expected_d * 1e-13;
			if(PRINT_ALL)
			System.out.printf("Expected 1^%d = %4.3e tol %3.2e ulp %3.2e%n",pow,expected_d,tol,ulp);

			for(int n=0;n<=Math.abs(pow);++n) {
				Complex root_of_unity = Complex.polarValueOf(1.0, n*2*Math.PI / pow);
				Complex power = new Complex(pow);
				final String formula = formula1.apply(n, pow);
				if(PRINT_ALL)
				System.out.printf(formula+"\t",n,pow,pow);

				{
					Complex res = root_of_unity.power(power);
					Complex diff = res.sub(expected);
					final double error = diff.abs()/expected_d;
					if(error>worse_error) {
						worse_error = error;
						worse_n = n;
						worse_d = pow;
					}

					if(PRINT_ALL)
					System.out.printf(" C %3.0f",diff.abs()/ulp);
					assertTrue(formula+"="+res+"!="+expected,res.equals(expected, tol));
				}
				{
					Complex res = root_of_unity.fastPower(pow);
					Complex diff = res.sub(expected);
					if(PRINT_ALL)
					System.out.printf(" I %3.0f",diff.abs()/ulp);
					assertTrue(formula+"="+res+"!="+expected,res.equals(expected, tol));
				}
				{
					Complex res2 = root_of_unity.powerI(pow);
					Complex diff = res2.sub(expected);
					if(PRINT_ALL)
					System.out.printf(" I2 %3.0f",diff.abs()/ulp);
					assertTrue(formula+"="+res2+"!="+expected,res2.equals(expected, tol));
				}
				{
					Complex res3 = root_of_unity.powerD(pow);
					Complex diff = res3.sub(expected);
					if(PRINT_ALL)
					System.out.printf(" D %3.0f",diff.abs()/ulp);
					assertTrue(formula+"="+res3+"!="+expected,res3.equals(expected, tol));
				}
				// there can be rounding errors if numbers lie on a coordinate axis
				double re = root_of_unity.re();
				double im = root_of_unity.im();
				if(Math.abs(re)<1e-9) re=0.0;
				if(Math.abs(im)<1e-9) im=0.0;
				if(re==0.0 || im == 0.0) {
					root_of_unity.set(re, im);
					{
						Complex res = root_of_unity.power(power);
						Complex diff = res.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" C %3.0f",diff.abs()/ulp);
						assertTrue(formula+"="+res+"!="+expected,res.equals(expected, tol));
					}
					{
						Complex res = root_of_unity.fastPower(pow);
						Complex diff = res.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" I  %3.0f",diff.abs()/ulp);
						assertTrue(formula+"="+res+"!="+expected,res.equals(expected, tol));
					}
					{
						Complex res3 = root_of_unity.powerI(pow);
						Complex diff = res3.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" I2 %3.0f",diff.abs()/ulp);
						assertTrue(formula+"="+res3+"!="+expected,res3.equals(expected, tol));
					}
					{
						Complex res3 = root_of_unity.powerD(pow);
						Complex diff = res3.sub(expected);
						if(PRINT_ALL)
						System.out.printf(" D %3.0f",diff.abs()/ulp);
						assertTrue(formula+"="+res3+"!="+expected,res3.equals(expected, tol));
					}
				}
				if(PRINT_ALL)
				System.out.println();

			}
		}

		
		{
			String formula = formula1.apply(worse_n, worse_d);
			System.out.println("Worse result "+formula);
			final double expected_d = Math.pow(1.0, worse_d);
			Complex expected = new Complex(expected_d);
			final double ulp = Math.ulp(expected_d);
			System.out.printf("Expected 1^%d = %4.3e%n",worse_d,expected_d);
			Complex root_of_unity = Complex.polarValueOf(1.0, worse_n*2*Math.PI / worse_d);
			Complex power = new Complex(worse_d);

			{
				Complex res = root_of_unity.power(power);
				Complex diff = res.sub(expected);
				System.out.printf(" C  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_of_unity.fastPower(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" FP 1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_of_unity.powerI(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" I  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_of_unity.powerD(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" D  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			
			double re = root_of_unity.re();
			double im = root_of_unity.im();
			double re_d = Math.nextDown(re);
			double im_d = Math.nextDown(im);
//			double re_u = Math.nextUp(re);
//			double im_u = Math.nextUp(Math.nextUp(im));
			Complex root_dd = new Complex(re_d,im_d);
			Complex root_uu = Complex.polarValueOf(1.0, worse_n*2*Math.nextUp(Math.PI) / worse_d);
			
			{
				Complex res = root_dd.power(power);
				Complex diff = res.sub(expected);
				System.out.printf(" C  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_dd.fastPower(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" FP 1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_dd.powerI(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" I  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_dd.powerD(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" D  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}

			{
				Complex res = root_uu.power(power);
				Complex diff = res.sub(expected);
				System.out.printf(" C  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_uu.fastPower(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" FP 1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_uu.powerI(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" I  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}
			{
				Complex res = root_uu.powerD(worse_d);
				Complex diff = res.sub(expected);
				System.out.printf(" D  1+(%+4.3g,%+4.3g) |actual-expected|/|expected| %4.3g = %3.0f ulp%n",
						res.re()-expected_d,res.im(),
						diff.abs()/expected_d,diff.abs()/ulp);
			}

		}

		Function<Integer,Complex> i_pow_n = n -> {
			switch(Math.floorMod(n,4) ) { 
			case 0: return Complex.ONE; 
			case 1: return Complex.I;
			case 2: return negOne;
			case 3: return negi;
			default: return Complex.ZERO;
			}
		};
		Complex c2i = new Complex(0,2);
		Complex cm2i = new Complex(0,-2);
		Complex c2 = new Complex(2,0);
		Complex cm2 = new Complex(-2,0);
		for(int k=-5;k<=5;++k) { 
			final double expected_d = Math.pow(2, k);

			Complex ex_2i = i_pow_n.apply(k).mul(expected_d);
			Complex ex_m2i = i_pow_n.apply(-k).mul(expected_d);
			Complex ex_m2 = i_pow_n.apply(2*k).mul(expected_d);
			Complex ex_2 = i_pow_n.apply(4*k).mul(expected_d);

			BiConsumer<Complex,Complex> Ceq = (z,w) -> assertTrue(""+z+"!="+w,z.eq(w)); 
			BiConsumer<Complex,Integer> Ceqpow = (z,n) -> Ceq.accept(z.fastPower(n),z.powerD(n));
//			final double tol = Math.ulp(expected_d)*128;
//			BiConsumer<Complex,Complex> Ceqtol = (z,w) -> assertTrue(""+z+"!="+w,z.equals(w, tol)); 
//			BiConsumer<Complex,Integer> Ceqpowtol = (z,n) -> Ceqtol.accept(z.fastPower(n),z.powerD(n));
			Ceqpow.accept(c2i,k);
			Ceqpow.accept(cm2i,k);
			Ceqpow.accept(c2,k);
			Ceqpow.accept(cm2,k);
			Ceq.accept(ex_2i, c2i.fastPower(k));
			Ceq.accept(ex_m2i, cm2i.fastPower(k));
			Ceq.accept(ex_m2, cm2.fastPower(k));
			Ceq.accept(ex_2, c2.fastPower(k));
			Ceq.accept(ex_2i, c2i.powerD(k));
			Ceq.accept(ex_m2i, cm2i.powerD(k));
			Ceq.accept(ex_m2, cm2.powerD(k));
			Ceq.accept(ex_2, c2.powerD(k));
			Ceq.accept(ex_2i, c2i.powerI(k));
			Ceq.accept(ex_m2i, cm2i.powerI(k));
			Ceq.accept(ex_m2, cm2.powerI(k));
			Ceq.accept(ex_2, c2.powerI(k));
		}

	}

	/**
	 * Tests the mul method
	 */
	@Test
	public void testMul() {
		Complex one = new Complex(1, 0);
		Complex negOne = new Complex(-1, 0);
		Complex i = new Complex(0, 1);

		// multiplication
		assertTrue((one.mul(one)).equals(one,0));
		assertTrue((one.mul(negOne)).equals(negOne,0));
		assertTrue((negOne.mul(one)).equals(negOne,0));
		assertTrue((i.mul(i)).equals(negOne,0));
	}

	@Test
	public void testRecip() {
		for(double r = -3;r<=3; r+= 0.25) {
			for(double i = -3; i<= 3; i+= 0.25) {
				if(r==0 && i==0) continue;
				Complex z = new Complex(r,i);
				Complex div = Complex.ONE.div(z);
				Complex recip = z.reciprocal();
				assertEquals("re(1/"+z.toString(),div.re(),recip.re(),1e-9);
				assertEquals("im(1/"+z.toString(),div.im(),recip.im(),1e-9);
			}
		}
	}
	@Test
	public void testMessage() {
		Complex one = new Complex(1, 0);
		Complex negOne = new Complex(-1, 0);
		Complex i = new Complex(0, 1);
		Complex negi = new Complex(0, -1);
		Complex pi4 = Complex.polarValueOf(1,Math.PI/4);
		assertEquals("(1.0, 0.0)",one.toString());
		assertEquals("(-1.0, 0.0)",negOne.toString());
		assertEquals("(0.0, 1.0)",i.toString());
		assertEquals("(0.0, -1.0)",negi.toString());
		assertEquals("("+pi4.re()+", "+pi4.im()+")",pi4.toString());
		try {
			Complex.MINUS_ONE.setIm(1);
		} catch(UnsupportedOperationException e){
			assertEquals("Cannot modify an ImmutableComplex number.",e.getMessage());
		}

	}

	@Test
	public void testMessageIstyle() {
		Complex one = new Complex(1, 0);
		Complex negOne = new Complex(-1, 0);
		Complex i = new Complex(0, 1);
		Complex negi = new Complex(0, -1);
		Complex q1 = new Complex(1,1);
		Complex q2 = new Complex(-1,1);
		Complex q3 = new Complex(-1,-1);
		Complex q4 = new Complex(1,-1);

		//        Complex pi4 = Complex.polarValueOf(1,Math.PI/4);
		//        Complex negpi4 = Complex.polarValueOf(1,-Math.PI/4);
		//        Complex pi3 = Complex.polarValueOf(1,Math.PI/3);
		//        Complex negpi3 = Complex.polarValueOf(1,-Math.PI/3);
		//        double rt22 = Math.sqrt(2)/2.0;
		//        double rt32 = Math.sqrt(3)/2.0;
		assertEquals("1.0",one.toString(true,false));
		assertEquals("-1.0",negOne.toString(true,false));
		assertEquals("1.0 i",i.toString(true,false));
		assertEquals("-1.0 i",negi.toString(true,false));
		assertEquals("1.0+1.0 i",q1.toString(true, false));
		assertEquals("-1.0+1.0 i",q2.toString(true, false));
		assertEquals("-1.0-1.0 i",q3.toString(true, false));
		assertEquals("1.0-1.0 i",q4.toString(true, false));

		assertEquals("1.0",one.toString(true,true));
		assertEquals("(-1.0)",negOne.toString(true,true));
		assertEquals("1.0 i",i.toString(true,true));
		assertEquals("(-1.0 i)",negi.toString(true,true));
		assertEquals("(1.0+1.0 i)",q1.toString(true, true));
		assertEquals("(-1.0+1.0 i)",q2.toString(true, true));
		assertEquals("(-1.0-1.0 i)",q3.toString(true, true));
		assertEquals("(1.0-1.0 i)",q4.toString(true, true));

	}

	@Test
	public void testMessageIformat() {
		DecimalFormat fmt = new DecimalFormat("0.0##");
		DecimalFormatSymbols decsym = fmt.getDecimalFormatSymbols();
		decsym.setInfinity("Infinity");
		decsym.setNaN("NaN");
		fmt.setDecimalFormatSymbols(decsym);

		Complex one = new Complex(1, 0);
		Complex negOne = new Complex(-1, 0);
		Complex i = new Complex(0, 1);
		Complex negi = new Complex(0, -1);
		Complex q1 = new Complex(1,1);
		Complex q2 = new Complex(-1,1);
		Complex q3 = new Complex(-1,-1);
		Complex q4 = new Complex(1,-1);

		Complex pi4 = Complex.polarValueOf(1,Math.PI/4);
		Complex negpi4 = Complex.polarValueOf(1,-Math.PI/4);
		Complex pi3 = Complex.polarValueOf(1,2*Math.PI/3);
		Complex negpi3 = Complex.polarValueOf(1,-2*Math.PI/3);
		double rt22 = Math.sqrt(2)/2.0;
		double rt32 = Math.sqrt(3)/2.0;
		assertEquals("1.0",one.toString(fmt,false));
		assertEquals("-1.0",negOne.toString(fmt,false));
		assertEquals("1.0 i",i.toString(fmt,false));
		assertEquals("-1.0 i",negi.toString(fmt,false));
		assertEquals(""+fmt.format(rt22)+"+"+fmt.format(rt22)+" i",pi4.toString(fmt,false));
		assertEquals(""+fmt.format(rt22)+"-"+fmt.format(rt22)+" i",negpi4.toString(fmt,false));
		assertEquals("-0.5+"+fmt.format(rt32)+" i",pi3.toString(fmt,false));
		assertEquals("-0.5-"+fmt.format(rt32)+" i",negpi3.toString(fmt,false));
		assertEquals("1.0+1.0 i",q1.toString(fmt, false));
		assertEquals("-1.0+1.0 i",q2.toString(fmt, false));
		assertEquals("-1.0-1.0 i",q3.toString(fmt, false));
		assertEquals("1.0-1.0 i",q4.toString(fmt, false));

		assertEquals("1.0",one.toString(fmt,true));
		assertEquals("(-1.0)",negOne.toString(fmt,true));
		assertEquals("1.0 i",i.toString(fmt,true));
		assertEquals("(-1.0 i)",negi.toString(fmt,true));
		assertEquals("(1.0+1.0 i)",q1.toString(fmt, true));
		assertEquals("(-1.0+1.0 i)",q2.toString(fmt, true));
		assertEquals("(-1.0-1.0 i)",q3.toString(fmt, true));
		assertEquals("(1.0-1.0 i)",q4.toString(fmt, true));

		Complex inf = new Complex(Double.POSITIVE_INFINITY);
		Complex iinf = new Complex(0.0,Double.POSITIVE_INFINITY);
		Complex nan = new Complex(Double.NaN);
		assertEquals("Infinity",inf.toString(fmt, false));
		assertEquals("Infinity i",iinf.toString(fmt, false));
		assertEquals("NaN+0.0 i",nan.toString(fmt, false));
	}

	@Test
	public void testImmutableComplex() {
		{
		Complex c1 = new NonPropagatingImmutableComplex(3,4);
		assertThrows(UnsupportedOperationException.class,() -> c1.set(4, 3));

		Complex c2 = new NonPropagatingImmutableComplex(3);
		assertThrows(UnsupportedOperationException.class,() -> c2.setRe(4));

		Complex c3 = new NonPropagatingImmutableComplex(0,3);
		assertThrows(UnsupportedOperationException.class,() -> c3.setIm(4));

		Complex c4 = new NonPropagatingImmutableComplex(new Complex(3,4));
		assertThrows(UnsupportedOperationException.class,() -> c4.set(new Complex(5,6)));

		Complex c5 = NonPropagatingImmutableComplex.polarValueOf(3, Math.PI/4);
		assertThrows(UnsupportedOperationException.class,() -> c5.set(5,6));
		
		final Complex c6 = c1.add(c2);
		assertEquals(new Complex(6,4),c6);
		c6.setRe(5);
		assertEquals(new Complex(5,4),c6);
		}
		
		{
			Complex c1 = Complex.ONE;
			assertThrows(UnsupportedOperationException.class,() -> c1.setRe(4));

			Complex c2 = Complex.ONE.neg();
			c2.setRe(5);
			assertTrue(c2.eq(new Complex(5,0)));			
		}
		{
		Complex c1 = new ImmutableComplex(3,4);
		assertThrows(UnsupportedOperationException.class,() -> c1.set(4, 3));

		Complex c2 = new ImmutableComplex(3);
		assertThrows(UnsupportedOperationException.class,() -> c2.setRe(4));

		Complex c3 = new ImmutableComplex(0,3);
		assertThrows(UnsupportedOperationException.class,() -> c3.setIm(4));

		Complex c4 = new ImmutableComplex(new Complex(3,4));
		assertThrows(UnsupportedOperationException.class,() -> c4.set(new Complex(5,6)));

		Complex c5 = ImmutableComplex.polarValueOf(3, Math.PI/4);
		assertThrows(UnsupportedOperationException.class,() -> c5.set(5,6));
		
		final Complex c6 = c1.add(c2);
		assertEquals(new Complex(6,4),c6);
		assertThrows(UnsupportedOperationException.class,() -> c6.setRe(5));
		}
		{
			Complex c1 = ImmutableComplex.U_ONE;
			assertThrows(UnsupportedOperationException.class,() -> c1.setRe(4));

			Complex c2 = ImmutableComplex.U_ONE.neg();
			assertThrows(UnsupportedOperationException.class,() -> c2.setRe(4));
		}
		
		
	}
}
