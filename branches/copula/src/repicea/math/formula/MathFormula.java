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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import repicea.math.formula.ExpressionHandler.DoubleHandler;
import repicea.math.formula.ExpressionHandler.MathFormulaHandler;

/**
 * The MathFormula class translates a String expression in a tractable mathematical formula.
 * @author Mathieu Fortin - February 2013
 */
public final class MathFormula implements Calculable {
	
	private final static char DOT = new String(".").charAt(0); 
		
	private String formula;
	private List<String> parameters;
	private List<String> variables;
	
	private Map<String, MathFormula> nestedMathFormulas;
	private Map<String, Double> constants;
	private Calculable finalCalculable;
	
	/**
	 * Constructor.
	 * @param formula a String that represents the formula
	 * @param parameters a List of String instances which represent the parameters
	 * @param variables a List of String instances which represent the variables
	 */
	public MathFormula(String formula, List<String> parameters, List<String> variables) {
		if (formula == null) {
			throw new InvalidParameterException("The formula in this MathFormula instance is null!");
		}
		
		nestedMathFormulas = new HashMap<String, MathFormula>();
		constants = new HashMap<String, Double>();
		
		this.formula = formula;
		if (parameters != null) {
			this.parameters = parameters;
		} else {
			this.parameters = new ArrayList<String>();
		}
		if (variables != null) {
			this.variables = variables;
		} else {
			this.variables = new ArrayList<String>();
		}

		parseParentheses();
		checkParametersAndVariables();
		defineOperators();
	}

	
	private void defineOperators() {
		String formula = this.formula;
		StringTokenizer tkz = new StringTokenizer(formula, "*/+-^");
		
		String leftPart = tkz.nextToken().trim();
		ExpressionHandler<?> leftExpression = getAppropriateExpressionHandler(leftPart);
		
		String rightPart;
		ExpressionHandler<?> rightExpression;
		MathOperator currentMathOperator;
		MathOperator lastMathOperator = null;
		
		while (tkz.hasMoreTokens()) {
			
			rightPart = tkz.nextToken().trim();
			rightExpression = getAppropriateExpressionHandler(rightPart);
			
			int index0 = formula.indexOf(leftPart) + leftPart.length();
			int index1 = formula.indexOf(rightPart);
			String currentOperator = formula.substring(index0, index1).trim();
			
			formula = formula.substring(formula.indexOf(currentOperator) + 1);
			currentMathOperator = MathOperator.getAppropriateMathOperator(currentOperator);
			
			if (lastMathOperator != null) {
				if (lastMathOperator.getPriority() >= currentMathOperator.getPriority()) {
					currentMathOperator.setLeftSide(lastMathOperator);
					currentMathOperator.setRightSide(rightExpression);
					finalCalculable = currentMathOperator;
				} else {
					lastMathOperator.setRightSide(currentMathOperator);
					currentMathOperator.setLeftSide(leftExpression);
					currentMathOperator.setRightSide(rightExpression);
				}
			} else {
				finalCalculable = currentMathOperator;
				currentMathOperator.setLeftSide(leftExpression);
				currentMathOperator.setRightSide(rightExpression);
			}
			lastMathOperator = currentMathOperator;
			leftPart = rightPart;
			leftExpression = rightExpression;
		}
		
		if (finalCalculable == null) {		// it means there was no operator at all in the formula e.g. ($nf3)
			finalCalculable = leftExpression;
		}

	}

	
	private ExpressionHandler<?> getAppropriateExpressionHandler(String expression) {
		if (constants.containsKey(expression)) {
			return new DoubleHandler(constants.get(expression));
		} else if (nestedMathFormulas.containsKey(expression)) {
			return new MathFormulaHandler(nestedMathFormulas.get(expression));
		} else {
			return null;		// TODO fix that when everything is working
		}
	}
	
