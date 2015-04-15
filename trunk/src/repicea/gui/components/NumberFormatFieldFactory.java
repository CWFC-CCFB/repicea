/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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
package repicea.gui.components;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.EventObject;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import repicea.gui.CommonGuiUtility;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;

/**
 * The NumberFormatFieldFactory class provides JTextField instances specially designed to 
 * receive numbers. The Document instance in the JTextField does not update if the 
 * inserted text does not comply with the rules set in the nested class NumberFormatter. 
 * Whenever, the text is changed successfully, the Document instance 
 * within the JTextField fires a NumberFieldDocumentEvent instance. 
 * The event can be listened to by adding a NumberFieldListener to the JFormattedNumericField instance.
 * @author Mathieu Fortin - February 2012
 */
@SuppressWarnings("serial")
public class NumberFormatFieldFactory {

	/**
	 * This interface ensures the listener can deal with NumberFieldEvent instances as soon as they are fired by the JFormattedNumericField instance.
	 * @author Mathieu Fortin - August 2013
	 */
	public static interface NumberFieldListener {
		
		/**
		 * This method deals with the event fired by the JFormattedNumericField instance.
		 * @param e a NumberFieldEvent instance
		 */
		public void numberChanged(NumberFieldEvent e);
		
	}
	
	
	/**
	 * The JFormattedNumericField class is a special JFormattedTextField case where input values are
	 * either Double or Integer. The method getValue() returns the text in the appropriate format, i.e. as a Double
	 * or as an Integer. If nulls are allowed and the text is empty, getValue() returns 0. Note this class cannot
	 * be instantiated. Instances can be obtained through the static method createNumberFormatField.
	 * @author Mathieu Fortin - January 2013
	 */
	public static class JFormattedNumericField extends JFormattedTextField {
		
		private CopyOnWriteArrayList<NumberFieldListener> listeners;	
		
		private Type type;
		
		private JFormattedNumericField(Type type) {
			super();
			listeners = new CopyOnWriteArrayList<NumberFieldListener>();
			this.type = type;
		}

		private JFormattedNumericField(JFormattedTextField.AbstractFormatter formatter) {
			super();
		}
		
		private JFormattedNumericField(JFormattedTextField.AbstractFormatterFactory factory) {
			super();
		}
		
		private JFormattedNumericField(JFormattedTextField.AbstractFormatterFactory factory, Object currentValue) {
			super();
		}
		
		private JFormattedNumericField(Object value) {
			super();
		}

		
		@Override
		public Number getValue() {
			if (getText() == null || getText().isEmpty()) {
				if (type == Type.Double) {
					return 0d;
				} else {
					return (int) 0;
				}
			} else {
				if (type == Type.Double) {
					return Double.parseDouble(getText());
				} else {
					return Integer.parseInt(getText());
				}
			}
		}
		
		@Override
		public void setDocument(Document doc) {
			super.setDocument(doc);
			if (doc instanceof NumberFieldDocument) {
				((NumberFieldDocument) doc).setOwner(this);
			}
		}

		protected void fireNumberFieldEvent(NumberFieldEvent event) {
			for (NumberFieldListener listener : listeners) {
				listener.numberChanged(event);
			}
		}
		
		/**
		 * This method adds a listener from the list of listener if it is not already contained in the list.
		 * @param listener a NumberFieldListener instance
		 */
		public void addNumberFieldListener(NumberFieldListener listener) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
		
		/**
		 * This method removes a listener from the list of listener if it is already contained in the list.
		 * @param listener a NumberFieldListener instance
		 */
		public void removeNumberFieldListener(NumberFieldListener listener) {
			if (listeners.contains(listener)) {
				listeners.remove(listener);
			}
		}
		
		
	}
	
	
	
	
	public enum Range {
		/**
		 * May include 0.
		 */
		Negative, 
		/**
		 * Does not include 0.
		 */
		StrictlyNegative,
		/**
		 * May include 0.
		 */
		Positive,
		/**
		 * Does not include 0.
		 */
		StrictlyPositive,
		All
	}
	
	public enum Type {
		Integer,
		Double
	}
	
	
	/**
	 * This class overrides method in the PlainDocument class in order to format the JTextField to numbers.
	 * @author Mathieu Fortin - February 2012
	 */
	public static class NumberFieldDocument extends PlainDocument {

		public static class NumberFieldEvent extends EventObject {

			private EventType type;
			
			private NumberFieldEvent(JFormattedNumericField field, 
					EventType type) {
				super(field);
				this.type = type;
			}

			public EventType getType() {return type;}
		}
		
				
		private boolean nullAllowed;
		private NumberFormatter numberFormatter;
		private boolean isReplacing;
		private JFormattedNumericField owner;
		
		private NumberFieldDocument(NumberFormatter numberFormatter) {
			this.numberFormatter = numberFormatter;
		}
		
		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			String currentText = getText(0, getLength());
			String newString = currentText.substring(0, offs) + 
					str +
					currentText.substring(offs, currentText.length());
			try {
				if (!newString.trim().isEmpty() || !nullAllowed) {
					numberFormatter.stringToValue(newString);
				}
				super.insertString(offs, str, a);
				owner.fireNumberFieldEvent(new NumberFieldEvent(owner, EventType.INSERT));
			} catch (Exception e) {
				return;
			}
		}
		
