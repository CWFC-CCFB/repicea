/*
 * This file is part of the repicea-iotools library.
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
package repicea.io.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CancellationException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import repicea.gui.AutomatedHelper;
import repicea.gui.CommonGuiUtility;
import repicea.gui.CommonGuiUtility.FileChooserOutput;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.genericwindows.REpiceaProgressBarDialog;
import repicea.io.REpiceaFileFilter;
import repicea.io.REpiceaFileFilterList;
import repicea.lang.MemoryWatchDog.ExpectedMemoryCapacityException;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The ExportDBFToolDialog class is the user interface of the ExportDBFTool class.
 * @author Mathieu Fortin - January 2011
 */
@Deprecated
public class ExportToolDialog extends REpiceaDialog implements ActionListener {

	static {
		UIControlManager.setTitle(ExportToolDialog.class, "REpicea export tool", "Utilitaire d'exportation REpicea");
	}
	
	private static final long serialVersionUID = 20110124L;

	public static enum MessageID implements TextableEnum {
		PleaseSelectAFile("Please select an export file",
				"Veuillez choisir un fichier d'exportation"), 
		OptionTitle("Possible formats",
				"Formats disponibles"),
		PleaseSelectAnOption("Please select a export format among the following options: ",
				"Veuillez choisir un format d'exportation parmi les suivants : "),
		ProgressMessageBuildingRecordSet("Creating the export dataset...", 
				"Construction du jeu de donn\u00E9es..."),
		ProgressMessageExport("Exporting the data...",
				"Exportation des donn\u00E9es..."),
		ErrorWhileSelectingFile("An error occured while selecting the file! Please contact the manager of this software.",
				"Une erreur est survenue lors de la s\u00E9lection du fichier \u00E0 sauvegarder! Veuillez contacter l'administrateur de ce logiciel.")
		;
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
	}
	
	
	private ExportTool caller;
	
	private JButton ok;
	private JButton cancel;
	private JButton help;
	private JButton browse;
	
	private JLabel pleaseSelectAFileLabel;
	protected JTextField filenameField;
	
	protected JList<?> optionList;
	protected JTextArea selectAnOptionTextArea;
	protected JLabel optionTitleLabel;
	
	protected boolean triggerRecordSetAndSaveManually = false;		// default option
	
	/**
	 * Constructor.
	 * @param caller an ExportTool instance
	 * @param parent the Window that owns this dialog (can be null)
	 */
	protected ExportToolDialog(ExportTool caller, Window parent) {
		super(parent);
		this.caller = caller;
		
		init();
		
		initUI();
		pack();
		setLocationRelativeTo(parent);
	}
	
