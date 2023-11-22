/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
package com.singularsys.jepexamples.applets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import javax.swing.JPanel;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.misc.LightWeightComponentSet;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.standard.Complex;
import com.singularsys.jep.standard.ImmutableComplex;
import com.singularsys.jep.walkers.ImportationVisitor;

/**
 * This class performs the drawing of the fractal, using a number of
 * threads/runnables to compute the image in chunks.
 * <p>
 * Each <code>Runnable</code> has is own local version of Jep, and its own copy
 * of the expression. These are set during construction of the runnable.
 * 
 * <pre>
 * Jep localJep = new Jep(new LightWeightComponentSet(jep));
 * ImportationVisitor iv = new ImportationVisitor(localJep);
 * Node localExpression = iv.deepCopy(expression);
 * Complex localC = new Complex(0, 0); // A single mutable value
 * Complex localZ = new Complex(0, 0);
 * localJep.addVariable("c", localC);
 * localJep.addVariable("z", localZ);
 * </pre>
 * <p>
 * These are copies are used in the main evaluation loop.
 * 
 * <pre>
 * localC.set(x, y);
 * localZ.set(0, 0);
 * int count = 0;
 * while (count &lt; iterations &amp;&amp; localZ.abs2() &lt; 4.0) {
 * 	localZ.set((Complex) localJep.evaluate(localExpression));
 * 	count++;
 * }
 * </pre>
 * <p>
 * Scheduling of threads is managed by a ThreadPoolExecutor with 10 threads
 * 
 * <pre>
 * ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS,
 * 		new ArrayBlockingQueue&lt;Runnable&gt;(100));
 * </pre>
 * 
 * When a new image is needed a set of <code>Runnable</code>s is created and
 * sent to the executor.
 * 
 * <pre>
 * try {
 * 	 for (int i = 0; i &lt; nDivisions; ++i) {
 * 	    for (int j = 0; j &lt; nDivisions; ++j) {
 *          Runnable blockRunner = new CalcBlockRunnable(i * wid, j * high, wid, high);
 *          Future&lt;Integer&gt; future = executor.submit(blockRunner, wid * high);
 *          synchronized (futures) {
 *              futures.add(future);
 *          }
 *      }
 *   }
 * } catch (RejectedExecutionException ex) {
 *     System.out.println("Rejected execution " + ex.toString());
 * } catch (JepException ex) {
 *     System.out.println(ex);
 * }
 * </pre>
 * <p>
 * Each of these has a <code>Future</code> value used to count the number of
 * completed runnables and cancel the tasks if needed. An <code>ArrayList</code>
 * of these futures is kept. If the screen needs to be redrawn before every task
 * is finished then they are killed by first calling
 * <code>future.cancel(true)</code> and then purging them with
 * <code>executor.purge()</code>.
 * <p>
 * An additional thread is used to collect statistics about completed tasks. A
 * <code>ConcurrentLinkedDeque&lt;Integer&gt;</code> is used to send messages to
 * this queue, and a <code>Lock</code> and <code>Condition</code> is used to
 * wake this thread when there is a new message.
 * 
 * <pre>
 * ConcurrentLinkedDeque&lt;Integer&gt; reportQueue = new ConcurrentLinkedDeque&lt;Integer&gt;();
 * Reporter reporter = new Reporter();
 * Thread reportThead = new Thread(reporter, "ReportThread");
 * reportThead.start();
 * // Lock for reporter
 * private Lock lock = new ReentrantLock();
 * // Condition to signal when the reportQueue has data
 * private Condition notEmpty = lock.newCondition();
 * </pre>
 * <p>
 * Messages are sent to this queue using
 * 
 * <pre>
 * lock.lock();
 * reportQueue.add(nItts);
 * notEmpty.signal();
 * lock.unlock();
 * </pre>
 * <p>
 * And the report thread waits for messages
 * 
 * <pre>
 * lock.lock();
 * while(true) {
 *     while(reportQueue.isEmpty()) {
           notEmpty.awaitUninterruptibly();
 *     }
 *     Integer itts =  reportQueue.pollFirst();
 *     ...
 * }
 * </pre>
 * <p>
 * The image is created using a <code>MemoryImageSource</code> backed by a
 * shared pixel array.
 * 
 * <pre>
 * int[] pixels = new int[dimensions.width * dimensions.height];
 * MemoryImageSource source = new MemoryImageSource(dimensions.width, dimensions.height, pixels, 0, dimensions.width);
 * source.setAnimated(true);
 * Image outImage = Toolkit.getDefaultToolkit().createImage(source);
 * </pre>
 * <p>
 * The pixels are simply set by the runnable
 * <code>pixels[index] = Color.getRGB()</code> and when an entire block is
 * finished image consumers are informed using
 * <code>source.newPixels(rect.x,rect.y,rect.width,rect.height )</code>. The
 * paint method simply uses the <code>Graphic.drawImage</code> using
 * <code>Component</code> <code>imageUpdate()</code> callback method to paint
 * more of the image as it becomes available.
 * 
 * <pre>
 * public void paint(Graphics g) {
 *     g.drawImage(outImage, 0, 0, this);
 * }
 * </pre>
 */
