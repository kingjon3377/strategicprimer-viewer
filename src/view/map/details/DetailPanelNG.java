package view.map.details;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import model.listeners.SelectionChangeListener;
import model.listeners.VersionChangeListener;
import model.map.HasPortrait;
import model.map.Point;
import model.map.TileFixture;
import model.misc.IDriverModel;
import model.viewer.FixtureListModel;
import org.eclipse.jdt.annotation.Nullable;
import util.ImageLoader;
import util.TypesafeLogger;
import view.map.key.KeyPanel;
import view.util.BorderedPanel;
import view.util.SplitWithWeights;

/**
 * A panel to show the details of a tile, using a tree rather than sub-panels with chits
 * for its fixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class DetailPanelNG extends JSplitPane
		implements VersionChangeListener, SelectionChangeListener {
	/**
	 * The "weight" to give the divider. We want the 'key' to get very little of any
	 * extra
	 * space, but to get some.
	 */
	private static final double DIVIDER_LOCATION = 0.9;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(DetailPanelNG.class);
	/**
	 * The 'key' panel, showing what each tile color represents.
	 */
	private final KeyPanel keyPanel;
	/**
	 * The list of fixtures on the current tile.
	 */
	private final FixtureList fixList;
	/**
	 * The 'header' label above the list.
	 */
	private final JLabel header = new JLabel("<html><body><p>Contents of the tile" +
													 " at (-1, -1)" +
													 ":</p></body></html>");

	/**
	 * Constructor.
	 *
	 * @param version the (initial) map version
	 * @param model   the driver model; needed for the fixture list
	 */
	public DetailPanelNG(final int version, final IDriverModel model) {
		super(HORIZONTAL_SPLIT, true);

		fixList = new FixtureList(this, new FixtureListModel(model.getMap()),
										 model.getMap().players());
		final PortraitPanel portrait = new PortraitPanel(fixList);
		fixList.addListSelectionListener(portrait);
		final JPanel listPanel =
				BorderedPanel.verticalPanel(header, new JScrollPane(fixList), null);

		keyPanel = new KeyPanel(version);
		setLeftComponent(SplitWithWeights.horizontalSplit(0.5, 0.5, listPanel,
				portrait));
		setRightComponent(keyPanel);
		setResizeWeight(DIVIDER_LOCATION);
		setDividerLocation(DIVIDER_LOCATION);
	}

	/**
	 * @param old        passed to key panel
	 * @param newVersion passed to key panel
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		keyPanel.changeVersion(old, newVersion);
	}

	/**
	 * @param old      passed to fixture list
	 * @param newPoint passed to fixture list and shown on the header
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		fixList.selectedPointChanged(old, newPoint);
		header.setText("<html><body><p>Contents of the tile at "
							   + newPoint + ":</p></body></html>");
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
	 * Prevent serialization.
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
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "DetailPanelNG: Header currently reads " + header.getText();
	}

	/**
	 * A component to show a fixture's portrait.
	 */
	private static class PortraitPanel extends JComponent
			implements ListSelectionListener {
		/**
		 * The list we're watching.
		 */
		private final JList<TileFixture> list;
		/**
		 * A reference to the image loading utility class.
		 */
		private final ImageLoader loader = ImageLoader.getLoader();
		/**
		 * The current portrait.
		 */
		@Nullable
		private Image portrait = null;
		/**
		 * Constructor.
		 *
		 * @param fixtureList The list to watch.
		 */
		protected PortraitPanel(final JList<TileFixture> fixtureList) {
			list = fixtureList;
		}

		/**
		 * Draw the portrait, if any.
		 *
		 * @param pen the graphics context
		 */
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
		@Override
		protected void paintComponent(@Nullable final Graphics pen) {
			super.paintComponent(pen);
			final Image local = portrait;
			if ((local != null) && (pen != null)) {
				pen.drawImage(local, 0, 0, getWidth(), getHeight(), this);
			}
		}

		/**
		 * Handle a selection change.
		 *
		 * @param event ignored (we go straight to the list)
		 */
		@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
		@Override
		public void valueChanged(@Nullable final ListSelectionEvent event) {
			final List<TileFixture> selections = list.getSelectedValuesList();
			portrait = null;
			if (!selections.isEmpty() && (selections.size() == 1)) {
				final TileFixture selectedValue = selections.get(0);
				if (selectedValue instanceof HasPortrait) {
					final String portraitName =
							((HasPortrait) selectedValue).getPortrait();
					if (!portraitName.isEmpty()) {
						try {
							portrait = loader.loadImage(portraitName);
							repaint();
						} catch (final IOException except) {
							//noinspection HardcodedFileSeparator
							LOGGER.log(Level.WARNING, "I/O error loading portrait",
									except);
						}
					}
				}
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
		 * Prevent serialization.
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
		 * @return a String representation of the object
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public String toString() {
			return "PortraitPanel";
		}
	}
}
