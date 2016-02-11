package view.map.main;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import controller.map.misc.IOHandler;
import model.viewer.IViewerModel;
import view.map.details.DetailPanelNG;
import view.util.SplitWithWeights;

/**
 * The main driver class for the map viewer app.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
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
public final class ViewerFrame extends JFrame {
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
		if (map.getMapFile().exists()) {
			setTitle(map.getMapFile().getName() + " | Map Viewer");
			getRootPane().putClientProperty("Window.documentFile",
					map.getMapFile());
		}
		model = map;
		final FixtureFilterMenu ffmenu = new FixtureFilterMenu();
		final MapComponent mapPanel = new MapComponent(map, ffmenu);
		map.addGraphicalParamsListener(mapPanel);
		map.addMapChangeListener(mapPanel);
		map.addSelectionChangeListener(mapPanel);
		final DetailPanelNG detailPanel = new DetailPanelNG(
																   map.getMapDimensions
																			   ()
																		   .version,
																   map);
		map.addVersionChangeListener(detailPanel);
		map.addSelectionChangeListener(detailPanel);
		setContentPane(SplitWithWeights.vertical(MAP_PROPORTION, MAP_PROPORTION,
				new MapScrollPanel(map, mapPanel), detailPanel));
		initializeDimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		pack();
		mapPanel.requestFocusInWindow();
		final WindowAdapter mwsl = new MapWindowSizeListener(mapPanel);
		addWindowListener(mwsl);
		addWindowStateListener(mwsl);

		setJMenuBar(new ViewerMenu(ioHandler, this, map));
		getJMenuBar().add(ffmenu);
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
}
