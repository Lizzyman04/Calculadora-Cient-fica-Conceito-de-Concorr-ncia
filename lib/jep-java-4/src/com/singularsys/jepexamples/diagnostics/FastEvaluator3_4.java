/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 

package com.singularsys.jepexamples.diagnostics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Stack;

import com.singularsys.jep.Evaluator;
import com.singularsys.jep.Jep;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParserVisitor;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.Variable;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.CallbackEvaluationI;
import com.singularsys.jep.functions.NaryBinaryFunction;
import com.singularsys.jep.functions.NaryFunction;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.parser.ASTConstant;
import com.singularsys.jep.parser.ASTFunNode;
import com.singularsys.jep.parser.ASTOpNode;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.parser.JccParserTreeConstants;

public class FastEvaluator3_4 implements Evaluator, ParserVisitor {
    private transient Stack<Object> stack = new Stack<Object>();

    /** Whether null values for variables are trapped 
     * @serial
     **/ 
    private boolean trapNullValues=true;
    private boolean trapNaN=false;
    private boolean trapInfinity=false;


    public void init(Jep jep) { /* do nothing */ }

    public Object eval(Node node) throws EvaluationException {
        Object res=null;
        //res = node.jjtAccept(this, null);
        res = nodeAccept(node);
        return res;
    }

    public Object evaluate(Node node) throws EvaluationException {
        stack.clear();
        Object res=null;
        // attempt to evaluate the expression
        //res = node.jjtAccept(this, null);
        res = nodeAccept(node);
        // Stack should only have a single item on it
        if (stack.size() != 0) 
            throw new EvaluationException("Stack corrupted");
        // Stack only has a single item, so pop it and return it
        return res;
    }

    // TODO minor speedup by using this method
    protected Object nodeAccept(Node node) throws EvaluationException {
        switch(node.getId())
        {
        case JccParserTreeConstants.JJTOPNODE: 
            return visitFun(node);
        case JccParserTreeConstants.JJTVARNODE: 
            return visitVar(node);
        case JccParserTreeConstants.JJTFUNNODE: 
            return visitFun(node);
        case JccParserTreeConstants.JJTCONSTANT: 
            return visitConstant(node);
        }
        try {
            return node.jjtAccept(this, null);
        } catch(EvaluationException e) { 
            throw e; 
        } catch (JepException e) {
            throw new EvaluationException(e);
        }
    }
    /*	    if(node instanceof ASTConstant) 
		visit((ASTConstant)node,null);
	    else if(node instanceof ASTVarNode)
		visit((ASTVarNode)node,null);
	    else visitFun(node);
	}
/**/
    public Object visit(ASTConstant node, Object data) throws EvaluationException {
        return visitConstant(node);
    }
    public Object visitConstant(Node node) throws EvaluationException {
        Object o = node.getValue();
        if(this.trapNaN) {
            if (  (o instanceof Double && ((Double) o).isNaN())
                    ||(o instanceof Float && ((Float) o).isNaN()) )
                throw new EvaluationException("NaN constant value detected");
        }
        if(this.trapInfinity) {
            if (  (o instanceof Double && ((Double) o).isInfinite())
                    ||(o instanceof Float && ((Float) o).isInfinite())
            )
                throw new EvaluationException("Infinite constant value "+ o.toString()+"detected");
        }
        return o;
    }

    public Object visit(ASTVarNode node, Object data) throws EvaluationException {
        return visitVar(node);
    }

    public Object visitVar(Node node) throws EvaluationException {

        Variable var = node.getVar();
        assert var!=null;

        // get the variable value
        Object temp = var.getValue();

        if (trapNullValues && temp == null) {
            String message = "Could not evaluate " + node.getName() + ": no value set for the variable. See com.singularsys.jep.standard.FastEvaluator.setTrapNullValues(boolean).";
            throw new EvaluationException(message);
        }
        if(this.trapNaN) {
            if (  (temp instanceof Double && ((Double) temp).isNaN())
                    ||(temp instanceof Float && ((Float) temp).isNaN()) )
                throw new EvaluationException("NaN value detected for variable "+node.getName()+". See com.singularsys.jep.standard.FastEvaluator.setTrapNaN(boolean).");
        }
        if(this.trapInfinity) {
            if (  (temp instanceof Double && ((Double) temp).isInfinite())
                    ||(temp instanceof Float && ((Float) temp).isInfinite())
            )
                throw new EvaluationException("Infinite value "+ temp.toString()+"detected for variable "+node.getName()+". See com.singularsys.jep.standard.FastEvaluator.setTrapInfinity(boolean).");
        }
        // all is fine
        return temp;
    }

    public Object visit(ASTFunNode node, Object data) throws EvaluationException {
        return visitFun(node);
    }

    public Object visit(ASTOpNode node, Object data) throws EvaluationException {
        return visitFun(node);
    }

