package view.map.details;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import model.map.HasImage;
import model.map.TileFixture;
import util.ImageLoader;
import util.NullCleaner;
import util.TypesafeLogger;

/**
 * A cell renderer for tile-details GUIs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
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
public final class FixtureCellRenderer implements ListCellRenderer<TileFixture> {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			                                     .getLogger(FixtureCellRenderer.class);
	/**
	 * the default fixture icon.
	 */
	private final Icon defaultFixtIcon = createDefaultFixtureIcon();

	/**
	 * Default list renderer, for cases we don't know how to handle.
	 */
	private static final ListCellRenderer<Object> LIST_DEFAULT =
			new DefaultListCellRenderer();

	/**
	 * @param list         the list being rendered
	 * @param value        the object in the list that's being rendered
	 * @param index        the index of the item that's being rendered
	 * @param isSelected   whether the node is selected
	 * @param cellHasFocus whether the tree has the focus
	 * @return a component representing the cell
	 */
	@Override
	public Component getListCellRendererComponent(final JList<? extends TileFixture>
			                                                  list,
	                                              final TileFixture value,
	                                              final int index,
	                                              final boolean isSelected,
	                                              final boolean cellHasFocus) {
		final Component component = LIST_DEFAULT.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);
		((JLabel) component).setText("<html><p>" + value.toString()
				                             + "</p></html>");
		if (value instanceof HasImage) {
			((JLabel) component).setIcon(getIcon((HasImage) value));
		} else {
			((JLabel) component).setIcon(defaultFixtIcon);
		}
		component.setMaximumSize(new Dimension(
				                                      component.getMaximumSize().width,
				                                      component.getMaximumSize().height *
						                                      2));
		setComponentPreferredSize((JComponent) component, list.getWidth());
		return component;
	}

	/**
	 * A cache of icon filenames that aren't available.
	 */
	private static final Set<String> MISSING =
			NullCleaner.assertNotNull(Collections.synchronizedSet(new HashSet<>()));

	/**
	 * @param obj a HasImage object
	 * @return an icon representing it
	 */
	private Icon getIcon(final HasImage obj) {
		String image = obj.getImage();
		if (image.isEmpty() || MISSING.contains(image)) {
			image = obj.getDefaultImage();
		}
		if (MISSING.contains(image)) {
			return defaultFixtIcon;
		}
		Icon retval;
		try {
			retval = ImageLoader.getLoader().loadIcon(image);
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "image file images/" + image
					                         + " not found");
			LOGGER.log(Level.FINEST, "With stack trace", e);
			MISSING.add(image);
			retval = defaultFixtIcon;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image", e);
			retval = defaultFixtIcon;
		}
		return retval;
	}

	/**
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		/*
		 * The margin we allow around the chit itself in the default image.
		 */
		final int imageSize = 24; // NOPMD
		final BufferedImage temp = new BufferedImage(imageSize, imageSize,
				                                            BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		final double margin = 0.15; // NOPMD
		pen.fillRoundRect((int) Math.round(imageSize * margin) + 1,
				(int) Math.round(imageSize * margin) + 1,
				(int) Math.round(imageSize * (1.0 - (margin * 2.0))),
				(int) Math.round(imageSize * (1.0 - (margin * 2.0))),
				(int) Math.round(imageSize * (margin / 2.0)),
				(int) Math.round(imageSize * (margin / 2.0)));
		pen.setColor(saveColor);
		pen.fillRoundRect(
				(int) Math.round((imageSize / 2.0) - (imageSize * margin)) + 1,
				(int) Math.round((imageSize / 2.0) - (imageSize * margin)) + 1,
				(int) Math.round(imageSize * margin * 2.0),
				(int) Math.round(imageSize * margin * 2.0),
				(int) Math.round((imageSize * margin) / 2.0),
				(int) Math.round((imageSize * margin) / 2.0));
		return new ImageIcon(temp);

	}

	/**
	 * Set a component's height given a fixed width. Adapted from http://blog.nobel
	 * -joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
	 *
	 * @param component the component we're laying out
	 * @param width     the width we're working within
	 */
	private static void setComponentPreferredSize(final JComponent component,
	                                              final int width) {
		final View view = (View) component
				                         .getClientProperty(BasicHTML.propertyKey);
		view.setSize(width, 0);
		final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
		final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
		component.setPreferredSize(new Dimension(wid, height));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "FixtureCellRenderer";
	}
}
