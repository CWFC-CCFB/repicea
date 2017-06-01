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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import repicea.gui.AutomatedHelper;
import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.gui.dnd.DragGestureImpl;
import repicea.io.FormatField;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * ImportDialog is a dialog box that matches the fields of the input data set
 * and the fields that are required for a particular model
 * @author Jean-Francois Lavoie and Mathieu Fortin - May 2009
 */
@SuppressWarnings("serial")
public class ImportFieldManagerDialog extends REpiceaDialog implements ActionListener, MouseListener, IOUserInterface {
	
	static {
		UIControlManager.setTitle(ImportFieldManagerDialog.class, "REpicea import tool", "Utilitaire d'importation REpicea");
	}
	
	private static final Font TEXT_FONT = new Font("Arial12",Font.PLAIN,12);
	private static final Color POPUP_COLOR = new Color(.99f,.99f,.75f);
	
	/**
	 * This enum contains all the messages that are displayed by this dialog.
	 * @author Mathieu Fortin - September 2012
	 */
	protected static enum MessageID implements TextableEnum {
		MissingField("This field is mandatory: ",
				"Le champ suivant est obligatoire et n'a pas \u00E9t\u00E9 s\u00E9lectionn\u00E9 dans le fichier de donn\u00E9es : "),
		OptionalField("[Optional]", 
				"[Facultatif]"),
		Instruction("Please select a field for each field in the following list: ",
				"Veuillez s\u00E9lectionner un champ du fichier de donn\u00E9es pour chacun des champs dans la liste suivante : "),
		FileToBeImported("File being imported: ", "Fichier en cours d'importation : ");
		
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
	
	
	/*
	 * Members of this class
	 */
	private ImportFieldManager caller;
	
	@SuppressWarnings("rawtypes")
	private JList fieldListInFile; 
	private JPanel fieldMatchPanel;
	
	private JButton ok;
	private JButton cancel;
	private JMenuItem help;
	private JMenuItem load;
	private JMenuItem save;
	private JMenuItem saveAs;
	
	private PopupFactory infoWindowMaker = new PopupFactory();
	private Popup infoWindow;
	private JTextArea selectedField;
	
	protected final WindowSettings windowSettings;
	
	
	/**
	 * Constructor.
	 * @param caller an ImportFieldManager instance
	 * @param owner the Frame that owns this dialog (can be null)
	 */
	@SuppressWarnings("rawtypes")
	protected ImportFieldManagerDialog(ImportFieldManager caller, Window owner) {
		super(owner);
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);
		this.caller = caller;
//		this.askUserBeforeExit = true;
		fieldListInFile = new JList();
		fieldListInFile.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		help = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);
		
		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		
		DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(fieldListInFile, DnDConstants.ACTION_COPY, new DragGestureImpl<FormatField>());
				
