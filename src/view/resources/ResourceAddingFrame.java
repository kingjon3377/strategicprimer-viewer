package view.resources;

import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.StreamSupport;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.map.Player;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.resources.ResourceManagementDriver;
import view.util.BoxPanel;
import view.util.ErrorShower;
import view.worker.WorkerMenu;

/**
 * A window to let the user enter resources etc. Note that this is not a dialog to enter
 * one resource and close.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ResourceAddingFrame extends JFrame {
	/**
	 * The driver model.
	 */
	private final ResourceManagementDriver model;
	/**
	 * The current player.
	 */
	@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
	private Player current;
	/**
	 * The "resource" label.
	 */
	private final JLabel resourceLabel;
	/**
	 * The "implement" label.
	 */
	private final JLabel implementLabel;
	/**
	 * The combo box for resource kinds.
	 */
	private final UpdatedComboBox resKindBox = new UpdatedComboBox();
	/**
	 * The parser for integers.
	 */
	private final NumberFormat nf = NumberFormat.getIntegerInstance();
	/**
	 * The text field for the turn resources were created
	 */
	private final JFormattedTextField resCreatedField = new JFormattedTextField(nf);
	/**
	 * The combo box for resource types.
	 */
	private final UpdatedComboBox resourceBox = new UpdatedComboBox();
	/**
	 * The text field for resource quantities.
	 */
	private final JFormattedTextField resQtyField = new JFormattedTextField(nf);
	/**
	 * The combo box for resource units.
	 */
	private final UpdatedComboBox resUnitsBox = new UpdatedComboBox();
	/**
	 * The combo box for implement kinds.
	 */
	private final UpdatedComboBox implKindBox = new UpdatedComboBox();

	/**
	 * Constructor.
	 * @param dmodel the driver model
	 * @param ioh the I/O handler for menu items
	 */
	public ResourceAddingFrame(final ResourceManagementDriver dmodel, final IOHandler ioh) {
		super("Resource Entry");
		model = dmodel;
		final IDFactory idf = IDFactoryFiller.createFactory(model);
		current = StreamSupport.stream(dmodel.getPlayers().spliterator(), false)
						  .filter(player -> player.isCurrent())
						  .findAny().orElse(new Player(-1, ""));
		resourceLabel =
				new JLabel(String.format("Add resource for %s:", current.getName()));
		implementLabel =
				new JLabel(String.format("Add equipment for %s:", current.getName()));
		ioh.addPlayerChangeListener((old, newPlayer) -> {
			if (newPlayer == null) {
				current = new Player(-1, "");
			} else {
				current = newPlayer;
			}
			resourceLabel
					.setText(String.format("Add resource for %s:", current.getName()));
			implementLabel
					.setText(String.format("Add equipment for %s:", current.getName()));
		});
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		add(resourceLabel);
		final JPanel panel = new BoxPanel(true);
		addPair(panel, new JLabel("General Category"), resKindBox);
		addPair(panel, new JLabel("Turn created"), resCreatedField);
		addPair(panel, new JLabel("Specific Resource"), resourceBox);
		addPair(panel, new JLabel("Quantity"), resQtyField);
		addPair(panel, new JLabel("Units"), resUnitsBox);
		final JButton resourceButton = new JButton("Add Resource");
		addPair(panel, new JLabel(""), resourceButton);
		final Component outer = this;
		resourceButton.addActionListener(evt -> {
			try {
				final String kind = resKindBox.getSelectedItem().toString().trim();
				final String resource = resourceBox.getSelectedItem().toString().trim();
				final String units = resUnitsBox.getSelectedItem().toString().trim();
				final ResourcePile pile = new ResourcePile(idf.createID(), kind, resource,
															nf.parse(resQtyField
																			 .getText()
																			 .trim())
																	.intValue(),
															units);
				pile.setCreated(nf.parse(resCreatedField.getText().trim()).intValue());
				model.addResource(pile, current);
				resKindBox.checkAndClear();
				resCreatedField.setText("");
				resourceBox.checkAndClear();
				resQtyField.setText("");
				resUnitsBox.checkAndClear();
				resKindBox.requestFocusInWindow();
			} catch (final ParseException ignored) {
				ErrorShower.showErrorDialog(outer, "Quantity must be numeric");
			}
		});
		add(panel);
		add(Box.createVerticalGlue());
		add(implementLabel);
		final JPanel secondPanel = new BoxPanel(true);
		secondPanel.add(implKindBox);
		final JButton implButton = new JButton("Add Equipment");
		implButton.addActionListener(evt -> {
			final String kind = implKindBox.getSelectedItem().toString().trim();
			model.addResource(new Implement(idf.createID(), kind), current);
			implKindBox.checkAndClear();
			implKindBox.requestFocusInWindow();
		});
		secondPanel.add(implButton);
		add(secondPanel);
		add(Box.createVerticalGlue());
		setJMenuBar(new WorkerMenu(ioh, this, model));
		pack();
	}

	/**
	 * Add two components in a panel joining them vertically.
	 * @param container the container to add the panel containing the two components to
	 * @param firstComponent the first component
	 * @param secondComponent the second component
	 */
	private static void addPair(final Container container, final Component firstComponent,
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
	 * A combo-box that handles 'enter' properly.
	 *
	 * TODO: Should handle Tab as well, if it doesn't already.
	 *
	 * TODO: The central functionality of this should be a top-level class in view.util
	 */
	private static class UpdatedComboBox extends JComboBox<String> {
		protected UpdatedComboBox() {
			setEditable(true);
		}

		/**
		 * From http://stackoverflow.com/a/24336768
		 *
		 * @param evt the event to process
		 */
		@Override
		public void processKeyEvent(final KeyEvent evt) {
			if ((evt.getID() != KeyEvent.KEY_PRESSED)
						|| (evt.getKeyCode() != KeyEvent.VK_TAB)) {
				super.processKeyEvent(evt);
				return;
			}

			if (isPopupVisible()) {
				assert evt.getSource() instanceof Component;
				final KeyEvent fakeEnterKeyEvent = new KeyEvent((Component) evt.getSource(),
																 evt.getID(),
																 evt.getWhen(),
																 0,
																 // No modifiers.
																 KeyEvent.VK_ENTER,
																 // Enter key.
																 KeyEvent
																		 .CHAR_UNDEFINED);
				super.processKeyEvent(fakeEnterKeyEvent);
			}
			if (evt.getModifiers() == 0) {
				transferFocus();
			} else if (evt.getModifiers() == InputEvent.SHIFT_MASK) {
				transferFocusBackward();
			}
		}

		/**
		 * The values we've had in the past.
		 */
		private final Collection<String> values = new HashSet<>();

		/**
		 * Clear the combo box, but if its value was one we haven't had previously, add
		 * it to the drop-down list.
		 */
		public void checkAndClear() {
			final String item = getSelectedItem().toString().trim();
			if (!values.contains(item)) {
				values.add(item);
				addItem(item);
			}
			setSelectedItem(null);
		}
	}
}
