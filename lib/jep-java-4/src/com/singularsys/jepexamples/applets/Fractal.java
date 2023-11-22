/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
/*
<applet code="org.nfunk.jepexamples.Fractal" width=300 height=320>
<param name=initialExpression value="z*z+c">
</applet>
 */
package com.singularsys.jepexamples.applets;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Applet/Application which calculates fractiles using a multi threaded system.
 * The formula define the fractile uses complex numbers
 * the default is <i>z =</i> <code>z^2 + c</code>, the <code>c</code> value is given by the coordinates
 * of the individual pixels, and the color depends on the number of iterations it takes before 
 * the value diverges. The equation of the fractile can be changed, but must only use the 
 * varibles <code>z</code> and <code>c</code>.
 * The formula for colors can also be changed. The default is
 * <code>[ (itts % 10)/10 ,( itts % 100)/100, (itts % maxItts)/ maxItts ]</code>
 * the result of evaluation should be a vector with three item which values between 0 and 1.
 * The formula for colors can contain <code>z</code> and <code>c</code> as well as
 * <code>itts</code> the number of itts before divergence <code>maxItts</code> the maximum number of 
 * itterations specifed. This value is set in a field in the main window. For example
 * <code>if(itts==maxItts, [0,0,0], [ (re(z) + 4)/8, (4-re(z))/8, (im(z)+4)/8] )</code>
 * colors by the position of z after the itteration, or black if it did not converge.
 * <p>
 * The default region shown is (-2,-2) .. (2,2). Double clicking zooms in centered on the mouse position,
 * right clicking zooms out, and dragging the mouse shifts the domain.
 * <p>
 * For details on the how the thread are implement see the {@link FractalCanvas} class.
 * See <a href="../../../../../html/threads.html">main thread documentation</a>.
 * 
 */
public class Fractal extends JApplet implements ActionListener {
    private static final long serialVersionUID = -1825231934586941116L;
    private JTextField exprField, itField;
    private FractalCanvas complexCanvas;
    private JTextField statusField;
    private JTextField colorField;
	private JTextField progressField;


    /** Initializes the applet Fractal */
    @Override
    public void init () {
        initComponents();
    }


    private void initComponents () {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;

        // Expression field
        String expr=null;
        if (expr==null) expr = "z*z+c";
        exprField = new JTextField(expr);

        exprField.setBackground (java.awt.Color.white);
        exprField.setName ("exprField");
        exprField.setFont (new Font ("Dialog", 0, 11));
        exprField.setForeground (Color.black);
        exprField.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent evt) {
                exprFieldTextValueChanged (evt);
            }
        }
        );
        c.gridx = 0;
        c.gridy = 0;
        
        c.weightx = 0;
        add(new JLabel("Formula"),c);
        
        c.gridx = 1;
        c.weightx = 1;
        gridbag.setConstraints(exprField, c);
        add(exprField);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.2;
        add(new JLabel("Itterations"));
 
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        add(new JLabel("Color formula"),c);
        
        c.gridx = 1;
        c.weightx = 1;
        
        colorField = new JTextField("[ (itts % 10)/10 ,( itts % 100)/100, (itts % maxItts)/ maxItts ]");
        add(colorField,c);
        colorField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				colorFieldTextValueChanged(e);
			}});
        
        
        c.gridx = 2;
        c.weightx = 0.2;

        // Iterations field
        itField = new JTextField("200");
        itField.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent evt) {
                itFieldTextValueChanged(evt);
            }
        }
        );

        gridbag.setConstraints(itField, c);
        add(itField);
        
        // CANVAS
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.weighty = 1;
        //		button2 = new Button("test");

        complexCanvas = new FractalCanvas(expr,this);
        gridbag.setConstraints(complexCanvas, c);
        add(complexCanvas);
        
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weighty = 0;
        statusField = new JTextField("Double click to zoom in, right click to zoom out");
        add(statusField,c);
 
        c.gridx = 2;
        c.weightx = 0;
        progressField = new JTextField("Progress");
        add(progressField,c);
        
        this.exprFieldTextValueChanged(null);
        colorFieldTextValueChanged(null);
    }

    public void showStatus(String line) {
    	this.statusField.setText(line);
    }

    public void showProgress(String line) {
    	this.progressField.setText(line);
    }

    /**
	 * @param evt  
	 */
    void exprFieldTextValueChanged (ActionEvent evt) {
        String newExpressionString = exprField.getText();
        boolean flag = complexCanvas.setExpressionString(newExpressionString);
        exprField.setForeground( flag ? Color.black : Color.red);
        if(flag) complexCanvas.render();
    }

    void colorFieldTextValueChanged (ActionEvent evt) {
        String newExpressionString = colorField.getText();
        boolean flag = complexCanvas.setColorExpression(newExpressionString);
        colorField.setForeground( flag ? Color.black : Color.red);
        if(flag) complexCanvas.render();
    }

    /**
	 * @param evt  
	 */
    void itFieldTextValueChanged (ActionEvent evt) {
        Integer newIterationsValue = Integer.valueOf(itField.getText());
        complexCanvas.setIterations(newIterationsValue.intValue());
        complexCanvas.render();
    }

    @Override
	public void actionPerformed(ActionEvent ae) {
        String str = ae.getActionCommand();
        if (str.equals("Render")) {
            String newExpressionString = exprField.getText();
            complexCanvas.setExpressionString(newExpressionString);
            complexCanvas.render();
        }
        if (str.equals("ZoomOut")) {
            complexCanvas.zoomOut();
        }
    }
    
    public static void main(String argv[]) {
    	JFrame f = new JFrame();
    	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	f.setSize(600, 400);
    	Fractal pp = new Fractal();
    	f.add(pp);
    	pp.init();
//        f.pack();
    	f.setVisible(true);
    }

}