	private void replaceInFormula(String str, String id) {
		int startIndex = formula.indexOf(str);
		int endIndex = startIndex + str.length();
		String newFormula = formula.substring(0,startIndex) + id + formula.substring(endIndex);
		formula = newFormula;
	}
	
	
	/**
	 * This method makes nested math formulas from expressions in parentheses.
	 */
	private void parseParentheses() {
		int firstOccurrence;
		int firstOccurrenceClosing;
		while ((firstOccurrence = formula.indexOf("(")) != -1) {
			firstOccurrenceClosing = formula.indexOf(")");
			if (firstOccurrenceClosing < firstOccurrence) {
				throw new InvalidParameterException("The closing parenthesis comes before the opening one in the formula!");
			} 
			int numberOfOpeningParentheses = 0;
			Character charact;
			for (int i = firstOccurrence + 1; i < formula.length(); i++) {
				charact = formula.charAt(i);
				if (charact.toString().equals("(")) {
					numberOfOpeningParentheses++;
				}
				if (charact.toString().equals(")")) {
					if (numberOfOpeningParentheses == 0) {
						String nestedFormula = formula.substring(firstOccurrence + 1, i);
						MathFormula nestedMathFormula = new MathFormula(nestedFormula, parameters, variables);
						String nestedFormulaID = "$nf" + nestedMathFormulas.size();
						nestedMathFormulas.put(nestedFormulaID, nestedMathFormula);
						String newFormula = formula.substring(0, firstOccurrence) + nestedFormulaID + formula.substring(i + 1);
						formula = newFormula;
						break;
					} else {
						numberOfOpeningParentheses--;
					}
				}
			}
			if (numberOfOpeningParentheses != 0) {
				throw new InvalidParameterException("The number of closing parentheses does not match the number of opening parentheses!");
			}
		}
		firstOccurrenceClosing = formula.indexOf(")");
		if (firstOccurrenceClosing != -1) {	
			throw new InvalidParameterException("Either the opening or closing parenthesis is missing!");
		} 
	}


	
	@Override
	public String toString() {
		String originalFormula = formula;
		MathFormula nestedFormula;
		for (String key : nestedMathFormulas.keySet()) {
			nestedFormula = nestedMathFormulas.get(key);
			originalFormula = originalFormula.replace(key, "(" + nestedFormula.toString() + ")");
		}
		for (String key : constants.keySet()) {
			double d = constants.get(key);
			originalFormula = originalFormula.replace(key, ((Double) d).toString());
		}
		return originalFormula;
	}
	
	

	private void checkParametersAndVariables() {
		List<String> constantList = new ArrayList<String>();
		StringTokenizer tkz = new StringTokenizer(formula, "+-*/^");
		String token;
		while (tkz.hasMoreTokens()) {
			token = tkz.nextToken().trim();
			if (!token.startsWith("$")) {
				if (isNumeric(token)) {
					constantList.add(token);
				} else {
					if (!parameters.contains(token) && !variables.contains(token)) {
						throw new InvalidParameterException("The expression " + token + " has not been defined as parameter or variable!");
					}
				}
			}
		}
		
		for (String constant : constantList) {
			registerConstant(constant);
		}
		
	}
	
	private void registerConstant(String token) {
		String constantID = "$c" + constants.size();
		constants.put(constantID, Double.parseDouble(token));
		replaceInFormula(token, constantID);
	}

	private boolean isNumeric(String str) {
		boolean hasDot = false;
		char c;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			if (!Character.isDigit(c)) {
				if (DOT != c) {
					return false;
				} else {
					if (hasDot) {
						return false;
					} else {
						hasDot = true;
					}
				
				}
			}
		}
		return true;
	}
	

	@Override
	public double calculate() {
		return finalCalculable.calculate();
	}

	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		List<String> parameters = new ArrayList<String>();
		parameters.add("alpha");
		MathFormula mathFormula = new MathFormula("2^(2 + 3)", parameters, null);
		double results = mathFormula.calculate();
		System.out.println(mathFormula.toString());
	}


}
