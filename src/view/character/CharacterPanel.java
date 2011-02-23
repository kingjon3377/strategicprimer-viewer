package view.character;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.character.AdminStatsImpl;
import model.character.CharStats;
import model.character.SPCharacter;
import view.util.ApplyButtonHandler;
import view.util.Applyable;
import view.util.ChangeableComponent;
import view.util.ConstraintHelper;
import view.util.IsAdmin;
import view.util.SaveableOpenable;
import controller.character.CharacterWriter;

/**
 * A panel to allow the user to view or edit a Character.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class CharacterPanel extends JPanel implements Applyable, SaveableOpenable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -4920419938470844999L;

	/**
	 * Constructor taking a value.
	 * 
	 * @param chrc
	 *            the character to edit
	 */
	public CharacterPanel(final SPCharacter chrc) {
		this();
		character = chrc;
		revert();
	}
	/**
	 * A panel to put Jobs in.
	 */
	private JPanel jobsPanel;
	/**
	 * No-value constructor.
	 */
	public CharacterPanel() {
		super(new GridBagLayout());
		add(new JLabel("Name"), new ConstraintHelper(0, 0));
		GridBagConstraints nameConstraints = new GridBagConstraints();
		add(nameField, new ConstraintHelper(1, 0));
		/**
		 * Labels describing the statistics.
		 */
		final JLabel[] statLabels = new JLabel[CharStats.Stat.values().length];
		for (CharStats.Stat stat : CharStats.Stat.values()) {
			statLabels[stat.ordinal()] = new JLabel(stat.toString()); // NOPMD
			add(statLabels[stat.ordinal()], new ConstraintHelper(0, stat.ordinal() + 1));
			if (IsAdmin.IS_ADMIN) {
				statComps[stat.ordinal()] = new ChangeableComponent(new JTextField(3)); // NOPMD
			} else {
				statComps[stat.ordinal()] = new ChangeableComponent(new JLabel()); // NOPMD
			}
			add(statComps[stat.ordinal()], new ConstraintHelper(1, stat.ordinal() + 1));
		}
		jobsPanel = new JobsPanel(this);
		add(jobsPanel, new ConstraintHelper(0, CharStats.Stat.values().length + 2, 2, 1));
		add(applyButton, new ConstraintHelper(0, CharStats.Stat.values().length + 3));
		add(revertButton, new ConstraintHelper(1, CharStats.Stat.values().length + 3));
		applyButton.addActionListener(list);
		revertButton.addActionListener(list);
	}

	/**
	 * The character we're editing.
	 */
	private SPCharacter character;
	/**
	 * A text box for the character's name.
	 */
	private final JTextField nameField = new JTextField();
	/**
	 * Apply button
	 */
	private final JButton applyButton = new JButton("Apply");
	/**
	 * Revert button
	 */
	private final JButton revertButton = new JButton("Revert");
	/**
	 * The components (JLabels if in player mode, JTextFields or JComboBoxes if
	 * in admin mode) that display the statistics.
	 */
	private final ChangeableComponent[] statComps = new ChangeableComponent[CharStats.Stat
			.values().length];
	/**
	 * The ActionListener that keeps that code out of this class.
	 */
	private final transient ActionListener list = new ApplyButtonHandler(this);

	/**
	 * Apply changes from fields to object.
	 */
	@Override
	public void apply() {
		if (character == null) {
			character = new SPCharacter(nameField.getText());
		} else {
			character.setName(nameField.getText());
		}
		if (statComps[0].getComponent() instanceof JTextField) {
			final AdminStatsImpl stats = (AdminStatsImpl) character.getStats();
			for (CharStats.Stat stat : CharStats.Stat.values()) {
				try {
				stats.setStat(stat, Integer
						.parseInt(((JTextField) statComps[stat.ordinal()].getComponent())
								.getText()));
				} catch (final NumberFormatException except) {
					continue;
				}
			}
		} else if (statComps[0].getComponent() instanceof JComboBox) {
			final CharStats stats = character.getStats();
			for (CharStats.Stat stat : CharStats.Stat.values()) {
				stats.setStat(stat,
						(CharStats.Attribute) ((JComboBox) statComps[stat
								.ordinal()].getComponent()).getSelectedItem());
			}
		}
	}

	/**
	 * Revert changes to fields.
	 */
	@Override
	public void revert() {
		if (character == null) {
			nameField.setText("");
		} else {
			nameField.setText(character.getName());
			if (IsAdmin.IS_ADMIN) {
				adminRevert();
			} else {
				final CharStats stats = character.getStats();
				for (CharStats.Stat stat : CharStats.Stat.values()) {
					((JLabel) statComps[stat.ordinal()].getComponent()).setText(stats.getStat(
							stat).toString());
				}
			}
		}
	}

	/**
	 * Revert changes to fields in admin mode.
	 */
	private void adminRevert() {
		final CharStats stats = character.getStats();
		if (stats instanceof AdminStatsImpl) {
			if (!(statComps[0].getComponent() instanceof JTextField)) {
				for (CharStats.Stat stat : CharStats.Stat.values()) {
					statComps[stat.ordinal()].setComponent(new JTextField()); // NOPMD
				}
			}
			for (CharStats.Stat stat : CharStats.Stat.values()) {
				((JTextField) statComps[stat.ordinal()].getComponent()).setText(Integer
						.toString(((AdminStatsImpl) stats).getStatValue(stat)));
			}
		} else {
			if (!(statComps[0].getComponent() instanceof JComboBox)) {
				for (CharStats.Stat stat : CharStats.Stat.values()) {
					statComps[stat.ordinal()].setComponent(new JComboBox( // NOPMD
							CharStats.Attribute.values()));
				}
			}
			for (CharStats.Stat stat : CharStats.Stat.values()) {
				((JComboBox) statComps[stat.ordinal()].getComponent()).setSelectedItem(stats
						.getStat(stat));
			}
		} 
	}
	/**
	 * @return the character we're editing 
	 */
	public SPCharacter getCharacter() {
		return character;
	}
	/**
	 * Required by SaveableOpenable spec.
	 * @param file ignored
	 */
	@Override
	public void open(final String file) {
		throw new IllegalStateException("Shouldn't get called");
	}
	/**
	 * Save the character to file.
	 * @param file the filename to save to
	 * @throws IOException on I/O error while saving
	 */
	@Override
	public void save(final String file) throws IOException {
		new CharacterWriter(file).write(character, !IsAdmin.IS_ADMIN);
	}
	/**
	 * Remove a Job.
	 * @param panel the panel holding the Job to remove
	 */
	protected void removeJob(final JobPanel panel) {
		// FIXME: implement
	}
}
