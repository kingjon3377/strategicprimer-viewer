package view.resources;

import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IDRegistrar;
import controller.map.misc.IOHandler;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.StreamSupport;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import model.map.Player;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.resources.ResourceManagementDriver;
import org.eclipse.jdt.annotation.Nullable;
import view.util.BoxPanel;
import view.util.FormattedLabel;
import view.util.ImprovedComboBox;
import view.util.ListenedButton;
import view.util.SPFrame;
import view.util.SplitWithWeights;
import view.util.StreamingLabel;
import view.worker.WorkerMenu;

/**
 * A window to let the user enter resources etc. Note that this is not a dialog to enter
 * one resource and close.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ResourceAddingFrame extends SPFrame {
	/**
	 * The label that we use to display diagnostics.
	 */
	private final StreamingLabel logLabel = new StreamingLabel();
	/**
	 * The combo box for resource kinds.
	 */
	private final UpdatedComboBox resKindBox = new UpdatedComboBox(logLabel);
	/**
	 * The model for the field giving the turn resources were created. See end of
	 * constructor for why the low maximum.
	 */
	private final SpinnerNumberModel resCreatedModel =
			new SpinnerNumberModel(-1, -1, 2000, 1);
	/**
	 * The combo box for resource types.
	 */
	private final UpdatedComboBox resourceBox = new UpdatedComboBox(logLabel);
	/**
	 * The model for the field for resource quantities. See end of constructor for why
	 * the low maximum.
	 */
	private final SpinnerNumberModel resQtyModel = new SpinnerNumberModel(0, 0, 2000, 1);
	/**
	 * The combo box for resource units.
	 */
	private final UpdatedComboBox resUnitsBox = new UpdatedComboBox(logLabel);
	/**
	 * The model for the spinner to add more than one identical implement. See end of
	 * constructor for why the low maximum.
	 */
	private final SpinnerNumberModel implQtyModel = new SpinnerNumberModel(1, 1, 2000,
																				  1);
	/**
	 * The field to let the user say how many identical implements to add.
	 */
	private final JSpinner implQtyField = new JSpinner(implQtyModel);
	/**
	 * The combo box for implement kinds.
	 */
	private final UpdatedComboBox implKindBox = new UpdatedComboBox(logLabel);
	/**
	 * The Player to use when the selected Player is null
	 */
	private final static Player NULL_PLAYER = new Player(-1, "");
	/**
	 * The current player.
	 */
	@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
	private Player current;
	/**
	 * Whether we have yet to ask the user to choose a player.
	 */
	private boolean playerIsDefault = true;

	/**
	 * Constructor.
	 *
	 * @param driverModel the driver model
	 * @param ioh         the I/O handler for menu items
	 */
	@SuppressWarnings("ObjectAllocationInLoop")
	public ResourceAddingFrame(final ResourceManagementDriver driverModel,
							   final IOHandler ioh) {
		super("Resource Entry", driverModel.getMapFile());
		final IDRegistrar idf = IDFactoryFiller.createFactory(driverModel);
		current = StreamSupport.stream(driverModel.getPlayers().spliterator(), false)
						  .filter(Player::isCurrent)
						  .findAny().orElse(NULL_PLAYER);
		final FormattedLabel resourceLabel =
				new FormattedLabel("Add resource for %s:", current.getName());
		final FormattedLabel implementLabel =
				new FormattedLabel("Add equipment for %s:", current.getName());
		ioh.addPlayerChangeListener(
				(final Player old, @Nullable final Player newPlayer) -> {
					if (newPlayer == null) {
						current = NULL_PLAYER;
					} else {
						current = newPlayer;
					}
					resourceLabel.setArgs(current.getName());
					implementLabel.setArgs(current.getName());
				});
		final BoxPanel mainPanel = new BoxPanel(false);
		mainPanel.add(resourceLabel);
		final JPanel panel = new BoxPanel(true);
		addPair(panel, new JLabel("General Category"), resKindBox);
		addPair(panel, new JLabel("Turn created"), new JSpinner(resCreatedModel));
		addPair(panel, new JLabel("Specific Resource"), resourceBox);
		addPair(panel, new JLabel("Quantity"), new JSpinner(resQtyModel));
		addPair(panel, new JLabel("Units"), resUnitsBox);
		final ActionListener resListener = evt -> {
			confirmPlayer(ioh);
			final String kind = resKindBox.getSelectedItem();
			final String resource = resourceBox.getSelectedItem();
			final String units = resUnitsBox.getSelectedItem();
			if (kind.isEmpty()) {
				resKindBox.requestFocusInWindow();
				return;
			} else if (resource.isEmpty()) {
				resourceBox.requestFocusInWindow();
				return;
			} else if (units.isEmpty()) {
				resUnitsBox.requestFocusInWindow();
				return;
			}
			final ResourcePile pile = new ResourcePile(idf.createID(), kind, resource,
															  resQtyModel.getNumber()
																	  .intValue(),
															  units);
			pile.setCreated(resCreatedModel.getNumber().intValue());
			driverModel.addResource(pile, current);
			logAddition(pile.toString());
			resKindBox.checkAndClear();
			resCreatedModel.setValue(Integer.valueOf(-1));
			resourceBox.checkAndClear();
			resQtyModel.setValue(Integer.valueOf(0));
			resUnitsBox.checkAndClear();
			resKindBox.requestFocusInWindow();
		};
		addPair(panel, new JLabel(""), new ListenedButton("Add Resource", resListener));
		// A listener on the combo box fires on every change to the selection
		resUnitsBox.addSubmitListener(resListener);
		mainPanel.add(panel);
		mainPanel.addGlue();
		mainPanel.add(implementLabel);
		final JPanel secondPanel = new BoxPanel(true);
		secondPanel.add(implQtyField);
		secondPanel.add(implKindBox);
		final ActionListener implListener = evt -> {
			confirmPlayer(ioh);
			final String kind = implKindBox.getSelectedItem();
			if (kind.isEmpty()) {
				return;
			}
			final int qty = implQtyModel.getNumber().intValue();
			for (int i = 0; i < qty; i++) {
				driverModel.addResource(new Implement(kind, idf.createID()), current);
			}
			logAddition(Integer.toString(qty) + " x " + kind);
			implQtyModel.setValue(Integer.valueOf(1));
			implKindBox.checkAndClear();
			implQtyField.requestFocusInWindow();
		};
		implKindBox.addSubmitListener(implListener);
		secondPanel.add(new ListenedButton("Add Equipment", implListener));
		mainPanel.add(secondPanel);
		mainPanel.addGlue();
		logLabel.setMinimumSize(new Dimension(getWidth() - 20, 50));
		logLabel.setPreferredSize(new Dimension(getWidth(), 100));
		final JScrollPane scrolledLog = new JScrollPane(logLabel);
		scrolledLog.setMinimumSize(logLabel.getMinimumSize());
		add(SplitWithWeights.verticalSplit(0.2, 0.1, mainPanel, scrolledLog));
		setJMenuBar(new WorkerMenu(ioh, this, driverModel));
		pack();
		// If we set these at model creation, the fields would (try to) be unnecessarily
		// large. Not that this helps.
		resCreatedModel.setMaximum(Integer.valueOf(Integer.MAX_VALUE));
		resQtyModel.setMaximum(Integer.valueOf(Integer.MAX_VALUE));
		implQtyModel.setMaximum(Integer.valueOf(Integer.MAX_VALUE));
	}

	/**
	 * Add two components in a panel joining them vertically.
	 *
	 * @param container       the container to add the panel containing the two
	 *                           components
	 *                        to
	 * @param firstComponent  the first component
	 * @param secondComponent the second component
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addPair(final Container container,
								final Component firstComponent,
								final Component secondComponent) {
		final JPanel panel = new BoxPanel(false);
		panel.add(Box.createVerticalGlue());
		panel.add(firstComponent);
		panel.add(Box.createVerticalGlue());
		panel.add(secondComponent);
		panel.add(Box.createVerticalGlue());
		container.add(panel);
	}

	/**
	 * Ask the user to choose a player, if the current player is unlikely to be what he
	 * or she wants and we haven't already done so.
	 *
	 * @param ioh the menu handler to use to show the dialog
	 */
	private void confirmPlayer(final IOHandler ioh) {
		if (playerIsDefault && current.getName().trim().isEmpty()) {
			ioh.actionPerformed(new ActionEvent(this, 1, "change current player"));
		}
		playerIsDefault = false;
	}

	/**
	 * Log the addition of something.
	 *
	 * @param addend what was added
	 */
	@SuppressWarnings("resource")
	private void logAddition(final String addend) {
		try (final PrintWriter writer = logLabel.getWriter()) {
			writer.print("<p style=\"color:white; margin-bottom: 0.5em; ");
			writer.printf(" margin-top: 0.5em;\">Added %s for %s</p>%n", addend,
					current.getName());
		}
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * @return a quasi-diagnostic String
	 */
	@Override
	public String toString() {
		return "ResourceAddingFrame with current player " + current;
	}

	/**
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Resource Entry";
	}

	/**
	 * Extends ImprovedComboBox to keep a running collection of values.
	 */
	private static class UpdatedComboBox extends ImprovedComboBox<String> {
		/**
		 * The values we've had in the past.
		 */
		private final Collection<String> values = new HashSet<>();
		/**
		 * The label that we can log to.
		 */
		private final StreamingLabel logLabel;

		/**
		 * Constructor. We need it to be neither private nor public for this to
		 * work with as few warnings as possible as a private inner class, and
		 * it needs to do something to not be an empty method, so we moved the
		 * initialization of the collection here.
		 * @param logger the label to log to
		 */
		protected UpdatedComboBox(final StreamingLabel logger) {
			logLabel = logger;
		}

		/**
		 * Clear the combo box, but if its value was one we haven't had previously, add
		 * it to the drop-down list.
		 */
		@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
		public void checkAndClear() {
			final String item = getSelectedItem().trim();
			if (!values.contains(item)) {
				values.add(item);
				addItem(item);
			}
			setSelectedItem(null);
		}

		/**
		 * Prevent serialization.
		 *
		 * @param out ignored
		 * @throws IOException always
		 */
		@SuppressWarnings({"unused", "static-method"})
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * Prevent serialization
		 *
		 * @param in ignored
		 * @throws IOException            always
		 * @throws ClassNotFoundException never
		 */
		@SuppressWarnings({"unused", "static-method"})
		private void readObject(final ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * @return a quasi-diagnostic String
		 */
		@Override
		public String toString() {
			return "UpdatedComboBox with " + values.size() + " items";
		}

		/**
		 * @return the selected item, as a String
		 */
		@Override
		public String getSelectedItem() {
			final Object retval = super.getSelectedItem();
			if (retval == null) {
				return "";
			} else if (retval instanceof String) {
				return ((String) retval).trim();
			} else {
				return retval.toString().trim();
			}
		}
		/**
		 * {@link JComboBox#addActionListener(ActionListener)} is *not* what we usually
		 * want; {@link JComboBox} will notify its ActionListeners every time the
		 * selected value changes. This is a somewhat-hackish method to do what we
		 * usually want.
		 * @param listener a listener to add.
		 */
		public void addSubmitListener(final ActionListener listener) {
			final Component inner = getEditor().getEditorComponent();
			if (inner instanceof JTextField) {
				((JTextField) inner).addActionListener(listener);
			} else {
				// logLabel.getWriter() returns a PrintWriter whose close() is a no-op.
				try (final PrintWriter logger = logLabel.getWriter()) {
					logger.print("Editor wasn't a text field, but a ");
					logger.println(inner.getClass().getCanonicalName());
				}
			}
		}
	}
}
