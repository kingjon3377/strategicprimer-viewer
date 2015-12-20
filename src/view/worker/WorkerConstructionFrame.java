package view.worker;

import static util.IsNumeric.isNumeric;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.misc.IDFactory;
import model.listeners.NewWorkerListener;
import model.listeners.NewWorkerSource;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.workermgmt.RaceFactory;
import util.NullCleaner;
import util.Pair;
import util.SingletonRandom;
import view.util.BorderedPanel;
import view.util.ErrorShower;
import view.util.ListenedButton;

/**
 * A window to let the user add a new worker. As this is a dialog, we do *not*
 * extend ApplicationFrame.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class WorkerConstructionFrame extends JFrame implements ActionListener,
		NewWorkerSource {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger.getLogger(WorkerConstructionFrame.class.getName()));
	/**
	 * The ID factory to use to generate IDs.
	 */
	private final IDFactory idf;

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
	private final Collection<NewWorkerListener> nwListeners = new ArrayList<>();
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

		createStats();
		final JPanel statsPanel = new JPanel(new GridLayout(0, 4));
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
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}
	/**
	 * @param box a text box
	 * @return its contents, asserted to not be null
	 */
	private static String getBoxText(final JTextField box) {
		final String string = box.getText();
		return NullCleaner.assertNotNull(string);
	}
	/**
	 * @return an explanation of what's wrong with the user's input.
	 */
	private String getErrorExpl() {
		final StringBuilder builder = new StringBuilder(50);
		if (name.getText().trim().isEmpty()) {
			builder.append("Worker needs a name.\n");
		}
		if (race.getText().trim().isEmpty()) {
			builder.append("Worker needs a race.\n");
		}
		builder.append(numericExpl(Pair.of(getBoxText(hpBox), "HP"),
				Pair.of(getBoxText(maxHP), "Max HP"),
				Pair.of(getBoxText(str), "Strength"),
				Pair.of(getBoxText(dex), "Dexterity"),
				Pair.of(getBoxText(con), "Constitution"),
				Pair.of(getBoxText(intel), "Intelligence"),
				Pair.of(getBoxText(wis), "Wisdom"),
				Pair.of(getBoxText(cha), "Charisma")));
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param numbers a sequence of Pairs of supposedly-numeric Strings and what
	 *        they represent. If any is nonnumeric, the return String includes
	 *        "such-and-such must be a number."
	 * @return such an explanation
	 */
	@SafeVarargs
	private static String numericExpl(final Pair<String, String>... numbers) {
		final StringBuilder builder = new StringBuilder(40);
		for (final Pair<String, String> number : numbers) {
			final String num = NullCleaner.assertNotNull(number.first().trim());
			if (!isNumeric(num)) {
				builder.append(number.second());
				builder.append(" must be a number.\n");
			}
		}
		return NullCleaner.assertNotNull(builder.toString());
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
			final String nameText = name.getText().trim();
			final String raceText = race.getText().trim();
			if (nameText.isEmpty()
					    || raceText.isEmpty()
					    || anyNonNumeric(hpBox.getText().trim(), maxHP.getText()
							                                             .trim(), str.getText().trim(),
					dex.getText().trim(), con.getText().trim(), intel
							                                            .getText().trim(), wis.getText().trim(),
					cha.getText().trim())) {
				ErrorShower.showErrorDialog(this, getErrorExpl());
			} else {
				final Worker retval = new Worker(nameText, raceText,
						idf.createID());
				try {
					retval.setStats(new WorkerStats(parseInt(hpBox),
							parseInt(maxHP), parseInt(str), parseInt(dex),
							parseInt(con), parseInt(intel), parseInt(wis),
							parseInt(cha)));
				} catch (final ParseException e) {
					LOGGER.log(Level.FINE, "Non-numeric input", e);
					ErrorShower.showErrorDialog(this, "All stats must be numbers");
					return;
				}
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
	 * Number parser.
	 */
	private static final NumberFormat NUM_PARSER = NullCleaner
			.assertNotNull(NumberFormat.getIntegerInstance());
	/**
	 * @param box a text field
	 * @return the integer value of its text
	 * @throws ParseException on non-numeric input
	 */
	private static int parseInt(final JTextField box) throws ParseException {
		return NUM_PARSER.parse(box.getText().trim()).intValue();
	}

	/**
	 * @param strings a collection of strings
	 * @return true if any of them is nonnumeric
	 */
	private static boolean anyNonNumeric(final String... strings) {
		return Stream.of(strings).anyMatch(string -> string == null || !isNumeric(string));
	}

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
		createSingleStat(str);
		createSingleStat(dex);
		createSingleStat(con);
		createSingleStat(intel);
		createSingleStat(wis);
		createSingleStat(cha);
	}

	/**
	 * Fill a stat's text box with an appropriate randomly-generated one.
	 * Doesn't take race into account.
	 *
	 * @param stat the field to fill
	 */
	private static void createSingleStat(final JTextField stat) {
		final Random rng = SingletonRandom.RANDOM;
		final int threeDeeSix = rng.nextInt(6) + rng.nextInt(6)
				+ rng.nextInt(6) + 3;
		stat.setText(Integer.toString(threeDeeSix));
	}

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
