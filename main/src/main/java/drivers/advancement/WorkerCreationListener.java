package drivers.advancement;

import org.jetbrains.annotations.Nullable;

import org.javatuples.Pair;

import java.awt.GridLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JComponent;

import static lovelace.util.ShowErrorDialog.showErrorDialog;
import lovelace.util.Platform;
import lovelace.util.ListenedButton;
import lovelace.util.BorderedPanel;

import common.idreg.IDRegistrar;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.IWorker;

import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.RaceFactory;

import drivers.worker_mgmt.UnitSelectionListener;

import worker.common.IWorkerTreeModel;

import lovelace.util.SingletonRandom;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.Arrays;

import java.util.function.Consumer;

/**
 * A listener to keep track of the currently selected unit and listen for
 * new-worker notifications, then pass this information on to the tree model.
 */
/* package */ class WorkerCreationListener implements ActionListener, UnitSelectionListener {
	private static final Logger LOGGER = Logger.getLogger(WorkerCreationListener.class.getName());
	public WorkerCreationListener(IWorkerTreeModel model, IDRegistrar factory) {
		this.model = model;
		this.factory = factory;
		workerCreationFrame = new WorkerCreationFrame(factory, this::addNewWorker);
	}

	private final IWorkerTreeModel model;
	private final IDRegistrar factory;

	/**
	 * The currently selected unit
	 */
	@Nullable
	private IUnit selectedUnit = null;

	public void addNewWorker(IWorker worker) {
		if (selectedUnit != null) {
			model.addUnitMember(selectedUnit, worker);
		} else {
			LOGGER.warning("New worker created when no unit selected");
			showErrorDialog(null, "Strategic Primer Worker Advancement",
				"As no unit was selected, the new worker wasn't added to a unit.");
		}
	}

	private final WorkerCreationFrame workerCreationFrame;

	private static class WorkerCreationFrame extends JFrame {
		private final JTextField name;
		private final JTextField race;
		private final SpinnerNumberModel hpModel = new SpinnerNumberModel(0, -1, 100, 1);
		private final SpinnerNumberModel maxHP = new SpinnerNumberModel(0, -1, 100, 1);
		private final SpinnerNumberModel strength = new SpinnerNumberModel(0, -1, 32, 1);
		private final SpinnerNumberModel dexterity = new SpinnerNumberModel(0, -1, 32, 1);
		private final SpinnerNumberModel constitution = new SpinnerNumberModel(0, -1, 32, 1);
		private final SpinnerNumberModel intelligence = new SpinnerNumberModel(0, -1, 32, 1);
		private final SpinnerNumberModel wisdom = new SpinnerNumberModel(0, -1, 32, 1);
		private final SpinnerNumberModel charisma = new SpinnerNumberModel(0, -1, 32, 1);
		private final IDRegistrar factory;
		private final Consumer<IWorker> addNewWorker;

		public WorkerCreationFrame(IDRegistrar factory, Consumer<IWorker> addNewWorker) {
			super("Create Worker");
			this.factory = factory;
			this.addNewWorker = addNewWorker;
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			name = new JTextField();
			race = new JTextField(RaceFactory.randomRace());
			JPanel textPanel = new JPanel(new GridLayout(0, 2));

			addLabeledField(textPanel, "Worker Name:", name);
			addLabeledField(textPanel, "Worker Race", race);

			JPanel buttonPanel = new JPanel(new GridLayout(0, 2));

			JButton addButton = new ListenedButton("Add Worker", ignored -> accept());
			buttonPanel.add(addButton);

			JButton cancelButton = new ListenedButton("Cancel", ignored -> revert());
			buttonPanel.add(cancelButton);

			Platform.makeButtonsSegmented(addButton, cancelButton);

			JPanel statsPanel = new JPanel(new GridLayout(0, 4));
			hpModel.setValue(8);
			addLabeledField(statsPanel, "HP:", new JSpinner(hpModel));

			maxHP.setValue(8);
			addLabeledField(statsPanel, "Max HP:", new JSpinner(maxHP));

			for (Pair<String, SpinnerNumberModel> pair : Arrays.asList(
					Pair.with("Strength:", strength),
					Pair.with("Intelligence:", intelligence),
					Pair.with("Dexterity:", dexterity),
					Pair.with("Wisdom:", wisdom),
					Pair.with("Constitution:", constitution),
					Pair.with("Charisma:", charisma))) {
				String stat = pair.getValue0();
				SpinnerNumberModel model = pair.getValue1();
				model.setValue(SingletonRandom.SINGLETON_RANDOM.ints(3, 1, 7).sum());
				addLabeledField(statsPanel, stat, new JSpinner(model));
			}

			setContentPane(BorderedPanel.verticalPanel(textPanel, statsPanel,
				buttonPanel));

			setMinimumSize(new Dimension(320, 240));

			pack();
		}

		private void accept() {
			String nameText = name.getText().trim();
			String raceText = race.getText().trim();
			int hpValue = hpModel.getNumber().intValue();
			int maxHPValue = maxHP.getNumber().intValue();
			int strValue = strength.getNumber().intValue();
			int dexValue = dexterity.getNumber().intValue();
			int conValue = constitution.getNumber().intValue();
			int intValue = intelligence.getNumber().intValue();
			int wisValue = wisdom.getNumber().intValue();
			int chaValue = charisma.getNumber().intValue();
			if (!nameText.isEmpty() && !raceText.isEmpty() && hpValue >= 0 &&
					maxHPValue >= 0 && strValue >= 0 && dexValue >= 0 &&
					conValue >= 0 && intValue >= 0 && wisValue >= 0 &&
					chaValue >= 0) {
				// TODO: These logging statements should
				// probably be "trace" i.e. "finer", not
				// "debug" i.e. "fine"
				LOGGER.fine("All worker-creation-dialog fields are acceptable");
				Worker retval = new Worker(nameText, raceText, factory.createID());
				retval.setStats(new WorkerStats(hpValue, maxHPValue, strValue,
					dexValue, conValue, intValue, wisValue, chaValue));
				addNewWorker.accept(retval);
				LOGGER.fine("Created and added the worker; about to hide the window");
				setVisible(false);
				dispose();
			} else {
				StringBuilder builder = new StringBuilder();
				if (nameText.isEmpty()) {
					LOGGER.fine("Worker not created because name field was empty.");
					builder.append("Worker needs a name.").append(System.lineSeparator());
				}
				if (raceText.isEmpty()) {
					LOGGER.fine("Worker not created because race field was empty.");
					builder.append("Worker needs a race.").append(System.lineSeparator());
				}
				// FIXME: Only include stats in the error message if actually non-positive!
				for (Pair<String, Integer> pair : Arrays.asList(
						Pair.with("HP", hpValue), Pair.with("Max HP", maxHPValue),
						Pair.with("Strength", strValue),
						Pair.with("Dexterity", dexValue),
						Pair.with("Constitution", conValue),
						Pair.with("Intelligence", intValue),
						Pair.with("Wisdom", wisValue),
						Pair.with("Charisma", chaValue))) {
					String stat = pair.getValue0();
					int val = pair.getValue1();
					LOGGER.fine(String.format(
						"Worker not created because non-positive %s provided", stat));
					builder.append(String.format("%s must be a non-negative number.",
						stat)).append(System.lineSeparator());
				}
				showErrorDialog(getParent(), "Strategic Primer Worker Advancement",
					builder.toString());
			}
		}

		private void addLabeledField(JPanel panel, String text, JComponent field) {
			panel.add(new JLabel(text));
			panel.add(field);
			if (field instanceof JTextField) {
				((JTextField) field).addActionListener(ignored -> accept());
				((JTextField) field).setActionCommand("Add Worker");
			}
		}

		public void revert() {
			name.setText("");
			for (SpinnerNumberModel field : Arrays.asList(hpModel, maxHP, strength,
					dexterity, constitution, intelligence, wisdom, charisma)) {
				field.setValue(-1);
			}
			race.setText(RaceFactory.randomRace());
			dispose();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().toLowerCase().startsWith("add worker")) {
			workerCreationFrame.revert();
			workerCreationFrame.setVisible(true);
		}
	}

	/**
	 * Update our currently-selected-unit reference.
	 */
	@Override
	public void selectUnit(@Nullable IUnit unit) {
		selectedUnit = unit;
	}
}
