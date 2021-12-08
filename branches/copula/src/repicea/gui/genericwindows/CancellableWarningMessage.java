package repicea.gui.genericwindows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * This dialog shows a warning when the projection exceeds a particular numbers of steps
 * It is possible to disable this warning through check box
 * Author Mathieu Fortin - August 2009
 */
@SuppressWarnings("serial")
public class CancellableWarningMessage extends REpiceaDialog implements ActionListener {

	public static enum MessageID implements TextableEnum {
		DisableWarningMessage("Disable this warning", "Ne plus afficher ce message");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText (String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	/*
	 * Members of this class
	 */
	private JCheckBox disableThisDialog;
	private JButton ok;
	private JButton cancel;
	private String m_StrWarningMessage;
	private boolean isCancelled;
		
	/**	
	 * Constructor.
	 */
	public CancellableWarningMessage(JDialog parent, String message) {
		super(parent);

		if (parent != null) {
			setIconImages(parent.getIconImages());
		}
		m_StrWarningMessage = message.trim();
		
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		ok.addActionListener (this);
		
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		cancel.addActionListener (this);

		disableThisDialog = new JCheckBox(MessageID.DisableWarningMessage.toString(), false);

		initUI();
		pack();
		setVisible (true);
	}

	@Override
	public void okAction() {
		isCancelled = false;
		super.okAction();
	}

	@Override
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(cancel)) {
			cancelAction();
		}
	}
		
	@Override
	public void cancelAction() {
		isCancelled = true;
		super.cancelAction();
	}
	
	/**
	 * This method returns true if the warning dialog has been cancelled.
	 * @return a boolean
	 */
	public boolean isCancelled() {
		return isCancelled;
	}
	
	@Override
	protected void initUI() {
		setTitle(UIControlManager.InformationMessageTitle.Warning.toString());

		JTextArea textArea = new JTextArea(m_StrWarningMessage);
		textArea.setBackground(getBackground());
		textArea.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setPreferredSize(new Dimension(70,70));

		JPanel subPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		subPanel1.add(disableThisDialog);
		subPanel1.add(Box.createHorizontalGlue());
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(textArea);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(subPanel1);
		mainPanel.add(Box.createVerticalStrut(5));

		JPanel controlPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(ok);
		controlPanel.add(cancel);

		ok.setDefaultCapable(true);
		getRootPane ().setDefaultButton(ok);

		getContentPane ().setLayout (new BorderLayout());
		
		JPanel panel = new JPanel();
		Icon icon = UIManager.getIcon("OptionPane.warningIcon");
		panel.add(new JLabel(icon));
		getContentPane().add(panel, BorderLayout.WEST);
		getContentPane ().add(mainPanel, BorderLayout.CENTER);
		getContentPane ().add(controlPanel, BorderLayout.SOUTH);
		setResizable(true);
		setModal(true);
	}

	public boolean isWarningDisabled() {
		return disableThisDialog.isSelected();
	}
	
	@Override
	public void listenTo() {
		ok.addActionListener(this);
		cancel.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore () {
		ok.removeActionListener(this);
		cancel.removeActionListener(this);
	}


	public static void main(String[] args) {
		new CancellableWarningMessage(null, "Allo les amis! Je suis allé ce matin au marché. Ca ma fait du bien. Bla bla bla. Allo les amis! Je suis allé ce matin au marché. Ca ma fait du bien. Bla bla bla.");
		System.exit(0);
	}

}


