package view.worker;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Player;
import model.map.fixtures.mobile.Unit;
import util.PropertyChangeSource;
import view.util.ListenedButton;
import controller.map.misc.IDFactory;

/**
 * A panel to let the user add a new unit. We fire the "add" property with the
 * value of the unit if OK is pressed and both fields are nonempty, then clear
 * them.
 *
 * @author Jonathan Lovelace
 *
 */
public class NewUnitDialog extends JFrame implements ActionListener, PropertyChangeSource, PropertyChangeListener {
	/**
	 * Maximum and preferred height for the dialog.
	 */
	private static final int PREF_HEIGHT = 90;
	/**
	 * Constructor.
	 * @param player the player to own the units
	 * @param idFactory a factory to generate ID numbers
	 */
	public NewUnitDialog(final Player player, final IDFactory idFactory) {
		super("Add a new unit");
		setLayout(new GridLayout(0, 2));

		owner = player;
		idf = idFactory;

		add(new JLabel("Unit name: "));
		add(setupField(nameField));

		add(new JLabel("Kind of unit: "));
		add(setupField(kindField));

		add(new ListenedButton("OK", this));
		add(new ListenedButton("Cancel", this));

		setMinimumSize(new Dimension(150, 80));
		setPreferredSize(new Dimension(200, PREF_HEIGHT));
		setMaximumSize(new Dimension(300, PREF_HEIGHT));
		pack();
	}
	/**
	 * Set up a field so that pressing Enter there will press the OK button.
	 * @param field the field to set up
	 * @return the field
	 */
	private JTextField setupField(final JTextField field) {
		field.setActionCommand("OK");
		field.addActionListener(this);
		return field;
	}
	/**
	 * The player to own created units.
	 */
	private Player owner;
	/**
	 * The factory to use to generate ID numbers.
	 */
	private final transient IDFactory idf;
	/**
	 * The field to let the user give the name of the unit.
	 */
	private final JTextField nameField = new JTextField(10);
	/**
	 * The field to let the user give the kind of unit.
	 */
	private final JTextField kindField = new JTextField(10);
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("OK".equals(evt.getActionCommand())) {
			if (nameField.getText().trim().isEmpty()) {
				nameField.requestFocusInWindow();
			} else if (kindField.getText().trim().isEmpty()) {
				kindField.requestFocusInWindow();
			} else {
				firePropertyChange("unit", null,
						new Unit(owner, kindField.getText().trim(), nameField
								.getText().trim(), idf.createID()));
				nameField.setText("");
				kindField.setText("");
				setVisible(false);
			}
		} else if ("Cancel".equals(evt.getActionCommand())) {
			nameField.setText("");
			kindField.setText("");
			setVisible(false);
		}
	}
	/**
	 * We listen for the "player" property, to change the owner of subsequent units.
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(@Nullable final PropertyChangeEvent evt) {
		if (evt != null && "player".equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Player) {
			owner = (Player) evt.getNewValue();
		}
	}
}