	/**
	 * The init method instantiates the members of this class.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void init() {
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		
		help = UIControlManager.createCommonButton(CommonControlID.Help);
		
		browse = UIControlManager.createCommonButton(CommonControlID.Browse);
		
		filenameField = new JTextField(35);
		filenameField.setText(getCaller().getFilename());
		
		pleaseSelectAFileLabel = new JLabel();
		pleaseSelectAFileLabel.setText(REpiceaTranslator.getString(MessageID.PleaseSelectAFile));
		
		optionTitleLabel = new JLabel();
		optionTitleLabel.setText(REpiceaTranslator.getString(MessageID.OptionTitle));
		
		selectAnOptionTextArea = new JTextArea(REpiceaTranslator.getString(MessageID.PleaseSelectAnOption));
		selectAnOptionTextArea.setLineWrap(true);
		selectAnOptionTextArea.setWrapStyleWord(true);
		selectAnOptionTextArea.setEditable(false);
		selectAnOptionTextArea.setBackground(this.getContentPane().getBackground());
		selectAnOptionTextArea.setFont(this.getFont());
		
		List<Enum> availableOptions = getCaller().getAvailableExportOptions();
		Vector<Enum> vecForJList = new Vector<Enum>();
		for (Enum var : availableOptions) {
			vecForJList.add(var);
		}
		optionList = new JList(vecForJList);
		if (caller.multipleSelection) {
			optionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			optionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		Set<Enum> selectedOptions = getCaller().getSelectedExportOptions();
		List<Integer> selectedIndices = new ArrayList<Integer>();
		
		for (Enum selectedOption : selectedOptions) {
			if (availableOptions.contains(selectedOption)) {
				selectedIndices.add(availableOptions.indexOf(selectedOption));
			}
		}
		
		if (selectedOptions.isEmpty()) {
			try {
				Set<Enum> defaultOptions = new HashSet<Enum>();
				defaultOptions.add(availableOptions.get(0));
				getCaller().setSelectedOptions(defaultOptions);
				selectedIndices.add(0);
			} catch (Exception e) {}
		}
		
		int[] convertedSelectedIndices = new int[selectedIndices.size()];
		for (int i = 0; i < selectedIndices.size(); i++) {
			convertedSelectedIndices[i] = selectedIndices.get(i);
		}
		
		optionList.setSelectedIndices(convertedSelectedIndices);
		
		
	}
	
	
	@Override
	protected void initUI() {
		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel fileSelectionPanel = CommonGuiUtility.createSelectAFilePanel(filenameField, browse, pleaseSelectAFileLabel);
		
		mainPanel.add(getTopPanels(), BorderLayout.CENTER);
		mainPanel.add(fileSelectionPanel, BorderLayout.SOUTH);
		
		add(mainPanel, BorderLayout.CENTER);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		controlPanel.add(ok);
		controlPanel.add(cancel);
		controlPanel.add(help);
		
		add(controlPanel, BorderLayout.SOUTH);
		
		Dimension dim = new Dimension(500,300);
		setMinimumSize(dim);
		getRootPane().setDefaultButton(ok);

		refreshTitle();
		pack();
		
	}
	
	protected JPanel getTopLeftPanel() {
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		selectAnOptionTextArea.setBackground(this.getBackground());
		Dimension dim1 = new Dimension(150,100);
		selectAnOptionTextArea.setSize(dim1);

		leftPanel.add(selectAnOptionTextArea);
		return leftPanel;
	}
	
	protected JPanel getTopRightPanel() {
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		
		JScrollPane	panelList = new JScrollPane(optionList);
		panelList.setSize(100, 100);

		rightPanel.add(optionTitleLabel);
		rightPanel.add(panelList);
		return rightPanel;
	}

	protected JPanel getTopPanels() {
		JPanel jointPanel = new JPanel(new BorderLayout());
		jointPanel.add(getTopLeftPanel(), BorderLayout.WEST);
		jointPanel.add(getTopRightPanel(), BorderLayout.CENTER);
		return jointPanel;
	}

	/**
	 * The okAction method checks if the filename is valid, then creates the record set and finally save the file.
	 */
	@Override
	public void okAction() {
		try {
			if (!checkFileValidity()) {
				return;						// if the user does want to write over that file
			}
			
			if (!this.triggerRecordSetAndSaveManually) {
				getCaller().createRecordSets();
				getCaller().save();
				JOptionPane.showMessageDialog(this, 
						REpiceaTranslator.getString(UIControlManager.InformationMessage.DataCorrectlySaved),
						REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Information),
						JOptionPane.INFORMATION_MESSAGE);
			}
			
			super.okAction();
		} catch (CancellationException e) {
			return;
		} catch (IOException e) {		// filename is incorrect
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					REpiceaTranslator.getString(UIControlManager.InformationMessage.IncorrectFilename), 
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			JOptionPane.showMessageDialog(this, 
					REpiceaTranslator.getString(UIControlManager.InformationMessage.CannotDeleteFile), 
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (ExpectedMemoryCapacityException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					REpiceaTranslator.getString(UIControlManager.InformationMessage.ExpectedMemoryBustOut), 
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (Exception e) {		// other exception
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					REpiceaTranslator.getString(UIControlManager.InformationMessage.ErrorWhileSavingData),
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	
	}
	
	
	/**
	 * This method checks if the filename is valid. In case the file already exists, it asks for confirmation before deleting it.
	 * @return a boolean true means everything is fine false means the user does not want to write over that file.
	 * @throws IOException if the filename is invalid
	 * @throws IllegalArgumentException if the deletion failed
	 * @throws Exception other exceptions
	 */
	@SuppressWarnings("rawtypes")
	protected boolean checkFileValidity() throws IOException, IllegalArgumentException, Exception {
		List<File> files = new ArrayList<File>();
		String filename = getCaller().getFilename();
		
		if (filename == null || filename.isEmpty()) {
			throw new IOException("A filename must be specified!");
		}
		
		for (Enum selectedOutputOption : getCaller().getSelectedExportOptions()) {
			if (getCaller().getSelectedExportOptions().size() == 1) {
				files.add(new File(filename));
			} else {
				int indexOfLastDot = filename.lastIndexOf(".");
				String extension = filename.substring(indexOfLastDot, filename.length()).trim();
				String originalFilename = filename.substring(0, indexOfLastDot).trim();
				String optionType = selectedOutputOption.name().trim();
				files.add(new File(originalFilename + optionType + extension));
			}
		}

		for (File file : files) {
			try {
				file = new File(getCaller().getFilename());
				
				if (file.createNewFile()) {				// test if the new file can be created. Do not test if the file already exists. If the name is invalid it throws an IOException
					file.delete();
				}
			} catch (Exception e) {
				throw new IOException(); 				// means the filename is incorrect
			}
		}

		boolean successfullyDeleted;				// instantiated to false by default

		List<File> filesThatAlreadyExists = new ArrayList<File>();
		
		for (File file : files) {
			if (file.exists()) {
				filesThatAlreadyExists.add(file);
			}
		}

		if (!filesThatAlreadyExists.isEmpty()) {
			if (!CommonGuiUtility.popupWriteOverWarningDialog(this)) {
				return false;
			}
			for (File file : filesThatAlreadyExists) {
				successfullyDeleted = file.delete();
				if (!successfullyDeleted) {
					throw new IllegalArgumentException("Delete: deletion failed");
				} 
				
			}
		}
		
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(ok)) {
			okAction();
		} else if (arg0.getSource().equals(cancel)) {
			cancelAction();
		} else if (arg0.getSource().equals(help)) {
			AutomatedHelper helper = UIControlManager.getHelper(this.getClass());
			if (helper != null) {
				helper.callHelp();
			}
		} else if (arg0.getSource().equals(browse)) {
			try {
				REpiceaFileFilterList fileFilters = new REpiceaFileFilterList(REpiceaFileFilter.DBF, REpiceaFileFilter.CSV);
				FileChooserOutput fco = CommonGuiUtility.browseAction(this, 
						JFileChooser.FILES_ONLY, 
						getCaller().getFilename(), 
						fileFilters,
						JFileChooser.SAVE_DIALOG);
				if (fco.isValid()) {
					filenameField.setText(fco.getFilename());	
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, 
						MessageID.ErrorWhileSelectingFile.toString(), 
						UIControlManager.InformationMessageTitle.Error.toString(), 
						JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}

	@Override
	public void cancelAction() {
		getCaller().setCanceled(true);
		super.cancelAction();
	}


	protected void refreshTitle() {
		String title = UIControlManager.getTitle(ExportToolDialog.class);
		String filename = getCaller().getFilename();
		if (filename != null) {
			if (filename.length() > 30) {
				filename = " - ..." + filename.substring(filename.length() - 28, filename.length());
			} else {
				filename = " - " + filename;
			}
		} else {
			filename = "";
		}
		setTitle(title + filename);
	}


	protected ExportTool getCaller() {return caller;}
	
	/**
	 * This method makes it possible to just save the filename and wait to trigger the createRecordSet and save methods manually.
	 * @param triggerRecordSetAndSaveManually a boolean
	 */
	public void setTriggerRecordSetAndSaveManually(boolean triggerRecordSetAndSaveManually) {
		this.triggerRecordSetAndSaveManually = triggerRecordSetAndSaveManually;
	}

	@SuppressWarnings("rawtypes")
	protected void showProgressBar(SwingWorker worker, Enum selectedOption, boolean isCreatingDataset) {
		JDialog owner = null;
		if (isVisible()) {
			owner = this;
		}
		
		String jobName = selectedOption.toString();
		if (isCreatingDataset) {
			jobName += " - " + REpiceaTranslator.getString(MessageID.ProgressMessageBuildingRecordSet);
		} else {
			jobName += " - " + REpiceaTranslator.getString(MessageID.ProgressMessageExport);
		}
		
		new REpiceaProgressBarDialog(owner, 
				REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Progress),
				jobName,
				worker,
				false);
	}

	@Override
	public void doNotListenToAnymore() {
		ok.removeActionListener(this);
		cancel.removeActionListener(this);
		help.removeActionListener(this);
		browse.removeActionListener(this);
		filenameField.removeCaretListener(getCaller());
		optionList.removeListSelectionListener(getCaller());
	}

	@Override
	public void listenTo() {
		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);
		browse.addActionListener(this);
		filenameField.addCaretListener(getCaller());
		optionList.addListSelectionListener(getCaller());
	}
	
}
