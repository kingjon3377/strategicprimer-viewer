package view.character;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.character.SPCharacter;
import view.util.ApplyButtonHandler;
import view.util.Applyable;
/**
 * A panel to allow the user to view or edit a Character.
 * @author Jonathan Lovelace
 *
 */
public final class CharacterPanel extends JPanel implements Applyable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -4920419938470844999L;
	/**
	 * Constructor taking a value.
	 * @param chrc the character to edit
	 */
	public CharacterPanel(final SPCharacter chrc) {
		this();
		character = chrc;
		revert();
	}
	/**
	 * No-value constructor.
	 */
	public CharacterPanel() {
		super(new GridLayout(0,2));
		add(new JLabel("Name"));
		add(nameField);
		add(applyButton);
		add(revertButton);
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
	 * The ActionListener that keeps that code out of this class.
	 */
	private final transient ActionListener list = new ApplyButtonHandler(this);
	/**
	 * Apply changes from fields to object.
	 */
	public void apply() {
		if (character == null) {
			character = new SPCharacter(nameField.getText());
		} else {
			character.setName(nameField.getText());
		}
	}
	/**
	 * Revert changes to fields.
	 */
	public void revert() {
		if (character == null) {
			nameField.setText("");
		} else {
			nameField.setText(character.getName());
		}
	}
}
