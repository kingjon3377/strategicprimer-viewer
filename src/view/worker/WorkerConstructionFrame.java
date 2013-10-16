package view.worker;

import static util.IsNumeric.isNumeric;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.listeners.NewWorkerListener;
import model.listeners.NewWorkerSource;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.workermgmt.RaceFactory;

import org.eclipse.jdt.annotation.Nullable;

import util.IsNumeric;
import util.Pair;
import util.SingletonRandom;
import view.util.BorderedPanel;
import view.util.ErrorShower;
import view.util.ListenedButton;
import controller.map.misc.IDFactory;

/**
 * A window to let the user add a new worker.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerConstructionFrame extends JFrame implements ActionListener,
		NewWorkerSource {
	/**
	 * Constructor.
	 *
	 * @param idFac the ID factory to use to generate IDs.
	 */
	public WorkerConstructionFrame(final IDFactory idFac) {
		super("Create Worker");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		race.setText(RaceFactory.getRace());
		idf = idFac;

		final JPanel textPanel = new JPanel(new GridLayout(0, 2));
		addLabeledField(textPanel, "Worker Name:", name);
		addLabeledField(textPanel, "Worker Race:", race);

		final JPanel statsPanel = new JPanel(new GridLayout(0, 4));
		createStats();
		addLabeledField(statsPanel, "HP:", hpBox);
		addLabeledField(statsPanel, "Max HP:", maxHP);
		addLabeledField(statsPanel, "Strength:", str);
		addLabeledField(statsPanel, "Intelligence:", intel);
		addLabeledField(statsPanel, "Dexterity:", dex);
		addLabeledField(statsPanel, "Wisdom:", wis);
		addLabeledField(statsPanel, "Constitution:", con);
		addLabeledField(statsPanel, "Charisma:", cha);

		final JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
		buttonPanel.add(new ListenedButton("Add Worker", this));
		buttonPanel.add(new ListenedButton("Cancel", this));
		setContentPane(new BorderedPanel(statsPanel, textPanel, buttonPanel,
				null, null));
		setMinimumSize(new Dimension(320, 240));
		pack();
	}

	/**
	 * @return an explanation of what's wrong with the user's input.
	 */
	private String getErrorExpl() {
		final StringBuilder builder = new StringBuilder();
		if (name.getText().trim().isEmpty()) {
			builder.append("Worker needs a name.\n");
		}
		if (race.getText().trim().isEmpty()) {
			builder.append("Worker needs a race.\n");
		}
		builder.append(numericExpl(Pair.of(hpBox.getText(), "HP"),
				Pair.of(maxHP.getText(), "Max HP"),
				Pair.of(str.getText(), "Strength"),
				Pair.of(dex.getText(), "Dexterity"),
				Pair.of(con.getText(), "Constitution"),
				Pair.of(intel.getText(), "Intelligence"),
				Pair.of(wis.getText(), "Wisdom"),
				Pair.of(cha.getText(), "Charisma")));
		return builder.toString();
	}

	/**
	 * @param numbers a sequence of Pairs of supposedly-numeric Strings and what
	 *        they represent. If any is nonnumeric, the return String includes
	 *        "such-and-such must be a number."
	 * @return such an explanation
	 */
	@SafeVarargs
	private static String numericExpl(final Pair<String, String>... numbers) {
		final StringBuilder builder = new StringBuilder();
		for (final Pair<String, String> number : numbers) {
			if (!IsNumeric.isNumeric(number.first().trim())) {
				builder.append(number.second());
				builder.append(" must be a number.\n");
			}
		}
		return builder.toString();
	}

	/**
	 * Handle button presses.
	 *
	 * @param evt the action to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("Add Worker".equalsIgnoreCase(evt.getActionCommand())) {
			if (name.getText().trim().isEmpty()
					|| race.getText().trim().isEmpty()
					|| anyNonNumeric(hpBox.getText().trim(), maxHP.getText()
							.trim(), str.getText().trim(),
							dex.getText().trim(), con.getText().trim(), intel
									.getText().trim(), wis.getText().trim(),
							cha.getText().trim())) {
				ErrorShower.showErrorDialog(this, getErrorExpl());
			} else {
				final Worker retval = new Worker(name.getText().trim(), race
						.getText().trim(), idf.createID());
				retval.setStats(new WorkerStats(parseInt(hpBox),
						parseInt(maxHP), parseInt(str), parseInt(dex),
						parseInt(con), parseInt(intel), parseInt(wis),
						parseInt(cha)));
				for (final NewWorkerListener list : nwListeners) {
					list.addNewWorker(retval);
				}
				setVisible(false);
				dispose();
			}
		} else if ("Cancel".equalsIgnoreCase(evt.getActionCommand())) {
			setVisible(false);
			dispose();
		}
	}

	/**
	 * @param box a text field
	 * @return the integer value of its text
	 */
	private static int parseInt(final JTextField box) {
		return Integer.parseInt(box.getText().trim());
	}

	/**
	 * @param strings a collection of strings
	 * @return true if any of them is nonnumeric
	 */
	private static boolean anyNonNumeric(final String... strings) {
		for (final String string : strings) {
			if (!isNumeric(string)) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * The ID factory to use to generate IDs.
	 */
	private final IDFactory idf;

	/**
	 * Add a label and a field to a panel.
	 *
	 * @param panel the panel to hold them
	 * @param text the text to put on the label
	 * @param field the text field, or similar, to add
	 */
	private static void addLabeledField(final JPanel panel, final String text,
			final JComponent field) {
		panel.add(new JLabel(text));
		panel.add(field);
	}

	/**
	 * Randomly generate stats.
	 */
	private void createStats() {
		hpBox.setText("8");
		maxHP.setText("8");
		createStat(str);
		createStat(dex);
		createStat(con);
		createStat(intel);
		createStat(wis);
		createStat(cha);
	}

	/**
	 * Fill a stat's text box with an appropriate randomly-generated one.
	 * Doesn't take race into account.
	 *
	 * @param stat the field to fill
	 */
	private static void createStat(final JTextField stat) {
		final Random rng = SingletonRandom.RANDOM;
		final int threeDeeSix = rng.nextInt(6) + rng.nextInt(6)
				+ rng.nextInt(6) + 3;
		stat.setText(Integer.toString(threeDeeSix));
	}

	/**
	 * The 'name' field.
	 */
	private final JTextField name = new JTextField();
	/**
	 * The 'race' field.
	 */
	private final JTextField race = new JTextField();
	/**
	 * The text box representing the worker's HP.
	 */
	private final JTextField hpBox = new JTextField();
	/**
	 * The text box representing the worker's max HP.
	 */
	private final JTextField maxHP = new JTextField();
	/**
	 * The text box representing the worker's strength.
	 */
	private final JTextField str = new JTextField();
	/**
	 * The text box representing the worker's dexterity.
	 */
	private final JTextField dex = new JTextField();
	/**
	 * The text box representing the worker's constitution.
	 */
	private final JTextField con = new JTextField();
	/**
	 * The text box representing the worker's intelligence.
	 */
	private final JTextField intel = new JTextField();
	/**
	 * The text box representing the worker's wisdom.
	 */
	private final JTextField wis = new JTextField();
	/**
	 * The text box representing the worker's charisma.
	 */
	private final JTextField cha = new JTextField();
	/**
	 * The list of listeners to notify on worker creation.
	 */
	private final List<NewWorkerListener> nwListeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addNewWorkerListener(final NewWorkerListener list) {
		nwListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeNewWorkerListener(final NewWorkerListener list) {
		nwListeners.remove(list);
	}
}
