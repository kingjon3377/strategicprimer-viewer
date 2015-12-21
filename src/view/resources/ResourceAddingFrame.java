package view.resources;

import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import model.map.Player;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.resources.ResourceManagementDriver;
import view.util.ErrorShower;
import view.worker.PlayerChooserHandler;
import view.worker.WorkerMenu;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * A window to let the user enter resources etc. Note that this is not a dialog to enter one resource and close.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan Lovelace.
 *
 * Copyright (C) 2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of version 3 of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ResourceAddingFrame extends JFrame {
	private final ResourceManagementDriver model;
	private Player current;
	private final JLabel resourceLabel;
	private final JLabel implementLabel;
	private final UpdatedComboBox resKindBox = new UpdatedComboBox();
	private final JTextField resPrefixField = new JTextField();
	private final UpdatedComboBox resourceBox = new UpdatedComboBox();
	private final NumberFormat nf = NumberFormat.getIntegerInstance();
	private final JFormattedTextField resQtyField = new JFormattedTextField(nf);
	private final UpdatedComboBox resUnitsBox = new UpdatedComboBox();
	private final UpdatedComboBox implKindBox = new UpdatedComboBox();

	public ResourceAddingFrame(ResourceManagementDriver dmodel, IOHandler ioh) {
		super("Resource Entry");
		model = dmodel;
		IDFactory idf = IDFactoryFiller.createFactory(model);
		current = StreamSupport.stream(dmodel.getPlayers().spliterator(), false).filter(player -> player.isCurrent())
				          .findAny().orElse(new Player(-1, ""));
		resourceLabel = new JLabel(String.format("Add resource for %s:", current.getName()));
		implementLabel = new JLabel(String.format("Add equipment for %s:", current.getName()));
		final PlayerChooserHandler pch = new PlayerChooserHandler(this, model);
		pch.addPlayerChangeListener((old, newPlayer) -> {
			if (newPlayer == null) {
				current = new Player(-1, "");
			} else {
				current = newPlayer;
			}
			resourceLabel.setText(String.format("Add resource for %s:", current.getName()));
			implementLabel.setText(String.format("Add equipment for %s:", current.getName()));
		});
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		add(resourceLabel);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		addPair(panel, new JLabel("General Category"), resKindBox);
		addPair(panel, new JLabel("Prefix (e.g. age)"), resPrefixField);
		addPair(panel, new JLabel("Specific Resource"), resourceBox);
		addPair(panel, new JLabel("Quantity"), resQtyField);
		addPair(panel, new JLabel("Units"), resUnitsBox);
		JButton resourceButton = new JButton("Add Resource");
		addPair(panel, new JLabel(""), resourceButton);
		Component outer = this;
		resourceButton.addActionListener(evt -> {
			try {
				String kind = resKindBox.getSelectedItem().toString().trim();
				String resource = resourceBox.getSelectedItem().toString().trim();
				String units = resUnitsBox.getSelectedItem().toString().trim();
				String prefixed = String.format("%s %s", resPrefixField.getText().trim(), resource).trim();
				model.addResource(new ResourcePile(idf.createID(), kind, prefixed,
						                                  nf.parse(resQtyField.getText().trim()).intValue(), units),
						current);
				resKindBox.checkAndClear();
				resPrefixField.setText("");
				resourceBox.checkAndClear();
				resQtyField.setText("");
				resUnitsBox.checkAndClear();
				resKindBox.requestFocusInWindow();
			} catch (ParseException except) {
				ErrorShower.showErrorDialog(outer, "Quantity must be numeric");
			}
		});
		add(panel);
		add(Box.createVerticalGlue());
		add(implementLabel);
		JPanel secondPanel = new JPanel();
		secondPanel.setLayout(new BoxLayout(secondPanel, BoxLayout.LINE_AXIS));
		secondPanel.add(implKindBox);
		JButton implButton = new JButton("Add Equipment");
		implButton.addActionListener(evt -> {
			String kind = implKindBox.getSelectedItem().toString().trim();
			model.addResource(new Implement(idf.createID(), kind), current);
			implKindBox.checkAndClear();
			implKindBox.requestFocusInWindow();
		});
		secondPanel.add(implButton);
		add(secondPanel);
		add(Box.createVerticalGlue());
		setJMenuBar(new WorkerMenu(ioh, this, pch, model, ioh));
		pack();
	}
	private static void addPair(Container container, Component one, Component two) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(Box.createVerticalGlue());
		panel.add(one);
		panel.add(Box.createVerticalGlue());
		panel.add(two);
		panel.add(Box.createVerticalGlue());
		container.add(panel);
	}

	/**
	 * TODO: The central functionality of this should be a top-level class in view.util
	 */
	private static class UpdatedComboBox extends JComboBox<String> {
		protected UpdatedComboBox() {
			setEditable(true);
		}
		/**
		 * From http://stackoverflow.com/a/24336768
		 * @param evt the event to process
		 */
		@Override
		public void processKeyEvent(KeyEvent evt) {
			if ( evt.getID() != KeyEvent.KEY_PRESSED
					     || evt.getKeyCode() != KeyEvent.VK_TAB) {
				super.processKeyEvent(evt);
				return;
			}

			if (isPopupVisible()) {
				assert evt.getSource() instanceof Component;
				KeyEvent fakeEnterKeyEvent = new KeyEvent((Component) evt.getSource(),
						                                         evt.getID(),
						                                         evt.getWhen(),
						                                         0,                   // No modifiers.
						                                         KeyEvent.VK_ENTER,   // Enter key.
						                                         KeyEvent.CHAR_UNDEFINED);
				super.processKeyEvent(fakeEnterKeyEvent);
			}
			if ( evt.getModifiers() == 0) {
				transferFocus();
			} else if ( evt.getModifiers() == KeyEvent.SHIFT_MASK) {
				transferFocusBackward();
			}
		}
		private Set<String> values = new HashSet<>();
		public void checkAndClear() {
			String item = getSelectedItem().toString().trim();
			if (!values.contains(item)) {
				values.add(item);
				addItem(item);
			}
			setSelectedItem(null);
		}
	}
}