		initializePopupContent();
		initUI();
		Dimension dim = new Dimension(700,500);
		setMinimumSize(dim);
		pack();
	}

	private void initializePopupContent() {
		selectedField = new JTextArea();
		selectedField.setFont(TEXT_FONT);
		selectedField.setLineWrap(true);
		selectedField.setWrapStyleWord(true);
		selectedField.setEditable(false);
		selectedField.setBackground(POPUP_COLOR);
		selectedField.setMargin(new Insets(2,2,2,2));
	}
		
	
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(cancel)) {
			cancelAction();
		} else if (evt.getSource().equals(help)) {
			AutomatedHelper helper = UIControlManager.getHelper(getClass());
			if (helper != null) {
				helper.callHelp();
			}
		}
	}
	
	public void mouseClicked(MouseEvent e) {}
		
	public void mouseEntered(MouseEvent e) {
		if (caller.isPopupInGuiInterfaceEnabled()) {
			Container parent = (Container) e.getSource();
			while (parent != null && !(parent instanceof ImportFieldElementPanel)) {
				parent = parent.getParent();
			}
			
			if (parent != null) {
				ImportFieldElement ife = ((ImportFieldElementPanel) parent).getCaller(); 
				selectedField.setText(ife.helpDescription);
				selectedField.setSize(new Dimension(200,200));
				JPanel pane = new JPanel();
				pane.add(this.selectedField);
				Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.GRAY);
				pane.setBorder(border);
				pane.setBackground(POPUP_COLOR);
				infoWindow = this.infoWindowMaker.getPopup(this, pane, 
						((MouseEvent) e).getLocationOnScreen().x+20, 
						((MouseEvent) e).getLocationOnScreen().y+10);
				infoWindow.show();
			}
		}
	}
		
	public void mousePressed(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {
		if (infoWindow != null) {
			infoWindow.hide();
		}
	}
	
	public void mouseReleased(MouseEvent e) {}

			
	/**
	 *  Initializes the GUI. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void initUI() {
		refreshTitle();

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fileMenu = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		menuBar.add(fileMenu);
		fileMenu.add(load);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		
		JMenu aboutMenu = UIControlManager.createCommonMenu(CommonMenuTitle.About);
		menuBar.add(aboutMenu);
		aboutMenu.add(help);
		
		fieldListInFile.setListData(caller.getFormatFields());
		fieldListInFile.setSelectedIndex(0);				// first field in the list is selected by default
		fieldListInFile.setVisibleRowCount(15);

		JScrollPane lList = new JScrollPane();
		JSplitPane lMain = new JSplitPane();
		
		fieldMatchPanel = new JPanel();
		fieldMatchPanel.setLayout(new BoxLayout(fieldMatchPanel, BoxLayout.Y_AXIS));
		
		List<ImportFieldElement> vecOfFieldElements = caller.getFields();
		ImportFieldElementPanel ifePanel;
		for (int i = 0; i < vecOfFieldElements.size(); i++) {
			ifePanel = vecOfFieldElements.get(i).getUI();
			fieldMatchPanel.add(ifePanel);
			ifePanel.descriptionLabel.addMouseListener(this);		// then add it again, to avoid having this class listening several time to the same container
		}
		lMain.setLeftComponent(fieldMatchPanel);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		fieldMatchPanel.add(horizontalStrut);
		
		lList.setViewportView(fieldListInFile);
		lMain.setRightComponent(lList);

		Dimension d2 = lMain.getMinimumSize();
		lMain.setMinimumSize(d2);
		d2.height -= 30;
		fieldListInFile.setMaximumSize(d2);
		
		Border etchedBorder = BorderFactory.createEtchedBorder();
		
		JLabel importedFileLabel = UIControlManager.getLabel(CommonGuiUtility.convertFilenameForLabel(caller.getFileSpecifications()[0], 60));
		JPanel importedFilePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		importedFilePane.add(importedFileLabel);
		importedFilePane.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.FileToBeImported.toString()));

		
		// Control panel
		JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pControl.add(ok);
		pControl.add(cancel);

		getContentPane().setLayout(new BorderLayout());
		
		JScrollPane mainScrollPane = new JScrollPane(lMain);
		mainScrollPane.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.Instruction.toString()));
		
		getContentPane().add (importedFilePane, BorderLayout.NORTH);
		getContentPane().add (mainScrollPane, BorderLayout.CENTER);
		getContentPane().add (pControl, BorderLayout.SOUTH);

		ok.requestFocusInWindow();
	}

	@Override
	public void okAction() {
		ImportFieldElement missingElement = caller.checkFields();
		if (missingElement != null) {
			JOptionPane.showMessageDialog(this, 
					MessageID.MissingField.toString() + missingElement.description, 
					UIControlManager.InformationMessageTitle.Warning.toString(),
					JOptionPane.ERROR_MESSAGE);	
			return;
		}
		caller.setUserValidated(true);
		caller.saveDefaultValues();
		super.okAction();
	}

	@Override
	public void cancelAction() {
		caller.setUserValidated(false);
		super.cancelAction();
	}

	@Override
	public void listenTo() {
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		ok.removeActionListener (this);
		cancel.removeActionListener (this);
		help.removeActionListener (this);
	}

	@Override
	public void refreshInterface() {
		List<ImportFieldElement> vecOfFieldElements = caller.getFields();
		for (int i = 0; i < vecOfFieldElements.size(); i++) {
			vecOfFieldElements.get(i).getUI().refreshInterface();
		}
		validate();
	}

	private void refreshTitle() {
		REpiceaIOFileHandlerUI.RefreshTitle(caller, this);
	}
	
	@Override
	public void postLoadingAction() {
		refreshInterface();
		refreshTitle();
	}

	@Override
	public void postSavingAction() {
		refreshTitle();
	}

	@Override
	public WindowSettings getWindowSettings() {return windowSettings;}

}


