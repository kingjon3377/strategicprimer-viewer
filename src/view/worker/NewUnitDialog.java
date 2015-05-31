package view.worker;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import model.listeners.NewUnitListener;
import model.listeners.NewUnitSource;
import model.listeners.PlayerChangeListener;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;

import org.eclipse.jdt.annotation.Nullable;

import util.IsNumeric;
import util.NullCleaner;
import view.util.ListenedButton;
import controller.map.misc.IDFactory;

/**
 * A panel to let the user add a new unit. We fire the "add" property with the
 * value of the unit if OK is pressed and both fields are nonempty, then clear
 * them. As this is a dialog, we do *not* extend ApplicationFrame.
 *
 * @author Jonathan Lovelace
 *
 */
public class NewUnitDialog extends JFrame implements ActionListener,
		NewUnitSource, PlayerChangeListener {
	/**
	 * The list of new-unit listeners listening to us.
	 */
	private final List<NewUnitListener> nuListeners = new ArrayList<>();

	/**
	 * The player to own created units.
	 */
	private Player owner;
	/**
	 * The factory to use to generate ID numbers.
	 */
	private final IDFactory idf;
	/**
	 * The field to let the user give the name of the unit.
	 */
	private final JTextField nameField = new JTextField(10);
	/**
	 * The field to let the user give the kind of unit.
	 */
	private final JTextField kindField = new JTextField(10);
	/**
	 * The field to let the user specify the unit's ID #.
	 */
	private final JFormattedTextField idField = new JFormattedTextField(
			NumberFormat.getIntegerInstance());
	/**
	 * Maximum and preferred height for the dialog.
	 */
	private static final int PREF_HEIGHT = 90;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger.getLogger(NewUnitDialog.class.getName()));
	/**
	 * Constructor.
	 *
	 * @param player the player to own the units
	 * @param idFactory a factory to generate ID numbers
	 */
	public NewUnitDialog(final Player player, final IDFactory idFactory) {
		super("Add a new unit");
		setLayout(new GridLayout(0, 2));

		owner = player;
		idf = idFactory;

		add(new JLabel("<html><b>Unit name:&nbsp;</b></html>"));
		add(setupField(nameField));

		add(new JLabel("<html><b>Kind of unit:&nbsp;</b></html>"));
		add(setupField(kindField));

		add(new JLabel("ID #: "));
		idField.setColumns(10);
		add(setupField(idField));

		add(new ListenedButton("OK", this));
		add(new ListenedButton("Cancel", this));

		setMinimumSize(new Dimension(150, 80));
		setPreferredSize(new Dimension(200, PREF_HEIGHT));
		setMaximumSize(new Dimension(300, PREF_HEIGHT));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	/**
	 * Set up a field so that pressing Enter there will press the OK button.
	 *
	 * @param field the field to set up
	 * @return the field
	 */
	private JTextField setupField(final JTextField field) {
		field.setActionCommand("OK");
		field.addActionListener(this);
		return field;
	}

	/**
	 * @param evt the event to handle
	 */
	@Override
	public final void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("OK".equals(evt.getActionCommand())) {
			final String name = nameField.getText().trim();
			final String kind = kindField.getText().trim();
			if (name.isEmpty()) {
				nameField.requestFocusInWindow();
			} else if (kind.isEmpty()) {
				kindField.requestFocusInWindow();
			} else {
				String reqId = NullCleaner.assertNotNull(idField.getText().trim());
				int idNum;
				if (IsNumeric.isNumeric(reqId)) {
					try {
						idNum = NumberFormat.getIntegerInstance().parse(reqId).intValue();
						idf.register(idNum);
					} catch (ParseException e) {
						LOGGER.log(Level.INFO, "Parse error parsing user-specified ID", e);
						idNum = idf.createID();
					}
				} else {
					idNum = idf.createID();
				}
				final IUnit unit = new Unit(owner, kind,
						name, idNum);
				for (final NewUnitListener list : nuListeners) {
					list.addNewUnit(unit);
				}
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
	 * To change the owner of subsequent units.
	 *
	 * @param old the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public final void playerChanged(@Nullable final Player old,
			final Player newPlayer) {
		owner = newPlayer;
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public final void addNewUnitListener(final NewUnitListener list) {
		nuListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public final void removeNewUnitListener(final NewUnitListener list) {
		nuListeners.remove(list);
	}
}
