package repicea.predictor.thinners.melothinner;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.gui.components.REpiceaCellEditor;
import repicea.gui.components.REpiceaTable;
import repicea.gui.components.REpiceaTableModel;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class MeloThinnerTreeHarvestDecisionDialog extends REpiceaDialog implements IOUserInterface {
	
	static {
		UIControlManager.setTitle(MeloThinnerTreeHarvestDecisionDialog.class, "Treatments to be applied in each potential vegetation", "Traitement \u00E0 appliquer dans chaque v\u00E9g\u00E9tation potentielle");
	}
	
	private static enum MessageID implements TextableEnum {
		PotentialVegetation("Potential vegetation", "V\u00E9g\u00E9tation potentielle"),
		SilviculturalTreatment("Treatment", "Traitement");

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

	private final MeloThinnerTreeHarvestDecision caller;
	private final REpiceaTable table;
	private final REpiceaTableModel tableModel;
	private final JMenuItem load;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final WindowSettings windowSettings;
	
	protected MeloThinnerTreeHarvestDecisionDialog(MeloThinnerTreeHarvestDecision caller, Window parent) {
		super(parent);
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);
		this.cancelOnClose = false;
		this.caller = caller;
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);
		
		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		
		
		tableModel = new REpiceaTableModel(MessageID.values());
		tableModel.setEditableVetos(0, true);
		table = new REpiceaTable(tableModel, false); // false : adding or deleting rows is disabled
		table.setDefaultEditor(Enum.class, new REpiceaCellEditor(new JComboBox<TextableEnum>(caller.potentialTreatments.toArray(new TextableEnum[]{})), tableModel));
//		table.setRowSorter(new TableRowSorter<REpiceaTableModel>(tableModel));
		
		Object[] record;
		for (String potentialVegetation : this.caller.treatmentMatchMap.keySet()) {
			record = new Object[2];
			record[0] = potentialVegetation;
			record[1] = this.caller.treatmentMatchMap.get(potentialVegetation);
			tableModel.addRow(record);
		}
		initUI();
		pack();

	}
	
	@Override
	public void listenTo() {
		tableModel.addTableModelListener(caller);
	}

	@Override
	public void doNotListenToAnymore() {
		tableModel.removeTableModelListener(caller);
	}

	@Override
	protected void initUI() {
		setTitle(UIControlManager.getTitle(getClass()));
		setJMenuBar(new JMenuBar());
		JMenu fileMenu = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		getJMenuBar().add(fileMenu);
		fileMenu.add(load);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		
		getContentPane().setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(table);
		getContentPane().add(scrollPane);
	}

	@Override
	public void postLoadingAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSavingAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WindowSettings getWindowSettings() {return windowSettings;}

}
