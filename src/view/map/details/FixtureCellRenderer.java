package view.map.details;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import model.map.HasImage;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ImageLoader;
import util.TypesafeLogger;

/**
 * A cell renderer for tile-details GUIs.
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
public final class FixtureCellRenderer implements ListCellRenderer<@NonNull TileFixture> {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(FixtureCellRenderer.class);
	/**
	 * Default list renderer, for cases we don't know how to handle.
	 */
	private static final ListCellRenderer<Object> LIST_DEFAULT =
			new DefaultListCellRenderer();
	/**
	 * A cache of icon filenames that aren't available.
	 */
	private static final Set<String> MISSING =
			Collections.synchronizedSet(new HashSet<>());
	/**
	 * the default fixture icon.
	 */
	private final Icon defaultFixtureIcon = createDefaultFixtureIcon();

	/**
	 * The default icon for a fixture.
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		/*
		 * The margin we allow around the chit itself in the default image.
		 */
		final int imageSize = 24;
		final BufferedImage temp =
				new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		// The margin as a fraction of the whole.
		final double margin = 0.15;
		// The margin, in pixels
		final double pixelMargin = Math.round(imageSize * margin);
		// The part of the image size not covered by margins.
		final double afterMargin = Math.round(imageSize * (1.0 - (margin * 2.0)));
		// The rounding on the corners: half as much as the normal margin.
		final double cornerRounding = Math.round((imageSize * margin) / 2.0);
		pen.fillRoundRect((int) pixelMargin + 1,
				(int) pixelMargin + 1,
				(int) afterMargin,
				(int) afterMargin,
				(int) cornerRounding,
				(int) cornerRounding);
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
		final View view = (View) component.getClientProperty(BasicHTML.propertyKey);
		view.setSize(width, 0);
		final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
		final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
		component.setPreferredSize(new Dimension(wid, height));
	}

	/**
	 * Render a list item.
	 * @param list         the list being rendered
	 * @param value        the object in the list that's being rendered
	 * @param index        the index of the item that's being rendered
	 * @param isSelected   whether the node is selected
	 * @param cellHasFocus whether the tree has the focus
	 * @return a component representing the cell
	 */
	@Override
	public Component getListCellRendererComponent(@Nullable
												  final JList<? extends TileFixture>
															  list,
												  final TileFixture value,
												  final int index,
												  final boolean isSelected,
												  final boolean cellHasFocus) {
		assert list != null;
		final Component component = LIST_DEFAULT.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);
		((JLabel) component).setText("<html><p>" + value + "</p></html>");
		if (value instanceof HasImage) {
			((JLabel) component).setIcon(getIcon((HasImage) value));
		} else {
			((JLabel) component).setIcon(defaultFixtureIcon);
		}
		component.setMaximumSize(new Dimension(component.getMaximumSize().width,
													  component.getMaximumSize().height *
															  2));
		setComponentPreferredSize((JComponent) component, list.getWidth());
		return component;
	}

	/**
	 * An icon representing an object.
	 * @param obj a HasImage object
	 * @return an icon representing it
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	private Icon getIcon(final HasImage obj) {
		String image = obj.getImage();
		if (image.isEmpty() || MISSING.contains(image)) {
			image = obj.getDefaultImage();
		}
		if (MISSING.contains(image)) {
			return defaultFixtureIcon;
		}
		Icon retval;
		try {
			retval = ImageLoader.getLoader().loadIcon(image);
		} catch (final FileNotFoundException | NoSuchFileException e) {
			LOGGER.log(Level.SEVERE,
					"image file images" + File.separatorChar + image + " not found");
			LOGGER.log(Level.FINEST, "With stack trace", e);
			MISSING.add(image);
			retval = defaultFixtureIcon;
		} catch (final IOException e) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error reading image", e);
			retval = defaultFixtureIcon;
		}
		return retval;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "FixtureCellRenderer";
	}
}
