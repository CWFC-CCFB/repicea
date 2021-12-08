/*
 * This file is part of the repicea-statistics library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.math.formula;

/**
 * The MathOperator class is an abstract class for all operators. Basically, an operator has
 * a left side and a right side component as well as a priority. 
 * @author Mathieu Fortin - May 2013
 */
abstract class MathOperator implements Calculable {

	protected int priority;
	protected Calculable leftSide;
	protected Calculable rightSide;

	protected void setLeftSide(Calculable leftSide) {
		this.leftSide = leftSide;
	}
	
	protected void setRightSide(Calculable rightSide) {
		this.rightSide = rightSide;
	}
	
	protected int getPriority() {
		return priority;
	}
	
	/**
	 * This method returns the appropriate mathematical operator.
	 * @param operator a symbol e.g. / * + -
	 * @return a MathOperator instance
	 */
	protected static MathOperator getAppropriateMathOperator(String operator) {
		if (operator.equals("*")) {
			return new MathOperator.Multiply();
		} else if (operator.equals("/")) {
			return new MathOperator.Divide();
		} else if (operator.equals("+")) {
			return new MathOperator.Plus();
		} else if (operator.equals("-")) {
			return new MathOperator.Minus();
		} else if (operator.equals("^")) {
			return new MathOperator.Power();
		} else {
			return null;
		}
	}

	
	
	/**
	 * A classical plus "+" operator.
	 * @author Mathieu Fortin - May 2013
	 */
	static class Plus extends MathOperator {
		
		protected Plus() {
			priority = 0;
		}
		
 		@Override
		public double calculate() {
			return leftSide.calculate() + rightSide.calculate();
		}
 		
	}

	/**
	 * A classical minus "-" operator.
	 * @author Mathieu Fortin - May 2013
	 */
	static class Minus extends MathOperator {
		
		protected Minus() {
			priority = 0;
		}
		
 		@Override
		public double calculate() {
			return leftSide.calculate() - rightSide.calculate();
		}
 		
	}

	/**
	 * A classical multiply "*" operator.
	 * @author Mathieu Fortin - May 2013
	 */
	static class Multiply extends MathOperator {
		
		protected Multiply() {
			priority = 1;
		}

		@Override
		public double calculate() {
			return leftSide.calculate() * rightSide.calculate();
		}
		
	}

	/**
	 * A classical divide "/" operator.
	 * @author Mathieu Fortin - May 2013
	 */
	static class Divide extends MathOperator {
		
		protected Divide() {
			priority = 1;
		}

		@Override
		public double calculate() {
			return leftSide.calculate() / rightSide.calculate();
		}
		
	}
	
	/**
	 * A classical power "^" operator.
	 * @author Mathieu Fortin - May 2013
	 */
	static class Power extends MathOperator {
		
		protected Power() {
			priority = 2;
		}

		@Override
		public double calculate() {
			return Math.pow(leftSide.calculate(), rightSide.calculate());
		}
	}
	
}
