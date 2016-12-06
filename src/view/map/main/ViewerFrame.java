package view.map.main;

import controller.map.misc.IOHandler;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import model.viewer.FixtureFilterTableModel;
import model.viewer.IViewerModel;
import util.NullCleaner;
import util.OnMac;
import view.map.details.DetailPanelNG;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.SPFrame;
import view.util.SplitWithWeights;

/**
 * The main driver class for the map viewer app.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ViewerFrame extends SPFrame {
	/**
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 800;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 600;
	/**
	 * The default proportion between map and detail panels.
	 */
	private static final double MAP_PROPORTION = 0.9;
	/**
	 * The driver model.
	 */
	private final IViewerModel model;

	/**
	 * Constructor.
	 *
	 * @param map       The map model.
	 * @param ioHandler the I/O handler, so we can handle 'open' and 'save' menu items.
	 */
	public ViewerFrame(final IViewerModel map, final IOHandler ioHandler) {
		super("Map Viewer", map.getMapFile());
		model = map;
		final FixtureFilterTableModel tableModel = new FixtureFilterTableModel();
		final MapComponent mapPanel = new MapComponent(map, tableModel, tableModel);
		tableModel.addTableModelListener(e -> mapPanel.repaint());
		map.addGraphicalParamsListener(mapPanel);
		map.addMapChangeListener(mapPanel);
		map.addSelectionChangeListener(mapPanel);
		final DetailPanelNG detailPanel =
				new DetailPanelNG(map.getMapDimensions().version, map);
		map.addVersionChangeListener(detailPanel);
		map.addSelectionChangeListener(detailPanel);
		setContentPane(SplitWithWeights.verticalSplit(MAP_PROPORTION, MAP_PROPORTION,
				SplitWithWeights.horizontalSplit(0.95, 0.95,
						ScrollListener.mapScrollPanel(map, mapPanel),
						createFilterPanel(tableModel)),
				detailPanel));
		initializeDimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		pack();
		mapPanel.requestFocusInWindow();
		final WindowAdapter windowSizeListener = new MapWindowSizeListener(mapPanel);
		addWindowListener(windowSizeListener);
		addWindowStateListener(windowSizeListener);

		setJMenuBar(new ViewerMenu(NullCleaner.assertNotNull(ioHandler), this,
										  map));
	}

	/**
	 * Create the panel that allows the user to filter the display and dynamically
	 * change the Z-order.
	 * @param tableModel the data model underlying the table that is the central part of
	 *                      this panel
	 * @return the panel
	 */
	private static JComponent createFilterPanel(final FixtureFilterTableModel
														   tableModel) {
		final JTable table = new JTable(tableModel);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new FixtureFilterTransferHandler());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final TableColumn firstColumn = table.getColumnModel().getColumn(0);
		firstColumn.setMinWidth(30);
		firstColumn.setMaxWidth(50);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		final JButton allButton = new JButton("Display All");
		allButton.addActionListener(
				e -> {
					tableModel.forEach(matcher -> matcher.setDisplayed(true));
					tableModel.fireTableRowsUpdated(0, tableModel.getRowCount());
				});
		final JButton noneButton = new JButton("Display None");
		noneButton.addActionListener(
				e -> {
					tableModel.forEach(matcher -> matcher.setDisplayed(false));
					tableModel.fireTableRowsUpdated(0, tableModel.getRowCount());
				});
		final JPanel buttonPanel;
		OnMac.makeButtonsSegmented(allButton, noneButton);
		if (OnMac.SYSTEM_IS_MAC) {
			buttonPanel = BoxPanel.centeredBox(true, 2, allButton, noneButton);
		} else {
			buttonPanel = BorderedPanel.horizontalPanel(allButton, null, noneButton);
		}
		return BorderedPanel.verticalPanel(new JLabel("Display ..."),
				new JScrollPane(table),
				buttonPanel);
	}

	/**
	 * Initialize size to the specified dimensions. Not that this actually works ...
	 *
	 * @param width  the specified width
	 * @param height the specified height
	 */
	private void initializeDimensions(final int width, final int height) {
		setPreferredSize(new Dimension(width, height));
		setSize(width, height);
		setMinimumSize(new Dimension(width, height));
	}

	/**
	 * @return the map model
	 */
	public IViewerModel getModel() {
		return model;
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
		final Optional<Path> mapFile = model.getMapFile();
		return mapFile.map(path -> "ViewerFrame showing map in " + path)
					   .orElse("ViewerFrame showing unsaved map");
	}

	/**
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Map Viewer";
	}
}
