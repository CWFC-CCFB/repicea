/*
 * This file is part of the repicea-console library.
 *
 * Copyright (C) 2012 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.console;


@Deprecated
final class SettingsDialog  {
//	extends REpiceaDialog implements OwnedWindow, ChangeListener, ListSelectionListener {
//}
//		
//	static {
//		UIControlManager.setFont(FontType.LabelFont, new Font("Tahoma", Font.BOLD, 11));
//	}
//	
//	public static class Language implements Serializable {
//		
//		public static final Language ENGLISH = new Language("English", "en");
//		public static final Language FRENCH = new Language("Fran\u00E7ais", "fr");
//		
//		private String longName;
//		private String capsisCode;
//		
//		private Language(String longName, String capsisCode) {
//			this.longName = longName;
//			this.capsisCode = capsisCode;
//		}
//		
//		@Override
//		public String toString() {return longName;}
//		public String getLanguageCode() {return capsisCode;}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if (obj instanceof Language) {
//				Language otherLanguage = (Language) obj;
//				if (longName.equals(otherLanguage.longName) && capsisCode.equals(otherLanguage.capsisCode)) {
//					return true;
//				}
//			} 
//			return false;
//		}
//	}
//	
//	
//	public static enum Encoding {
//		US_ASCII("US-ASCII"),
//		ISO_8859_1("ISO-8859-1"),
//		UTF_8("UTF-8");
//
//		String encodingName;
//		
//		Encoding(String encodingName) {
//			this.encodingName = encodingName;
//		}
//		
//		public String getEncodingName() {
//			return encodingName;
//		}
//		
//		@Override
//		public String toString() {
//			return getEncodingName();
//		}
//	}
//	
//	public static enum MessageID implements TextableEnum {
//		Title("Options", "Options"),
//		Language("Language", "Langue"),
//		JVMMemory1("JVM Maximum Memory (", "M\u00E9moire maximale de la JVM ("),
//		JVMMemory2(" MgBytes)", " MegaOctets)"),
//		EncodingLabel("Encoding", "Encodage");
//
//		MessageID(String englishText, String frenchText) {
//			setText(englishText, frenchText);
//		}
//		
//		@Override
//		public void setText(String englishText, String frenchText) {
//			REpiceaTranslator.setString(this, englishText, frenchText);
//		}
//
//		@Override
//		public String toString() {return REpiceaTranslator.getString(this);}
//	}
//	
//	
//	protected TriggerSettings settings;
//	
//	private JSlider slider;
//	private JLabel lblMemoryLabel;
//	private JList<Language> languageList;
////	private JButton ok;
////	private JButton cancel;
//	private JList<Encoding> encodingList;
//		
//	protected SettingsDialog(Frame parent, TriggerSettings settings) {
//		super(parent);
//		this.settings = settings;
//		this.cancelOnClose = false;
////		new REpiceaMemorizerHandler(this);
//		setResizable(false);
//		initUI();
//		pack();
////		ok.setFocusable(true);
////		ok.requestFocusInWindow();
//	}
//	
////	protected JPanel createControlPanel() {
////		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
////		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
////		controlPanel.add(ok);
////		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
////		controlPanel.add(cancel);
////		return controlPanel;
////	}
//	
//	
//	protected JPanel createMainPanel() {
//
//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//		
//		JPanel maxMemoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		panel.add(maxMemoryPanel);
//		
//		lblMemoryLabel = UIControlManager.getLabel("");
//		maxMemoryPanel.add(lblMemoryLabel);
//		
//		JPanel slideBarPanel = new JPanel();
//		panel.add(slideBarPanel);
//		slideBarPanel.setLayout(new BorderLayout(0, 0));
//		
//		Component horizontalStrut = Box.createHorizontalStrut(20);
//		slideBarPanel.add(horizontalStrut, BorderLayout.WEST);
//		
//		slider = new JSlider();
//		slider.setPaintTicks(true);
//		slider.setSnapToTicks(true);
//		slider.setMinorTickSpacing(128);
//		slider.setMinimum(256);
//		slider.setMaximum(2048);
//		slider.setMajorTickSpacing(256);
//		slider.setValue(settings.getMaxMemoryJVM());
//		updateMemoryLabel();
//		slider.setPaintLabels(true);
//		slideBarPanel.add(slider);
//		
//		Component verticalStrut = Box.createVerticalStrut(20);
//		panel.add(verticalStrut);
//		
//		JPanel languagePanel = new JPanel();
//		FlowLayout flowLayout_1 = (FlowLayout) languagePanel.getLayout();
//		flowLayout_1.setAlignment(FlowLayout.LEFT);
//		panel.add(languagePanel);
//		
//		JLabel lblNewLabel_1 = UIControlManager.getLabel(MessageID.Language);
////		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
//		languagePanel.add(lblNewLabel_1);
//		
//		JPanel menuPanel = new JPanel();
//		FlowLayout flowLayout_3 = (FlowLayout) menuPanel.getLayout();
//		flowLayout_3.setAlignment(FlowLayout.LEFT);
//		panel.add(menuPanel);
//		
//		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
//		menuPanel.add(horizontalStrut_1);
//		
//		Vector<Language> languages = new Vector<Language>();
//		languages.add(Language.ENGLISH);
//		languages.add(Language.FRENCH);
//		languageList = new JList<Language>(languages);
//		languageList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		languageList.setVisibleRowCount(4);
//		languageList.setLayoutOrientation(JList.VERTICAL_WRAP);
//		boolean matchFound = false;
//		int index = 0;
//		for (index = 0; index < languages.size(); index++) {
//			if (languages.get(index).equals(settings.getLanguage())) {
//				matchFound = true;
//				break;
//			}
//		}
//		if (!matchFound) {
//			languageList.setSelectedIndex(0);
//		} else {
//			languageList.setSelectedIndex(index);
//		}
//		languageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		menuPanel.add(languageList);
//		
//		setMinimumSize(new Dimension(400,300));
//		slider.setSize(300, 100);
//		
//		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
//		slideBarPanel.add(horizontalStrut_2, BorderLayout.EAST);
//		
//		Component verticalStrut_1 = Box.createVerticalStrut(20);
//		panel.add(verticalStrut_1);
//		
//		JPanel encodingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		panel.add(encodingPanel);
//		
//		@SuppressWarnings("unused")
//		JLabel lblNewLabel = UIControlManager.getLabel(MessageID.EncodingLabel);
//		
//		JPanel encodingMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		
//		Vector<Encoding> encodings = new Vector<Encoding>();
//		for (Encoding encoding : Encoding.values()) {
//			encodings.add(encoding);
//		}
//		encodingList = new JList<Encoding>(encodings);
//		encodingList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		encodingList.setVisibleRowCount(4);
//		encodingList.setLayoutOrientation(JList.VERTICAL_WRAP);
//		int selectedIndex = encodings.indexOf(settings.getEncoding());
//		if (selectedIndex < 0) {
//			selectedIndex = 0;
//		}
//		encodingList.setSelectedIndex(selectedIndex);
//		encodingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		
//		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
//		encodingMenuPanel.add(horizontalStrut_3);
//		encodingMenuPanel.add(encodingList);
//		
//		return panel;
//	}
//	
//	private void updateMemoryLabel() {
//		lblMemoryLabel.setText(REpiceaTranslator.getString(MessageID.JVMMemory1)
//				+ slider.getValue() 
//				+ REpiceaTranslator.getString(MessageID.JVMMemory2));
//	}
//
//	@Override
//	public void doNotListenToAnymore() {
//		slider.removeChangeListener(this);
//		languageList.removeListSelectionListener(this);
//		encodingList.removeListSelectionListener(this);
////		ok.removeActionListener(this);
////		cancel.removeActionListener(this);
//	}
//
//	@Override
//	public void listenTo() {
//		slider.addChangeListener(this);
//		languageList.addListSelectionListener(this);
//		encodingList.addListSelectionListener(this);
////		ok.addActionListener(this);
////		cancel.addActionListener(this);
//	}
//
//
//	@Override
//	public void stateChanged(ChangeEvent arg0) {
//		if (arg0.getSource().equals(slider)) {
//			settings.setMaxMemoryJVM(slider.getValue());
//			updateMemoryLabel();
//			validate();
//		}
//	}
//
//
////	@Override
////	public void actionPerformed(ActionEvent arg0) {
////		if (arg0.getSource().equals(ok)) {
////			okAction();
////		} else if (arg0.getSource().equals(cancel)) {
////			cancelAction();
////		}
////	}
//
//	@Override
//	public void okAction() {
//		super.okAction();
//		settings.recordSettings();
//	}
//
//	@Override
//	protected void initUI() {
//		setTitle(REpiceaTranslator.getString(MessageID.Title));
////		getContentPane().add(createControlPanel(), BorderLayout.SOUTH);
//		getContentPane().add(createMainPanel(), BorderLayout.NORTH);
//	}
//
//	@Override
//	public void synchronizeUIWithOwner() {}
//
//	@Override
//	public Memorizable getWindowOwner() {return settings;}
//
//	@Override
//	public void valueChanged(ListSelectionEvent arg0) {
//		if (arg0.getSource().equals(languageList)) {
//			settings.setLanguage(languageList.getSelectedValue());
//		} else if (arg0.getSource().equals(encodingList)) {
//			settings.setEncoding(encodingList.getSelectedValue());
//		}
//	}
//	
//

}
