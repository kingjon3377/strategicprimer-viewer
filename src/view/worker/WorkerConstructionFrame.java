package view.worker;

import static util.IsNumeric.isNumeric;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.WorkerStats;
import util.SingletonRandom;
import view.util.BorderedPanel;
import controller.map.misc.IDFactory;
/**
 * A window to let the user add a new worker.
 * @author Jonathan Lovelace
 *
 */
public class WorkerConstructionFrame extends JFrame implements ActionListener {
	/**
	 * Constructor.
	 * @param idFac the ID factory to use to generate IDs.
	 */
	public WorkerConstructionFrame(final IDFactory idFac) {
		super("Create Worker");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		race.setText(RACES.get(SingletonRandom.RANDOM.nextInt(RACES.size())));
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
		final JButton ok = new JButton("Add Worker");
		ok.addActionListener(this);
		buttonPanel.add(ok);
		final JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttonPanel.add(cancel);
		setContentPane(new BorderedPanel().setNorth(textPanel)
				.setCenter(statsPanel).setSouth(buttonPanel));
		setMinimumSize(new Dimension(320, 240));
		pack();
	}
	/**
	 * Handle button presses.
	 * @param evt the action to handle.
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("Add Worker".equalsIgnoreCase(evt.getActionCommand())) {
			if (name.getText().isEmpty()
					|| race.getText().isEmpty()
					|| anyNonNumeric(hpBox.getText(), maxHP.getText(),
							str.getText(), dex.getText(), con.getText(),
							intel.getText(), wis.getText(), cha.getText())) {
				// TODO: Report the error to the user
			} else {
				final Worker retval = new Worker(name.getText(), race.getText(), idf.createID());
				retval.setStats(new WorkerStats(parseInt(hpBox),
						parseInt(maxHP), parseInt(str), parseInt(dex),
						parseInt(con), parseInt(intel), parseInt(wis),
						parseInt(cha)));
				firePropertyChange("worker", null, retval);
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
		return Integer.parseInt(box.getText());
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
	private final transient IDFactory idf;
	/**
	 * Add a label and a field to a panel.
	 * @param panel the panel to hold them
	 * @param text the text to put on the label
	 * @param field the text field, or similar, to add
	 */
	private static void addLabeledField(final JPanel panel, final String text, final JComponent field) {
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
	 * Fill a stat's text box with an appropriate randomly-generated one. Doesn't take race into account.
	 * @param stat the field to fill
	 */
	private static void createStat(final JTextField stat) {
		final Random rng = SingletonRandom.RANDOM;
		final int threeDeeSix = rng.nextInt(6) + rng.nextInt(6) + rng.nextInt(6) + 3;
		stat.setText(Integer.toString(threeDeeSix));
	}
	/**
	 * A list of races.
	 */
	private static final List<String> RACES = new ArrayList<>();
	static {
		RACES.add("dwarf");
		RACES.add("elf");
		RACES.add("gnome");
		RACES.add("half-elf");
		RACES.add("Danan");
		// ESCA-JAVA0076:
		while (RACES.size() < 20) {
			RACES.add("human");
		}
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
}
