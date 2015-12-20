package view.resources;

import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import model.map.Player;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.resources.ResourceManagementDriver;
import view.util.ErrorShower;
import view.util.SPMenu;
import view.worker.PlayerChooserHandler;
import view.worker.WorkerMenu;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private final JComboBox<String> resKindBox = new JComboBox<>();
	private final Set<String> resKindSet = new HashSet<>();
	private final JTextField resPrefixField = new JTextField();
	private final JComboBox<String> resourceBox = new JComboBox<>();
	private final Set<String> resourceSet = new HashSet<>();
	private final NumberFormat nf = NumberFormat.getIntegerInstance();
	private final JFormattedTextField resQtyField = new JFormattedTextField(nf);
	private final JComboBox<String> resUnitsBox = new JComboBox<>();
	private final Set<String> resUnitsSet = new HashSet<>();
	private final JComboBox<String> implKindBox = new JComboBox<>();
	private final Set<String> implKindSet = new HashSet<>();

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
		resKindBox.setEditable(true);
		resourceBox.setEditable(true);
		resUnitsBox.setEditable(true);
		implKindBox.setEditable(true);
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
				if (!resKindSet.contains(kind)) {
					resKindSet.add(kind);
					resKindBox.addItem(kind);
				}
				resKindBox.setSelectedItem(null);
				resPrefixField.setText("");
				if (!resourceSet.contains(resource)) {
					resourceSet.add(resource);
					resourceBox.addItem(resource);
				}
				resourceBox.setSelectedItem(null);
				resQtyField.setText("");
				if (!resUnitsSet.contains(units)) {
					resUnitsSet.add(units);
					resUnitsBox.addItem(units);
				}
				resUnitsBox.setSelectedItem(null);
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
			if (!implKindSet.contains(kind)) {
				implKindSet.add(kind);
				implKindBox.addItem(kind);
			}
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
}