public class FractalCanvas extends JPanel implements ComponentListener {
	private static final long serialVersionUID = -593341831485283712L;

	/** Size of the canvas */
	private Dimension dimensions;
	/** max number of iterations to perform */
	private int iterations;

	private boolean hasError;

	private Jep jep;

	/** Number of divisions in each direction.
	 * image will be divided up into nDivisions^ blocks. 
	 */
	private static final int nDivisions = 5;

	Region region;

	private Node expression;
	Node colorExpressions;



	private RegionListener listner;

	Fractal parent;

	/** Executor for runnables */
	private ThreadPoolExecutor executor;
	/** Future values of runnables */
	List<Future<Integer>> futures = new ArrayList<>();
	/** Queue for reporting */
	ConcurrentLinkedDeque<Integer> reportQueue = new ConcurrentLinkedDeque<Integer>();
	/** Lock for reporter */
	private Lock lock = new ReentrantLock();
	/** Condition to signal when the reportQueue has data */
	private Condition notEmpty = lock.newCondition();

	/** Array of pixels for image */
	private int[] pixels;
	/** ImageProducer based on pixels */
	private MemoryImageSource source;
	/** Image to draw, dynamically updated. */
	private Image outImage;


	/** Start of evaluation */
	long startTime;
	/** Numer of itterations for all pixels */
	long totalItts;

