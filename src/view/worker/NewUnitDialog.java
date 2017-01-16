package view.worker;

import controller.map.misc.IDRegistrar;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import model.listeners.NewUnitListener;
import model.listeners.NewUnitSource;
import model.listeners.PlayerChangeListener;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import org.eclipse.jdt.annotation.Nullable;
import util.IsNumeric;
import util.OnMac;
import util.TypesafeLogger;
import view.util.ListenedButton;
import view.util.SPDialog;

/**
 * A panel to let the user add a new unit. We fire the "add" property with the value of
 * the unit if OK is pressed and both fields are nonempty, then clear them. As this is a
 * dialog, we do *not* extend ApplicationFrame.
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
public final class NewUnitDialog extends SPDialog
		implements NewUnitSource, PlayerChangeListener {
	/**
	 * Maximum and preferred height for the dialog.
	 */
	private static final int PREF_HEIGHT = 90;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(
			NewUnitDialog.class);
	/**
	 * The list of new-unit listeners listening to us.
	 */
	private final Collection<NewUnitListener> nuListeners = new ArrayList<>();
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
	private final JFormattedTextField idField =
			new JFormattedTextField(NumberFormat.getIntegerInstance());
	/**
	 * The player to own created units.
	 */
	private Player owner;

	/**
	 * Constructor.
	 *
	 * @param player    the player to own the units
	 * @param idFactory a factory to generate ID numbers
	 */
	public NewUnitDialog(final Player player, final IDRegistrar idFactory) {
		super(null, "Add a new unit");
		setLayout(new GridLayout(0, 2));

		owner = player;

		final ActionListener okListener = evt -> {
			final String name = nameField.getText().trim();
			final String kind = kindField.getText().trim();
			if (name.isEmpty()) {
				nameField.requestFocusInWindow();
			} else if (kind.isEmpty()) {
				kindField.requestFocusInWindow();
			} else {
				final String reqId = idField.getText().trim();
				int idNum;
				if (IsNumeric.isNumeric(reqId)) {
					try {
						idNum = NumberFormat.getIntegerInstance().parse(reqId)
										.intValue();
						idFactory.register(idNum);
					} catch (final ParseException e) {
						LOGGER.log(Level.INFO,
								"Parse error parsing user-specified ID", e);
						idNum = idFactory.createID();
					}
				} else {
					idNum = idFactory.createID();
				}
				final IUnit unit = new Unit(owner, kind, name, idNum);
				for (final NewUnitListener list : nuListeners) {
					list.addNewUnit(unit);
				}
				nameField.setText("");
				kindField.setText("");
				setVisible(false);
				dispose();
			}
		};
		add(new JLabel("<html><b>Unit name:&nbsp;</b></html>"));
		add(setupField(nameField, okListener));

		add(new JLabel("<html><b>Kind of unit:&nbsp;</b></html>"));
		add(setupField(kindField, okListener));

		add(new JLabel("ID #: "));
		idField.setColumns(10);
		add(setupField(idField, okListener));

		final ListenedButton okButton = new ListenedButton("OK", okListener);
		add(okButton);
		final ListenedButton cancelButton = new ListenedButton("Cancel", evt -> {
			nameField.setText("");
			kindField.setText("");
			setVisible(false);
			dispose();
		});
		OnMac.makeButtonsSegmented(okButton, cancelButton);
		add(cancelButton);

		setMinimumSize(new Dimension(150, 80));
		setPreferredSize(new Dimension(200, PREF_HEIGHT));
		setMaximumSize(new Dimension(300, PREF_HEIGHT));
		pack();
	}

	/**
	 * Set up a field so that pressing Enter there will press the OK button.
	 *
	 * @param field the field to set up
	 * @param list  the listener to listen for Enter in the field.
	 * @return the field
	 */
	private static JTextField setupField(final JTextField field,
										 final ActionListener list) {
		field.setActionCommand("OK");
		field.addActionListener(list);
		return field;
	}

	/**
	 * To change the owner of subsequent units.
	 *
	 * @param old       the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		owner = newPlayer;
	}

	/**
	 * Add a listener.
	 * @param list a listener to add
	 */
	@Override
	public void addNewUnitListener(final NewUnitListener list) {
		nuListeners.add(list);
	}

	/**
	 * Remove a listener.
	 * @param list a listener to remove
	 */
	@Override
	public void removeNewUnitListener(final NewUnitListener list) {
		nuListeners.remove(list);
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

	/**
	 * A simple toString().
	 * @return a quasi-diagnostic String
	 */
	@Override
	public String toString() {
		return "NewUnitDialog for player " + owner;
	}
}