    /**
     * Visits a function/operator node. This is the most visited method for most
     * expression evaluations. Keeping it fast is important.
     * @param node
     * @throws EvaluationException
     */
    protected Object visitFun(Node node) throws EvaluationException {
        PostfixMathCommandI pfmc = node.getPFMC();
        int nchild = node.jjtGetNumChildren();
        Object res;

        if (pfmc == null)
            throw new EvaluationException(
                    "No function class associated with " + node.getName());

        if (pfmc instanceof CallbackEvaluationI) {
            res = ((CallbackEvaluationI) pfmc).evaluate(node, this);
        }
        else if(pfmc instanceof UnaryFunction) {
            if(nchild != 1) throw new EvaluationException(node.getName()+": incorected number of children "+nchild+" expecting 1");
            Object cval = nodeAccept(node.jjtGetChild(0));
            res = ((UnaryFunction) pfmc).eval(cval);
        }
        else if(pfmc instanceof BinaryFunction) {
            if(nchild != 2) throw new EvaluationException(node.getName()+": incorected number of children "+nchild+" expecting 1");
            Object lval = nodeAccept(node.jjtGetChild(0));
            Object rval = nodeAccept(node.jjtGetChild(1));
            res = ((BinaryFunction) pfmc).eval(lval,rval);
        }
        else if(pfmc instanceof NaryBinaryFunction) {
            switch(nchild) {
            case 0:
                throw new EvaluationException(node.getName()+": incorected number of children "+nchild+" must be >0");
            case 1:
                res = nodeAccept(node.jjtGetChild(0));
                break;
            case 2:
                Object lval = nodeAccept(node.jjtGetChild(0));
                Object rval = nodeAccept(node.jjtGetChild(1));
                res = ((NaryBinaryFunction) pfmc).eval(lval,rval);
                break;
            default:
            	// get the number of children
            	Object[] cvals = new Object[nchild];
            	// loop through each child
            	for (int i=0; i<nchild; ++i) {
            		Node child = node.jjtGetChild(i);
            		cvals[i] = nodeAccept(child);
            	}
            	pfmc.setCurNumberOfParameters(nchild);
            	res = ((NaryBinaryFunction) pfmc).eval(cvals);
            }
        }
        else if(pfmc instanceof NaryFunction) {
            if (!pfmc.checkNumberOfParameters(nchild))
                throw new EvaluationException(node.getName() + "Incorrect number of children "+nchild+". " +
                        (pfmc.getNumberOfParameters() >0 ? "Expected "+pfmc.getNumberOfParameters() : ""));
            Object[] args = new Object[nchild];
            for (int i=0; i<nchild; ++i) {
                Node child = node.jjtGetChild(i);

                //nodeAccept(child);
                args[i] = nodeAccept(child);
            }
            pfmc.setCurNumberOfParameters(nchild);
            res = ((NaryFunction) pfmc).eval(args);
        }
        else {
            // check whether the number of parameters is correct
            if (!pfmc.checkNumberOfParameters(nchild))
                throw new EvaluationException(node.getName() + "Incorrect number of children "+nchild+". " +
                        (pfmc.getNumberOfParameters() >0 ? "Expected "+pfmc.getNumberOfParameters() : ""));
            // evaluate all the children
            Object cval=null;
            for (int i=0; i<nchild; ++i) {
                Node child = node.jjtGetChild(i);

                cval = nodeAccept(child);
                stack.push(cval);
            }
            // set the number of parameters for this node
            pfmc.setCurNumberOfParameters(nchild);
            // run the function on the stack
            pfmc.run(stack);
            res = stack.pop();
        }

        if (trapNullValues && res == null) {
            throw new EvaluationException("Null value detected for result of function/operator "+node.getName());
        }
        if(this.trapNaN) {
            if (  (res instanceof Double && ((Double) res).isNaN())
                    ||(res instanceof Float && ((Float) res).isNaN()) )
                throw new EvaluationException("NaN value detected for result of function/operator "+node.getName());
        }
        if(this.trapInfinity) {
            if (  (res instanceof Double && ((Double) res).isInfinite())
                    ||(res instanceof Float && ((Float) res).isInfinite())
            )
                throw new EvaluationException("Infinite value "+ res.toString()+"detected for result of function/operator "+node.getName());
        }
        return res;
    }

    /**
     * Whether null values for variables are trapped.
     * @return the status if the trap null values flag.
     */
    public boolean isTrapNullValues() {
        return trapNullValues;
    }

    /**
     * Sets whether null values for variables are trapped.
     * If set (the default) then an EvaluationException is 
     * thrown for null values of variables.
     * If not set then null values are passed to PostfixMathCommands
     * who will need to test for null values.
     * @param trapNullValues
     */
    public void setTrapNullValues(boolean trapNullValues) {
        this.trapNullValues = trapNullValues;
    }

    public boolean isTrapNaN() {
        return trapNaN;
    }

    public void setTrapNaN(boolean trapNaN) {
        this.trapNaN = trapNaN;
    }

    public boolean isTrapInfinity() {
        return trapInfinity;
    }

    public void setTrapInfinity(boolean trapInfinity) {
        this.trapInfinity = trapInfinity;
    }

    /**
     * @return an new FastEvaluator
     */
    public JepComponent getLightWeightInstance() {
        FastEvaluator3_4 se = new FastEvaluator3_4();
        se.trapNullValues = this.trapNullValues;
        se.trapNaN = this.trapNaN;
        se.trapInfinity = this.trapInfinity;
        return se;
    }

    private static final long serialVersionUID = 300L;
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        stack = new Stack<Object>();
    }

}