	/**
	 * Constructor.
	 */
	public FractalCanvas(String initialExpression, Fractal parent) {
		this.parent = parent;
		iterations = 200;
		dimensions = getSize();

		region = new Region(-2.0, -2.0, 2.0, 2.0);
		hasError = true;
		initParser(initialExpression);

		listner = new RegionListener();
		this.addMouseListener(listner);
		this.addMouseMotionListener(listner);

		pixels = new int[dimensions.width * dimensions.height];
		this.source = new MemoryImageSource(dimensions.width, dimensions.height, pixels, 0, dimensions.width);
		this.source.setAnimated(true);
		this.outImage = Toolkit.getDefaultToolkit().createImage(source);

		this.addComponentListener(this);

		executor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));

		Reporter reporter = new Reporter();
		Thread reportThead = new Thread(reporter, "ReportThread");
		reportThead.start();
	}

	/**
	 * Initializes the parser
	 */
	void initParser(String initialExpression) {
		// Init Parser
		jep = new Jep();

		jep.setVariable("z", new Complex(0, 0));
		jep.setVariable("c", new Complex(0, 0));

		setExpressionString(initialExpression);
		setColorExpression("[ (itts % 100)/100 ,0,sqrt(itts)/sqrt(maxItts)]");
	}

	/**
	 * Parses a new expression
	 * 
	 * @return true on successful parse
	 */
	public boolean setExpressionString(String newString) {
		// Parse the new expression
		try {
			expression = jep.parse(newString);
			hasError = false;
			return true;
		} catch (ParseException e) {
			parent.showStatus(e.toString());
			hasError = true;
			return false;
		}

	}

	/**
	 * Parses a new expression for colors
	 * 
	 * @return true on successful parse
	 */
	public boolean setColorExpression(String str) {

		// Parse the new expression
		try {
			colorExpressions = jep.parse(str);
			hasError = false;
			return true;
		} catch (ParseException e) {
			parent.showStatus(e.toString());
			return false;
		}
	}

	/**
	 * Sets number of iterations
	 * @param iterations_in
	 */
	public void setIterations(int iterations_in) {
		iterations = iterations_in;
	}

	/**
	 * Main Runnable for calculating a block of pixels
	 */
	class CalcBlockRunnable implements Runnable {
		Rectangle rect;
		private Jep localJep;
		private Node localExpression;
		private Node localColorExpression;
		private Complex localC;
		private Complex localZ;

		public CalcBlockRunnable(Rectangle rect) throws JepException {
			this.rect = rect;
			localJep = new Jep(new LightWeightComponentSet(jep));
			ImportationVisitor iv = new ImportationVisitor(localJep);
			localExpression = iv.deepCopy(expression);
			localC = new Complex(0, 0);
			localJep.addVariable("c", localC);
			localZ = new Complex(0, 0);
			localJep.addVariable("z", localZ);
			localColorExpression = iv.deepCopy(colorExpressions);
		}

		public CalcBlockRunnable(int x, int y, int width, int height) throws JepException {
			this(new Rectangle(x, y, width, height));
		}

		/**
		 * Main calculation method. 
		 */
		@Override
		public void run() {
			int nEvals = 0;
			try {
				for (int loop_x = 0; loop_x < rect.width; loop_x++) {
					for (int loop_y = 0; loop_y < rect.height; loop_y++) {
						final int screen_x = rect.x + loop_x;
						final int screen_y = rect.y + loop_y;
						ImmutableComplex w = getProportions(screen_x, screen_y);
						Complex pt = region.getC(w);
						localC.set(pt);
						localZ.set(0, 0);

						int count = 0;
						while ((count < iterations) && (localZ.abs2() < 4.0)) {
							localZ.set((Complex) localJep.evaluate(localExpression));
							count++;
							nEvals++;
						}

						final int index = screen_x + screen_y * dimensions.width;
						final Color color = calcColor(count);
						pixels[index] = color.getRGB();
					}
				}
				source.newPixels(rect.x, rect.y, rect.width, rect.height);

			} catch (EvaluationException e) {
				System.out.println(e.toString());
			}
			lock.lock();
			reportQueue.add(nEvals);
			notEmpty.signal();
			lock.unlock();
		}

		/**
		 * Uses an expressions to calculate the color of a pixel.
		 * @param count number of iteration until escape.
		 * @return a Color
		 */
		private Color calcColor(int count) {
			try {
				localJep.setVariable("itts", count);
				localJep.setVariable("maxItts", iterations);
				Function<Double, Integer> colorClip = d -> (d < 0 ? 0 : d > 1 ? 255 : (int) (d * 255));

				Object vec = localJep.evaluate(localColorExpression);
				int r = 0, g = 0, b = 0;
				if (vec instanceof List<?>) {
					List<?> list = (List<?>) vec;
					if (list.size() >= 1)
						r = colorClip.apply((Double) list.get(0));
					if (list.size() >= 2)
						g = colorClip.apply((Double) list.get(1));
					if (list.size() >= 3)
						b = colorClip.apply((Double) list.get(2));
				}
				return new Color(r, g, b);
			} catch (EvaluationException ex) {
				// System.out.println(ex);
				if (count != iterations) {
					return new Color(0, 0, (int) (255.0 * (Math.sqrt(count) / Math.sqrt(iterations))));
				} else
					return Color.white;
			}
		}

	}

	/**
	 * Runnable class which waits for other tasks to complete
     * calculates the amount done and sends a progress message to the main app. 
	 */
	class Reporter implements Runnable {

		public void run() {
			lock.lock();
			while (true) {
				while (reportQueue.isEmpty()) {
					notEmpty.awaitUninterruptibly();
				}
				Integer itts = reportQueue.pollFirst();
				if (itts != null)
					totalItts += itts;
				int nDone = 0, nTotal = 0, nPixels = 0;
				synchronized (futures) {
					for (Future<Integer> fut : futures) {
						if (fut.isDone()) {
							++nDone;
							try {
								nPixels += fut.get();
							} catch (ExecutionException | InterruptedException e) {
							}
						}
						++nTotal;
					}
				}
				String msg = nTotal > 0 ? "Done " + (nDone * 100 / nTotal) + "%" : "";

				long finishTime = System.currentTimeMillis();
				long elapsed = finishTime - startTime;
				double perItt = (1000.0 * elapsed) / totalItts;
				double perPixel = (1000.0 * elapsed) / nPixels;
				msg += String.format(" %,d ms %4.3f μs/itt %4.3f μs/pixel", elapsed, perItt, perPixel);
				if (nTotal > 0)
					parent.showProgress(msg);
			}
//			lock.unlock();
		}
	}

	/**
	 * Starts the calculation. Kills waiting tasks then schedules a new set of runnables.
	 */
	 void calcFractal() {
		if(hasError)
			return;
		killWaitingTasks();

		final int wid = dimensions.width / nDivisions;
		final int high = dimensions.height / nDivisions;

		startTime = System.currentTimeMillis();
		totalItts = 0;
		try {
			for (int i = 0; i < nDivisions; ++i) {
				for (int j = 0; j < nDivisions; ++j) {

					Runnable blockRunner = new CalcBlockRunnable(i * wid, j * high, wid, high);
					Future<Integer> future = executor.submit(blockRunner, wid * high);
					synchronized (futures) {
						futures.add(future);
					}
				}
			}
		} catch (RejectedExecutionException ex) {
			System.out.println("Rejected execution " + ex.toString());
		} catch (JepException ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Kill off any tasks which have not completed.
	 */
	private void killWaitingTasks() {
//		int nCancled = 0;
//		int nDone = 0;
		synchronized (futures) {
			for (Future<Integer> fut : futures) {
				if (fut.isDone()) {
//					++nDone;
				} else {
//					++nCancled;
					fut.cancel(true);
				}
			}
			futures.clear();
		}
		executor.purge();
//		System.out.println("Cleaning up tasks " + nDone + " done " + nCancled + " cancled");
	}

	/**
	 * Scale coordinate to be in range 0..1 so both have uniform scale, and largest
	 * range fist the screen.
	 * 
	 * @param x
	 * @param y
	 * @return complex number with proportions.
	 */
	ImmutableComplex getProportions(int x, int y) {
		double xd, yd;
		if (dimensions.width < dimensions.height) {
			xd = (((double) x) - dimensions.width / 2 + dimensions.height / 2) / dimensions.height;
			yd = ((double) y) / dimensions.height;
		} else {
			xd = ((double) x) / dimensions.width;
			yd = (((double) y) - dimensions.height / 2 + dimensions.width / 2) / dimensions.width;
		}
		return new ImmutableComplex(xd, yd);
	}

	@Override
	public void paint(Graphics g) {
		// System.out.println("Painting... ");
		g.drawImage(outImage, 0, 0, this);
	}

	/**
	 * Older non threaded version
	 */
    void paintFractal(Graphics g) {
	Complex z, c;
	int count;
	int nEvals=0;
	long start = System.currentTimeMillis();
	try {
	    c = new Complex(0, 0);
	    jep.addVariable("c", c);
	    z = new Complex(0, 0);
	    jep.addVariable("z", z);

	    System.out.printf("dim %d %d region %s%n",
	    		dimensions.width,dimensions.height,
	    		region);
	    for (int x = 0; x <= (dimensions.width - 1); x++) {
		for (int y = 0; y <= (dimensions.height - 1); y++) {
			ImmutableComplex w = getProportions(x, y);
		    count = 0;
		    Complex pt = region.getC(w);
		    c.set(pt);
		    z.set(0, 0);

		    while ((count < iterations) && (z.abs2() < 4.0)) {
			z.set((Complex) jep.evaluate());
			count++;
			nEvals++;
		    }

		    if (count != iterations) {
			g.setColor(new Color(0, 0, (int) (255.0 * (Math.sqrt(count) / Math.sqrt(iterations)))));
			g.fillRect(x, y, 1, 1);
		    }
		}
	    }
	    long finish = System.currentTimeMillis();	    
		long elapsed = finish - start;
		double perItt = (1000.0 * elapsed) / nEvals;
		double perPixel = (1000.0 * elapsed) / (dimensions.width*dimensions.height);
		String msg = String.format(" %,d ms %4.3f μs/itt %4.3f μs/pixel", elapsed, perItt, perPixel);
		parent.showProgress(msg);

	} catch (JepException e) {
	    System.out.println("Couldn't evaluate expression.");
	}
    }

	/** Responds to mouse drag events translates the scene. */
	public void shiftSelection(RegionListener regionListener) {
		ImmutableComplex A = region.getC(getProportions(regionListener.first_x, regionListener.first_y));
		ImmutableComplex B = region.getC(getProportions(regionListener.last_x, regionListener.last_y));
		Complex diff = A.sub(B);
		Complex tl = new ImmutableComplex(region.x_low, region.y_low).add(diff);
		Complex br = new ImmutableComplex(region.x_high, region.y_high).add(diff);
		region.reshape(tl, br);
		parent.showStatus("Shift " + region);
		this.calcFractal();
		this.repaint();
	}

	/**
	 * Zoom in the sceen
	 * @param mid midpoint of the new scene
	 */
	public void zoomIn(Complex mid) {
		double len = 0.1;
		Complex tl = region.getC(mid.re() - len, mid.im() - len);
		Complex br = region.getC(mid.re() + len, mid.im() + len);
		region.reshape(tl, br);
		parent.showStatus("Zoom in " + region);
		this.calcFractal();
		this.repaint();
	}

	/** 
	 * Zoom out the scene.
	 */
	public void zoomOut() {
		double dx = region.x_high - region.x_low;
		double dy = region.y_high - region.y_low;
		double xmin = region.x_low - 2 * dx;
		double xmax = region.x_high + 2 * dx;
		double ymin = region.y_low - 2 * dy;
		double ymax = region.y_high + 2 * dy;

		region.x_low = xmin;
		region.x_high = xmax;
		region.y_low = ymin;
		region.y_high = ymax;
		parent.showStatus("Zoom out " + region);
		this.calcFractal();
		this.repaint();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		dimensions = getSize();
		this.pixels = new int[dimensions.width * dimensions.height];
		this.source = new MemoryImageSource(dimensions.width, dimensions.height, pixels, 0, dimensions.width);
		this.source.setAnimated(true);
		this.outImage = Toolkit.getDefaultToolkit().createImage(source);
		this.calcFractal();
		this.repaint();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	public void render() {
		this.calcFractal();
		this.repaint();
	}

	/**
	 * Listens to mouse events
	 */
	final class RegionListener implements MouseListener, MouseMotionListener {
		int first_x, first_y;
		int last_x, last_y;

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				Complex A = getProportions(e.getX(), e.getY());
				zoomIn(A);
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				zoomOut();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			first_x = e.getX();
			first_y = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			last_x = e.getX();
			last_y = e.getY();
			shiftSelection(this);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			last_x = e.getX();
			last_y = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}

	/**
	 * Represents the region in complex coordinates.
	 */
	static class Region {
		double x_low, x_high, y_low, y_high;

		public Region(double x_low, double y_low, double x_high, double y_high) {
			super();
			this.x_low = x_low;
			this.x_high = x_high;
			this.y_low = y_low;
			this.y_high = y_high;
		}

		public String toString() {
			return String.format("(%8.6f,%5.3f)..(%8.6f,%8.6f)%n", x_low, y_low, x_high, y_high);
		}

		/**
		 * Gets the c value given point on the screen
		 * 
		 * @param z with real and imaginary components between 0 and 1.
		 * @return
		 */
		public ImmutableComplex getC(ImmutableComplex z) {
			return getC(z.re(), z.im());
		}

		/**
		 * Gets the c value given point on the screen
		 * 
		 * @param x between 0 and 1
		 * @param y between 0 and 1
		 * @return
		 */
		public ImmutableComplex getC(double x, double y) {
			return new ImmutableComplex(x_low * (1 - x) + x_high * x, y_low * (1 - y) + y_high * y);
		}

		/**
		 * Reset the domain
		 * @param tl coordinated of top left corner.
		 * @param br coordinated of bottom right coordinates.
		 */
		public void reshape(Complex tl, Complex br) {
			x_low = tl.re() < br.re() ? tl.re() : br.re();
			x_high = tl.re() < br.re() ? br.re() : tl.re();
			y_low = tl.im() < br.im() ? tl.im() : br.im();
			y_high = tl.im() < br.im() ? br.im() : tl.im();
		}
	}

}