		@Override
		public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			isReplacing = true;
			String currentText = getText(0, getLength());
			String newString = currentText.substring(0, offset) +
					text +
					currentText.substring(offset + length, getLength());
			try {
				if (!newString.trim().isEmpty() || !nullAllowed) {
					numberFormatter.stringToValue(newString);
				}
				super.replace(offset, length, text, attrs);
				owner.fireNumberFieldEvent(new NumberFieldEvent(owner, EventType.CHANGE));
			} catch (Exception e) {
				return;
			} finally {
				isReplacing = false;
			}
		}
		
		@Override
		public void remove(int offs, int len) throws BadLocationException {
			if (isReplacing) {
				super.remove(offs, len);
			} else {
				String currentText = getText(0, getLength());
				String newString = currentText.substring(0, offs) +
						currentText.substring(offs + len, getLength());
				try {
					if (!newString.trim().isEmpty() || !nullAllowed) {
						numberFormatter.stringToValue(newString);
					}
					super.remove(offs, len);
					owner.fireNumberFieldEvent(new NumberFieldEvent(owner, EventType.REMOVE));
				} catch (Exception e) {
					return;
				}
			}
		}
		
				
		private void setOwner(JFormattedNumericField field) {
			owner = field;
		}
		
	}
	
	
	
	private static class NumberFormatter extends javax.swing.JFormattedTextField.AbstractFormatter {
		
		private NumberFormatFieldFactory.Range range;
		private NumberFormatFieldFactory.Type type;
		
		private NumberFormatter(Type type, Range range) {
			if (type == null) {
				throw new InvalidParameterException("The parameter type cannot be set to null!");
			}
			if (range == null) {
				throw new InvalidParameterException("The parameter range cannot be set to null!");
			}
			this.range = range;
			this.type = type;
		}
		
		
		@Override
		public Object stringToValue(String text) throws ParseException {
			for (char c : text.toCharArray()) {
				if (Character.isLetter(c)) {
					throw new ParseException("Error while parsing " + text, 0);
				}
			}
			Object d = null;
			try {
				switch(type) {
				case Integer:
					d = Integer.parseInt(text);
					break;
				case Double:
					d = Double.parseDouble(text);
					break;
				}
				d = check((Number) d);
			} catch (Exception e) {
				throw new ParseException("Error while parsing " + text, 0);
			}
			return d;
		}

		@SuppressWarnings("incomplete-switch")
		private Number check(Number d) throws ParseException {
			switch(range) {
			case Negative:
				if (d.doubleValue() > 0) {
					throw new ParseException("Number " + d.toString() + " is positive whereas the range was set to " + range.name(),0);
				}
				break;
			case StrictlyNegative:
				if (d.doubleValue() >= 0) {
					throw new ParseException("Number " + d.toString() + " is positive whereas the range was set to " + range.name(),0);
				}
				break;
			case Positive:
				if (d.doubleValue() < 0) {
					throw new ParseException("Number " + d.toString() + " is positive whereas the range was set to " + range.name(),0);
				}
				break;
			case StrictlyPositive:
				if (d.doubleValue() <= 0) {
					throw new ParseException("Number " + d.toString() + " is positive whereas the range was set to " + range.name(), 0);
				}
				break;
			}
			return d;
		}


		@Override
		public String valueToString(Object value) throws ParseException {
			if (value == null) {
				throw new ParseException("The value is null!", 0);
			} else {
				return value.toString();
			}
		}

	}

	/**
	 * This method returns JFormattedNumericField instances with some bounds and number type (integer or double).
	 * @param type a Type enum instance (Integer or Double)
	 * @param range a Range enum instance (Negative, StrictlyNegative, Positive, StrictlyPositive, All)
	 * @return a JFormattedTextField instance
	 */
	public static JFormattedNumericField createNumberFormatField(Type type, Range range, boolean nullAllowed) {
		return createNumberFormatField(0, type, range, nullAllowed);
	}


	/**
	 * This method returns JFormattedNumericField instances with some bounds and number type (integer or double).
	 * @param numberOfColumns the number of columns in the text field
	 * @param type a Type enum instance (Integer or Double)
	 * @param range a Range enum instance (Negative, StrictlyNegative, Positive, StrictlyPositive, All)
	 * @return a JFormattedTextField instance
	 */
	public static JFormattedNumericField createNumberFormatField(int numberOfColumns, Type type, Range range, boolean nullAllowed) {
		NumberFormatter formatter = new NumberFormatter(type, range);
		NumberFieldDocument document = new NumberFieldDocument(formatter);
		document.nullAllowed = nullAllowed;
		JFormattedNumericField field = new JFormattedNumericField(type);
		if (numberOfColumns > 0) {
			CommonGuiUtility.setNumberOfColumns(field, numberOfColumns);
		} else if (numberOfColumns < 0) {
			throw new InvalidParameterException("The number of columns must be equal to or greater than 0!");
		}
		field.setHorizontalAlignment(SwingConstants.RIGHT);
		field.setDocument(document);
		return field;
	}

}
