package view.worker;

import controller.map.misc.IDRegistrar;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
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
import util.OnMac;
import util.Pair;
import util.SingletonRandom;
import util.TypesafeLogger;
import view.util.BorderedPanel;
import view.util.ErrorShower;
import view.util.ListenedButton;

import static util.IsNumeric.isNumeric;

/**
 * A window to let the user add a new worker. As this is a dialog, we do *not* extend
 * ApplicationFrame.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class WorkerConstructionFrame extends JFrame implements NewWorkerSource {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(WorkerConstructionFrame.class);
	/**
	 * Number parser.
	 */
	private static final NumberFormat NUM_PARSER = NumberFormat.getIntegerInstance();
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
	private final JTextField strength = new JTextField();
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
	public WorkerConstructionFrame(final IDRegistrar idFac) {
		super("Create Worker");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		race.setText(RaceFactory.getRace());

		final JPanel textPanel = new JPanel(new GridLayout(0, 2));
		addLabeledField(textPanel, "Worker Name:", name);
		addLabeledField(textPanel, "Worker Race:", race);

		final JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
		final ListenedButton addButton = new ListenedButton("Add Worker", evt -> {
			final String nameText = name.getText().trim();
			final String raceText = race.getText().trim();
			if (nameText.isEmpty() || raceText.isEmpty() ||
						areAnyNonNumeric(hpBox, maxHP, strength, dex, con, intel, wis,
								cha)) {
				ErrorShower.showErrorDialog(this, getErrorExplanation());
			} else {
				final Worker retval = new Worker(nameText, raceText, idFac.createID());
				try {
					retval.setStats(new WorkerStats(parseInt(hpBox), parseInt(maxHP),
														   parseInt(strength),
														   parseInt(dex), parseInt(con),
														   parseInt(intel), parseInt
																					(wis),
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
		});
		buttonPanel.add(addButton);
		final ListenedButton cancelButton = new ListenedButton("Cancel", evt -> {
			setVisible(false);
			dispose();
		});
		OnMac.makeButtonsSegmented(addButton, cancelButton);
		buttonPanel.add(cancelButton);
		setContentPane(new BorderedPanel(createStatsPanel(), textPanel, buttonPanel,
												null, null));
		setMinimumSize(new Dimension(320, 240));
		pack();
	}

	/**
	 * Create the stats panel.
	 * @return the initialized stats panel
	 */
	private JPanel createStatsPanel() {
		createStats();

		final JPanel statsPanel = new JPanel(new GridLayout(0, 4));
		addLabeledField(statsPanel, "HP:", hpBox);
		addLabeledField(statsPanel, "Max HP:", maxHP);
		addLabeledField(statsPanel, "Strength:", strength);
		addLabeledField(statsPanel, "Intelligence:", intel);
		addLabeledField(statsPanel, "Dexterity:", dex);
		addLabeledField(statsPanel, "Wisdom:", wis);
		addLabeledField(statsPanel, "Constitution:", con);
		addLabeledField(statsPanel, "Charisma:", cha);
		return statsPanel;
	}

	/**
	 * Given a text box and an explanation/title, return a pair of the box's text and
	 * the title.
	 * @param box a text box
	 * @param expl an explanation or title of it
	 * @return its contents, asserted to not be null
	 */
	private static Pair<String, String> getBoxText(final JTextField box, final String expl) {
		return Pair.of(box.getText(), expl);
	}

	/**
	 * The Pairs' first elements are supposedly-numeric strings (as the second elements
	 * are what they represent); uf any of the supposedly-numeric Srings are
	 * non-numeric, "such-and-such must be a number" is printed to the Formatter.
	 * @param numbers a sequence of Pairs of supposedly-numeric Strings and what they
	 *                represent.
	 * @param formatter a Formatter to write the explanation to
	 */
	@SafeVarargs
	private static void numericExplanation(final Formatter formatter,
											 final Pair<String, String>... numbers) {
		for (final Pair<String, String> number : numbers) {
			final String num = number.first().trim();
			if (!isNumeric(num)) {
				formatter.format("%s must be a number.%n", number.second());
			}
		}
	}

	/**
	 * Parse the text box's contents as an integer.
	 * @param box a text field
	 * @return the integer value of its text
	 * @throws ParseException on non-numeric input
	 */
	private static int parseInt(final JTextField box) throws ParseException {
		return NUM_PARSER.parse(box.getText().trim()).intValue();
	}

	/**
	 * Whether any of the text boxes contains non-numeric data.
	 * @param boxes a collection of text fields
	 * @return true if any of them contains non-numeric data
	 */
	@SuppressWarnings("QuestionableName")
	private static boolean areAnyNonNumeric(final JTextField... boxes) {
		return Stream.of(boxes).map(JTextField::getText).map(String::trim)
					   .anyMatch(string -> (string == null) || !isNumeric(string));
	}

	/**
	 * Add a label and a field to a panel.
	 *
	 * @param panel the panel to hold them
	 * @param text  the text to put on the label
	 * @param field the text field, or similar, to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addLabeledField(final JPanel panel, final String text,
										final JComponent field) {
		panel.add(new JLabel(text));
		panel.add(field);
	}

	/**
	 * Fill a stat's text box with an appropriate randomly-generated one. Doesn't take
	 * race into account.
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
	 * Produce an explanation of what's wrong with the user's input.
	 * @return an explanation of what's wrong with the user's input.
	 */
	private String getErrorExplanation() {
		final StringBuilder builder = new StringBuilder(50);
		try (final Formatter formatter = new Formatter(builder)) {
			if (name.getText().trim().isEmpty()) {
				formatter.format("Worker needs a name.%n");
			}
			if (race.getText().trim().isEmpty()) {
				formatter.format("Worker needs a race.%n");
			}
			numericExplanation(formatter, getBoxText(hpBox, "HP"),
					getBoxText(maxHP, "Max HP"), getBoxText(strength, "Strength"),
					getBoxText(dex, "Dexterity"), getBoxText(con, "Constitution"),
					getBoxText(intel, "Intelligence"), getBoxText(wis, "Wisdom"),
					getBoxText(cha, "Charisma"));
		}
		return builder.toString();
	}

	/**
	 * Randomly generate stats.
	 */
	private void createStats() {
		hpBox.setText("8");
		maxHP.setText("8");
		createSingleStat(strength);
		createSingleStat(dex);
		createSingleStat(con);
		createSingleStat(intel);
		createSingleStat(wis);
		createSingleStat(cha);
	}

	/**
	 * Add a new-worker listener.
	 * @param list a listener to add
	 */
	@Override
	public void addNewWorkerListener(final NewWorkerListener list) {
		nwListeners.add(list);
	}

	/**
	 * Remove a new-worker listener.
	 * @param list a listener to remove
	 */
	@Override
	public void removeNewWorkerListener(final NewWorkerListener list) {
		nwListeners.remove(list);
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
