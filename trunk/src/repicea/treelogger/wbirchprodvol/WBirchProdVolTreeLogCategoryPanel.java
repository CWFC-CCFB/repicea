/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package repicea.treelogger.wbirchprodvol;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.simulation.treelogger.LogCategoryPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class WBirchProdVolTreeLogCategoryPanel extends LogCategoryPanel<WBirchProdVolTreeLogCategory> {

	protected static enum MessageID implements TextableEnum {
		MaximumDecayDiameter("Maximum decay diameter (cm)", "Diam\u00E8tre maximum de carie (cm)"),
		EligibleLogGrade("Eligible log grades", "Grades de billes \u00E9ligibles");

		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	
	private final JTextField smallEndDiameterCmTextField;
	private final JTextField logLengthMTextField;
	private final JTextField eligibleLogGradeTextField;
	private final JTextField maximumDecayDiameterCmTextField;
	
	protected WBirchProdVolTreeLogCategoryPanel(WBirchProdVolTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setText(logCategory.getName());
		nameTextField.setEditable(false);
		smallEndDiameterCmTextField = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, true);	// null not allowed
		smallEndDiameterCmTextField.setEditable(false);
		logLengthMTextField = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, true);	// null allowed
		logLengthMTextField.setEditable(false);
		maximumDecayDiameterCmTextField = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, true);
		maximumDecayDiameterCmTextField.setEditable(false);
		eligibleLogGradeTextField = new JTextField();
		eligibleLogGradeTextField.setEditable(false);
		createUI();
	}

	private JPanel makePanel(Enum<?> labelEnum, JTextField field) {
		JPanel featurePanel1 = new JPanel();
		featurePanel1.setLayout(new BorderLayout(0, 0));
		
		JPanel labelPanel1 = new JPanel();
		FlowLayout fl_labelPanel1 = (FlowLayout) labelPanel1.getLayout();
		fl_labelPanel1.setAlignment(FlowLayout.LEFT);
		featurePanel1.add(labelPanel1, BorderLayout.CENTER);
		
		JLabel featureLabel1 = new JLabel(labelEnum.toString());
		featureLabel1.setFont(new Font("Arial", Font.PLAIN, 12));
		labelPanel1.add(featureLabel1);
		
		JPanel textFieldPanel1 = new JPanel();
		FlowLayout fl_textFieldPanel1 = (FlowLayout) textFieldPanel1.getLayout();
		fl_textFieldPanel1.setAlignment(FlowLayout.RIGHT);
		featurePanel1.add(textFieldPanel1, BorderLayout.EAST);
		
		field.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldPanel1.add(field);
		field.setColumns(10);
		
		return featurePanel1;
	}
	
	
	private void createUI() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(new BorderLayout(0, 0));
		
		JPanel logCategoryNamePanel = new JPanel();
		add(logCategoryNamePanel, BorderLayout.NORTH);
		FlowLayout flowLayout = (FlowLayout) logCategoryNamePanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		logCategoryNamePanel.add(horizontalStrut);
		
		JLabel nameLabel = new JLabel(LogCategoryPanel.MessageID.LogGradeName.toString());
		nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		logCategoryNamePanel.add(nameLabel);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		logCategoryNamePanel.add(horizontalStrut_1);
		
		nameTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		logCategoryNamePanel.add(nameTextField);
		nameTextField.setColumns(15);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(makePanel(MessageID.EligibleLogGrade, eligibleLogGradeTextField));
		
		panel.add(makePanel(LogCategoryPanel.MessageID.LongLength, logLengthMTextField));
		
		panel.add(makePanel(LogCategoryPanel.MessageID.SmallEndDiameter, smallEndDiameterCmTextField));

		panel.add(makePanel(MessageID.MaximumDecayDiameter, maximumDecayDiameterCmTextField));

		
	}

	
	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.gui.Refreshable#refreshInterface()
	 */
	@Override
	public void refreshInterface() {
		String eligibleLogGrade = getTreeLogCategory().eligibleLogGrade;
		if (eligibleLogGrade != null && !eligibleLogGrade.isEmpty()) {
			eligibleLogGradeTextField.setText(eligibleLogGrade);
		}
		logLengthMTextField.setText(((Double) getTreeLogCategory().lengthM).toString());
		Double smallEndDiameterCm = getTreeLogCategory().minimumSmallEndDiameterCm;
		if (smallEndDiameterCm != null) {
			smallEndDiameterCmTextField.setText(smallEndDiameterCm.toString());
		}
		Double maximumDecayDiameterCm = getTreeLogCategory().maximumDecayDiameterCm;
		if (maximumDecayDiameterCm != null) {
			maximumDecayDiameterCmTextField.setText(maximumDecayDiameterCm.toString());			
		}
	}


	
}

