package drivers.advancement;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serial;
import java.util.Objects;
import java.util.stream.IntStream;

import lovelace.util.LovelaceLogger;
import org.jspecify.annotations.Nullable;

import org.javatuples.Pair;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import lovelace.util.Platform;
import lovelace.util.ListenedButton;
import lovelace.util.BorderedPanel;

import legacy.idreg.IDRegistrar;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Worker;
import legacy.map.fixtures.mobile.IWorker;

import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.RaceFactory;

import worker.common.IWorkerTreeModel;

import lovelace.util.SingletonRandom;

import java.util.Arrays;

import java.util.function.Consumer;

/**
 * A listener to keep track of the currently selected unit and listen for
 * new-worker notifications, then pass this information on to the tree model.
 */
/* package */ final class WorkerCreationListener implements IWorkerCreationListener {
	public WorkerCreationListener(final IWorkerTreeModel model, final IDRegistrar factory) {
		this.model = model;
		workerCreationFrame = new WorkerCreationFrame(factory, this::addNewWorker);
	}

	private final IWorkerTreeModel model;

	/**
	 * The currently selected unit
	 */
	private @Nullable IUnit selectedUnit = null;

	@SuppressWarnings("TypeMayBeWeakened") // Don't change public API
	@Override
	public void addNewWorker(final IWorker worker) {
		if (Objects.isNull(selectedUnit)) {
			LovelaceLogger.warning("New worker created when no unit selected");
			JOptionPane.showMessageDialog(null,
					"As no unit was selected, the new worker wasn't added to a unit.",
					"Strategic Primer Worker Advancement", JOptionPane.ERROR_MESSAGE);
		} else {
			model.addUnitMember(selectedUnit, worker);
		}
	}

	private final WorkerCreationFrame workerCreationFrame;

	private static final class WorkerCreationFrame extends JFrame {
		@Serial
		private static final long serialVersionUID = 1L;
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

		public WorkerCreationFrame(final IDRegistrar factory, final Consumer<IWorker> addNewWorker) {
			super("Create Worker");
			this.factory = factory;
			this.addNewWorker = addNewWorker;
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			name = new JTextField();
			race = new JTextField(RaceFactory.randomRace());
			final JPanel textPanel = new JPanel(new GridLayout(0, 2));

			addLabeledField(textPanel, "Worker Name:", name);
			addLabeledField(textPanel, "Worker Race", race);

			final JPanel buttonPanel = new JPanel(new GridLayout(0, 2));

			final JButton addButton = new ListenedButton("Add Worker", this::accept);
			buttonPanel.add(addButton);

			final JButton cancelButton = new ListenedButton("Cancel", this::revert);
			buttonPanel.add(cancelButton);

			Platform.makeButtonsSegmented(addButton, cancelButton);

			final JPanel statsPanel = new JPanel(new GridLayout(0, 4));
			hpModel.setValue(8);
			addLabeledField(statsPanel, "HP:", new JSpinner(hpModel));

			maxHP.setValue(8);
			addLabeledField(statsPanel, "Max HP:", new JSpinner(maxHP));

			for (final Pair<String, SpinnerNumberModel> pair : Arrays.asList(
					Pair.with("Strength:", strength),
					Pair.with("Intelligence:", intelligence),
					Pair.with("Dexterity:", dexterity),
					Pair.with("Wisdom:", wisdom),
					Pair.with("Constitution:", constitution),
					Pair.with("Charisma:", charisma))) {
				final String stat = pair.getValue0();
				final SpinnerNumberModel model = pair.getValue1();
				model.setValue(SingletonRandom.SINGLETON_RANDOM.ints(3, 1, 7).sum());
				addLabeledField(statsPanel, stat, new JSpinner(model));
			}

			setContentPane(BorderedPanel.verticalPanel(textPanel, statsPanel,
					buttonPanel));

			setMinimumSize(new Dimension(320, 240));

			pack();
		}

		private void accept() {
			final String nameText = name.getText().strip();
			final String raceText = race.getText().strip();
			final int hpValue = hpModel.getNumber().intValue();
			final int maxHPValue = maxHP.getNumber().intValue();
			final int strValue = strength.getNumber().intValue();
			final int dexValue = dexterity.getNumber().intValue();
			final int conValue = constitution.getNumber().intValue();
			final int intValue = intelligence.getNumber().intValue();
			final int wisValue = wisdom.getNumber().intValue();
			final int chaValue = charisma.getNumber().intValue();
			if (!nameText.isEmpty() && !raceText.isEmpty() && IntStream.of(hpValue, maxHPValue, strValue, dexValue,
					conValue, intValue, wisValue, chaValue).allMatch(x -> x >= 0)) {
				LovelaceLogger.debug("All worker-creation-dialog fields are acceptable");
				final Worker retval = new Worker(nameText, raceText, factory.createID());
				retval.setStats(new WorkerStats(hpValue, maxHPValue, strValue,
						dexValue, conValue, intValue, wisValue, chaValue));
				addNewWorker.accept(retval);
				LovelaceLogger.trace("Created and added the worker; about to hide the window");
				setVisible(false);
				dispose();
			} else {
				final StringBuilder builder = new StringBuilder();
				if (nameText.isEmpty()) {
					LovelaceLogger.trace("Worker not created because name field was empty.");
					builder.append("Worker needs a name.").append(System.lineSeparator());
				}
				if (raceText.isEmpty()) {
					LovelaceLogger.trace("Worker not created because race field was empty.");
					builder.append("Worker needs a race.").append(System.lineSeparator());
				}
				for (final Pair<String, Integer> pair : Arrays.asList(
						Pair.with("HP", hpValue), Pair.with("Max HP", maxHPValue),
						Pair.with("Strength", strValue),
						Pair.with("Dexterity", dexValue),
						Pair.with("Constitution", conValue),
						Pair.with("Intelligence", intValue),
						Pair.with("Wisdom", wisValue),
						Pair.with("Charisma", chaValue))) {
					final String stat = pair.getValue0();
					final int val = pair.getValue1();
					if (val < 0) {
						LovelaceLogger.trace(
								"Worker not created because non-positive %s provided", stat);
						builder.append("%s must be a non-negative number.%n".formatted(stat));
					}
				}
				final Component parent = getParent();
				JOptionPane.showMessageDialog(parent, builder.toString(),
						"Strategic Primer Worker Advancement", JOptionPane.ERROR_MESSAGE);
			}
		}

		private void addLabeledField(final JPanel panel, final String text, final JComponent field) {
			panel.add(new JLabel(text));
			panel.add(field);
			if (field instanceof final JTextField tf) {
				tf.addActionListener(ignored -> accept());
				tf.setActionCommand("Add Worker");
			}
		}

		public void revert() {
			name.setText("");
			for (final SpinnerNumberModel field : Arrays.asList(hpModel, maxHP, strength,
					dexterity, constitution, intelligence, wisdom, charisma)) {
				field.setValue(-1);
			}
			race.setText(RaceFactory.randomRace());
			dispose();
		}
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event.getActionCommand().toLowerCase().startsWith("add worker")) {
			workerCreationFrame.revert();
			workerCreationFrame.setVisible(true);
		}
	}

	/**
	 * Update our currently-selected-unit reference.
	 */
	@Override
	public void selectUnit(final @Nullable IUnit unit) {
		selectedUnit = unit;
	}
}
