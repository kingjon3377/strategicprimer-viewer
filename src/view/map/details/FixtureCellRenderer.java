package view.map.details;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.text.View;

import model.map.HasImage;
import model.map.TileFixture;
import util.ImageLoader;
import util.TypesafeLogger;

/**
 * A cell renderer for tile-details GUIs.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureCellRenderer implements ListCellRenderer<TileFixture> {
	/**
	 * Default list renderer, for cases we don't know how to handle.
	 */
	private static final DefaultListCellRenderer LIST_DEFAULT = new DefaultListCellRenderer();

	/**
	 * @param list the list being rendered
	 * @param value the object in the list that's being rendered
	 * @param index the index of the item that's being rendered
	 * @param isSelected whether the node is selected
	 * @param cellHasFocus whether the tree has the focus
	 * @return a component representing the cell
	 */
	// ESCA-JAVA0138:
	@Override
	public Component getListCellRendererComponent(
			final JList<? extends TileFixture> list, final TileFixture value,
			final int index, final boolean isSelected,
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
				component.getMaximumSize().height * 2));
		setComponentPreferredSize((JComponent) component, list.getWidth());
		return component;
	}

	/**
	 * @param obj a HasImage object
	 * @return an icon representing it
	 */
	private Icon getIcon(final HasImage obj) {
		// ESCA-JAVA0177:
		Icon retval;
		String image = obj.getImage();
		if (image.isEmpty()) {
			image = obj.getDefaultImage();
		}
		try {
			retval = ImageLoader.getLoader().loadIcon(image);
		} catch (final FileNotFoundException e) { // $codepro.audit.disable
													// logExceptions
			LOGGER.log(Level.SEVERE, "image file images/" + image
					+ " not found");
			retval = defaultFixtIcon;
		} catch (final IOException e) { // $codepro.audit.disable logExceptions
			LOGGER.log(Level.SEVERE, "I/O error reading image");
			retval = defaultFixtIcon;
		}
		return retval;
	}

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
	 * @return the default icon for fixtures.
	 */
	private static Icon createDefaultFixtureIcon() {
		/**
		 * The margin we allow around the chit itself in the default image.
		 */
		final double margin = 0.15; // NOPMD
		final int imageSize = 24; // NOPMD
		final BufferedImage temp = new BufferedImage(imageSize, imageSize,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		pen.fillRoundRect((int) Math.round(imageSize * margin) + 1,
				(int) Math.round(imageSize * margin) + 1,
				(int) Math.round(imageSize * (1.0 - margin * 2.0)),
				(int) Math.round(imageSize * (1.0 - margin * 2.0)),
				(int) Math.round(imageSize * (margin / 2.0)),
				(int) Math.round(imageSize * (margin / 2.0)));
		pen.setColor(saveColor);
		pen.fillRoundRect(
				((int) Math.round(imageSize / 2.0 - imageSize * margin)) + 1,
				((int) Math.round(imageSize / 2.0 - imageSize * margin)) + 1,
				(int) Math.round(imageSize * margin * 2.0),
				(int) Math.round(imageSize * margin * 2.0),
				(int) Math.round(imageSize * margin / 2.0),
				(int) Math.round(imageSize * margin / 2.0));
		return new ImageIcon(temp);

	}

	/**
	 * Set a component's height given a fixed width. Adapted from
	 * http://blog.nobel
	 * -joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
	 *
	 * @param component the component we're laying out
	 * @param width the width we're working within
	 */
	private static void setComponentPreferredSize(final JComponent component,
			final int width) {
		final View view = (View) component
				.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
		view.setSize(width, 0);
		final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
		final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
		component.setPreferredSize(new Dimension(wid, height));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FixtureCellRenderer";
	}
}
