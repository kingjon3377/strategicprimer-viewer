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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;
import model.viewer.FixtureFilterTableModel;
import model.viewer.IViewerModel;
import util.NullCleaner;
import view.map.details.DetailPanelNG;
import view.util.BorderedPanel;
import view.util.ISPWindow;
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
public final class ViewerFrame extends JFrame implements ISPWindow {
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
	 * Constructor.
	 *
	 * @param map       The map model.
	 * @param ioHandler the I/O handler, so we can handle 'open' and 'save' menu items.
	 */
	public ViewerFrame(final IViewerModel map, final IOHandler ioHandler) {
		super("Map Viewer");
		final Optional<Path> filename = map.getMapFile();
		if (filename.isPresent()) {
			setTitle(filename.get() + " | Map Viewer");
			getRootPane().putClientProperty("Window.documentFile",
					filename.get().toFile());
		}
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
		BorderedPanel tablePanel = BorderedPanel.vertical(new JLabel("Display ..."),
				new JScrollPane(table),
				BorderedPanel.horizontal(allButton, null, noneButton));
		setContentPane(SplitWithWeights.verticalSplit(MAP_PROPORTION, MAP_PROPORTION,
				SplitWithWeights.horizontalSplit(0.8, 0.8,
						ScrollListener.mapScrollPanel(map, mapPanel), tablePanel),
				detailPanel));
		initializeDimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		pack();
		mapPanel.requestFocusInWindow();
		final WindowAdapter windowSizeListener = new MapWindowSizeListener(mapPanel);
		addWindowListener(windowSizeListener);
		addWindowStateListener(windowSizeListener);

		setJMenuBar(new ViewerMenu(NullCleaner.assertNotNull(ioHandler), this,
				map));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	/**
	 * @return the map model
	 */
	public IViewerModel getModel() {
		return model;
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
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
		if (mapFile.isPresent()) {
			return "ViewerFrame showing map in " + mapFile.get();
		} else {
			return "ViewerFrame showing unsaved map";
		}
	}

	/**
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Map Viewer";
	}
}
