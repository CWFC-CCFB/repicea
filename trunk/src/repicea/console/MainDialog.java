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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import repicea.app.Logger;
import repicea.console.TriggerSettings.Encoding;
import repicea.console.TriggerTask.TaskID;
import repicea.gui.AutomatedHelper;
import repicea.gui.REpiceaFrame;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.multiprocess.IndependentProcess.StateValue;
import repicea.multiprocess.JavaProcessWrapper;
import repicea.net.BrowserCaller;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class MainDialog extends REpiceaFrame implements ActionListener, PropertyChangeListener, ItemListener, ChangeListener {
	
	public static enum MessageID implements TextableEnum {
		Start("Start", "D\u00E9marrer"),
		Stop("Stop" , "Arr\u00EAter"),
		Quit("Quit", "Quitter"),
		StopActionMessage("The application will shut down and the data related with your project could be lost. Are you sure you want to proceed?",
				"L'application sera ferm\u00E9e et toutes les donn\u00E9es de votre projet pourraient \u00EAtre perdues. Voulez-vous continuer?"),
		CancelActionMessage("Do you really want to shut down the console?",
				"Voulez-vous vraiment fermer la console ?"),
		LanguageMenuTitle("Language", "Langage"),
		EncodingMenuTitle("Encoding", "Encodage"),
		MemoryMenuTitle("Memory", "M\u00E9moire"),
		JVMMemorySuffixe(" MgBytes", " MgOctets");
;

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
	}
	
	
	private class LanguageRadioButtonMenuItem extends JRadioButtonMenuItem {
	
		private final Language language;
		
		private LanguageRadioButtonMenuItem(Language language, Trigger caller) {
			this.language = language;
			setSelected(caller.settings.getLanguage() == language);
		}
		
		@Override
		public String getText() {
			if (language != null) {
				return language.toString();
			} else {
				return "";
			}
		}
	}
	

	private class EncodingRadioButtonMenuItem extends JRadioButtonMenuItem {
		
		private final Encoding encoding;
		
		private EncodingRadioButtonMenuItem(Encoding encoding, Trigger trigger) {
			this.encoding = encoding;
			setSelected(trigger.settings.getEncoding() == encoding);
		}
		
		@Override
		public String getText() {
			if (encoding != null) {
				return encoding.toString();
			} else {
				return "";
			}
		}
	}

	
	private JScrollPane scrollPane;
	protected Trigger caller;
	protected JMenu applicationMenu;
	protected JMenu languageMenu;
	protected JMenu encodingMenu;
	protected JMenu memorySizeMenu;
	protected JMenu currentMemoryMenu;
	protected JMenuItem mntmStart;
	protected JMenuItem mntmStop;
	protected JMenuItem mntmQuit;
	protected JMenuItem mntmHelp;
	protected JavaProcessWrapper javaProcessWrapper;
	protected Logger logger;
	protected LanguageRadioButtonMenuItem languageFr;
	protected LanguageRadioButtonMenuItem languageEn;
	protected EncodingRadioButtonMenuItem utf8Encoding;
	protected EncodingRadioButtonMenuItem asciiEncoding;
	protected EncodingRadioButtonMenuItem iso8859Encoding;
	
	
	private JSlider slider;

	
	

	public MainDialog(Trigger caller, Logger logger) {
		super();
		try {
			Method callHelp = BrowserCaller.class.getMethod("openUrl", String.class);
			String url = "http://www.inra.fr/capsis/help_" 
					+ caller.getSettings().getLanguage().getCode() 
					+ "/quebecmrnf/capsisstarter";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(getClass(), helper);
		} catch (Exception e) {}
		this.caller = caller;
		this.logger = logger;
		createUI();
	}

	protected void createUI() {
		setTitle(caller.getTitle() 
				+ " - JRE " + caller.getSettings().getJreVersion()
				+ " Revision " + caller.getSettings().getRevision());
		getContentPane().setLayout(new BorderLayout());
		scrollPane = logger.getUI();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		scrollPane.setColumnHeaderView(menuBar);
		
		applicationMenu = new JMenu(caller.getName());
		menuBar.add(applicationMenu);
		
		mntmStart = new JMenuItem(MessageID.Start.toString());
		applicationMenu.add(mntmStart);
		
		mntmStop = UIControlManager.createCommonMenuItem(CommonControlID.Stop);
		applicationMenu.add(mntmStop);
		mntmStop.setEnabled(false);
		
		mntmQuit = UIControlManager.createCommonMenuItem(CommonControlID.Quit);
		applicationMenu.add(mntmQuit);
		
		
		memorySizeMenu = new JMenu(MessageID.MemoryMenuTitle.toString());
		menuBar.add(memorySizeMenu);
		
		currentMemoryMenu = new JMenu();
		memorySizeMenu.add(currentMemoryMenu);
		
		slider = new JSlider(JSlider.VERTICAL);
		slider.setPaintTrack(true);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.setPaintLabels(true);
		int minimum = caller.getSettings().minAllowedMemoryJVM;
		int maximum = caller.getSettings().maxAllowedMemoryJVM;
		int majorTickSpacing;
		int minorTickSpacing;
		if (caller.getSettings().getArchitecture().endsWith("64")) {
//			minimum = 1024;
//			maximum = 8 * 1024;
			majorTickSpacing = 2*1024;
			minorTickSpacing = 1024;
		} else {
//			minimum = 256;
//			maximum = 2 * 1024;
			majorTickSpacing = 256;
			minorTickSpacing = 128;
		}
		slider.setMinimum(minimum);
		slider.setMaximum(maximum);
		slider.setMinorTickSpacing(minorTickSpacing);
		slider.setMajorTickSpacing(majorTickSpacing);
		slider.setValue(caller.settings.getAllocatedMemoryJVM());
		Hashtable<Integer, JComponent> valueTable = slider.createStandardLabels(majorTickSpacing, minimum);
		for (JComponent comp : valueTable.values()) {
			JLabel label = (JLabel) comp; // 2020-02-20 This cast is required for compatibility with Java 11 and 13
			label.setText(label.getText() + MessageID.JVMMemorySuffixe.toString());
		}
		slider.setLabelTable(valueTable);
		updateCurrentMemoryLabel();
		currentMemoryMenu.add(slider);
		
		languageMenu = new JMenu(MessageID.LanguageMenuTitle.toString());
		menuBar.add(languageMenu);
		languageFr = new LanguageRadioButtonMenuItem(Language.French, caller);
		languageMenu.add(languageFr);
		languageEn = new LanguageRadioButtonMenuItem(Language.English, caller);
		languageMenu.add(languageEn);
		ButtonGroup languageButtons = new ButtonGroup();
		languageButtons.add(languageFr);
		languageButtons.add(languageEn);

		encodingMenu = new JMenu(MessageID.EncodingMenuTitle.toString());
//		menuBar.add(encodingMenu);
		
		utf8Encoding = new EncodingRadioButtonMenuItem(Encoding.UTF_8, caller);
		encodingMenu.add(utf8Encoding);
		asciiEncoding = new EncodingRadioButtonMenuItem(Encoding.US_ASCII, caller);
		encodingMenu.add(asciiEncoding);
		iso8859Encoding = new EncodingRadioButtonMenuItem(Encoding.ISO_8859_1, caller);
		encodingMenu.add(iso8859Encoding);
		ButtonGroup encodingButtons = new ButtonGroup();
		encodingButtons.add(utf8Encoding);
		encodingButtons.add(asciiEncoding);
		encodingButtons.add(iso8859Encoding);
		
		JMenu mnAbout = UIControlManager.createCommonMenu(CommonMenuTitle.About);
		menuBar.add(mnAbout);
		
		mntmHelp = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		mnAbout.add(mntmHelp);
		
		setResizable(true);
		setSize(new Dimension(600,400));
	}
	
	private void updateCurrentMemoryLabel() {
		currentMemoryMenu.setText(slider.getValue()	+ MessageID.JVMMemorySuffixe.toString());
	}

	

	@Override
	public void actionPerformed(ActionEvent arg0) {
//		if (arg0.getSource().equals(mntmOptions)) {
//			openSettingsDialogAction();
//		} else 
		if (arg0.getSource().equals(mntmStart)) {
			startAction();
		} else if (arg0.getSource().equals(mntmQuit)) {
			cancelAction();
		} else if (arg0.getSource().equals(mntmStop)) {
			stopAction();
		} else if (arg0.getSource().equals(mntmHelp)) {
			helpAction();
		} 
	}

	
//	protected void openSettingsDialogAction() {
//		Language formerLanguage = caller.getSettings().getLanguage();
//		new SettingsDialog(this, caller.getSettings()).setVisible(true);
//		if (!caller.getSettings().getLanguage().equals(formerLanguage)) {
//			updateGUIForLanguage();
//		}
//	}
	
	protected void updateGUIForLanguage() {
		doNotListenToAnymore();
		getContentPane().removeAll();
		createUI();
		listenTo();
		validate();
	}
	
		
	@Override
	public void cancelAction() {
		if (JOptionPane.showConfirmDialog(this, 
				REpiceaTranslator.getString(MessageID.CancelActionMessage), 
				REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Warning), 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE) != 0) {		// if the answer is no
			return;
		} 
		if (javaProcessWrapper != null 
				&& javaProcessWrapper.getInternalProcess() != null 
				&& javaProcessWrapper.getInternalProcess().getState() == StateValue.STARTED) {
			int stopActionResult = stopAction();
			if (stopActionResult != 0) {		// the answer was no
				return;
			}
		}
		super.cancelAction();
		caller.requestShutdown();
	}
	
	
	protected int stopAction() {
		int result = JOptionPane.showConfirmDialog(this, 
				REpiceaTranslator.getString(MessageID.StopActionMessage), 
				REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Warning), 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		if (result == 0) {
			if (javaProcessWrapper != null) {
				javaProcessWrapper.cancel();
			}
		}
		return result;
	}
	
	protected void checkEnabledFeatures(boolean embeddedApplicationStarting) {
		mntmStart.setEnabled(!embeddedApplicationStarting);
		setMenuEnabled(!embeddedApplicationStarting);
		mntmStop.setEnabled(embeddedApplicationStarting);
	}
	
	protected void startAction() {
		caller.addTask(new TriggerTask(TaskID.ReduceInterface, caller));
		caller.addTask(new TriggerTask(TaskID.StartEmbeddedApplication, caller));
		caller.addTask(new TriggerTask(TaskID.ExpandInterface, caller));
	}

	
	protected void setMenuEnabled(boolean bool) {
		languageMenu.setEnabled(bool);
		encodingMenu.setEnabled(bool);
		memorySizeMenu.setEnabled(bool);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (javaProcessWrapper != null && javaProcessWrapper.getInternalProcess() != null) {
			if (arg0.getSource().equals(javaProcessWrapper.getInternalProcess())) {
				if (arg0.getPropertyName().equals("state")) {
					if (arg0.getNewValue() == StateValue.STARTED) {
						checkEnabledFeatures(true);
					} else if (arg0.getNewValue() == StateValue.DONE) {
						javaProcessWrapper.getInternalProcess().removePropertyChangeListener(this);
						checkEnabledFeatures(false);
					}
				}
			}
		}
	}



	@Override
	public void doNotListenToAnymore() {
		mntmStart.removeActionListener(this);
		mntmQuit.removeActionListener(this);
		mntmStop.removeActionListener(this);
		mntmHelp.removeActionListener(this);
		languageFr.removeItemListener(this);
		languageEn.removeItemListener(this);
		utf8Encoding.removeItemListener(this);
		asciiEncoding.removeItemListener(this);
		iso8859Encoding.removeItemListener(this);
		slider.removeChangeListener(this);
	}



	@Override
	public void listenTo() {
		mntmStart.addActionListener(this);
		mntmQuit.addActionListener(this);
		mntmStop.addActionListener(this);
		mntmHelp.addActionListener(this);
		languageFr.addItemListener(this);
		languageEn.addItemListener(this);
		utf8Encoding.addItemListener(this);
		asciiEncoding.addItemListener(this);
		iso8859Encoding.addItemListener(this);
		slider.addChangeListener(this);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof LanguageRadioButtonMenuItem) {
			LanguageRadioButtonMenuItem button = (LanguageRadioButtonMenuItem) e.getSource();
			if (button.isSelected() && button.language != caller.settings.getLanguage()) {
				caller.settings.setLanguage(button.language);
				updateGUIForLanguage();
			}
		} else if (e.getSource() instanceof EncodingRadioButtonMenuItem) {
			EncodingRadioButtonMenuItem button = (EncodingRadioButtonMenuItem) e.getSource();
			caller.settings.setEncoding(button.encoding);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(slider)) {
			caller.settings.setAllocatedMemoryJVM(slider.getValue());
			updateCurrentMemoryLabel();
		}
		
	}
}
