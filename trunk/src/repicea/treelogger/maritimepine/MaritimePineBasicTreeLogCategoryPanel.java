package repicea.treelogger.maritimepine;

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

import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLogCategoryPanel;
import repicea.util.REpiceaTranslator;

@SuppressWarnings("serial")
public class MaritimePineBasicTreeLogCategoryPanel extends TreeLogCategoryPanel<MaritimePineBasicTreeLogCategory> {
	
	protected MaritimePineBasicTreeLogCategoryPanel(MaritimePineBasicTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setText(logCategory.getName());
		nameTextField.setEditable(false);
		createUI();
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

		JLabel nameLabel = new JLabel(REpiceaTranslator.getString(MessageID.LogGradeName));
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

		JPanel featurePanel = new JPanel();
		featurePanel.add(Box.createHorizontalStrut(5));
		featurePanel.add(UIControlManager.getLabel(MessageID.SmallEndDiameter));
		featurePanel.add(Box.createHorizontalStrut(5));
		JTextField textField = new JTextField(5);
		if (!Double.isNaN(getTreeLogCategory().smallEndDiameter)) {
			textField.setText(getTreeLogCategory().smallEndDiameter.toString());
		}
		textField.setEditable(false);
		featurePanel.add(textField);
		featurePanel.add(Box.createHorizontalGlue());
		
		panel.add(featurePanel);

	}
}