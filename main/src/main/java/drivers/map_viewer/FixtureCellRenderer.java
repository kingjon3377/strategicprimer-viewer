package drivers.map_viewer;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import common.map.TileFixture;
import common.map.HasImage;

import java.awt.image.BufferedImage;

import javax.swing.plaf.basic.BasicHTML;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ImageIcon;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JComponent;
import javax.swing.Icon;

import javax.swing.text.View;

import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;

import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A cell renderer for tile-details GUIs.
 */
/* package */ class FixtureCellRenderer implements ListCellRenderer<TileFixture> {
	private static final Logger LOGGER = Logger.getLogger(FixtureCellRenderer.class.getName());
	private static final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

	private static final Set<String> MISSING_FILENAMES = new HashSet<>();

	private static Icon createDefaultFixtureIcon() {
		final int imageSize = 24;
		final BufferedImage temp = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		final double margin = 0.15;
		// TODO: Each of these calculations was wrapped in halfEven() in Ceylon; investigate whether Math.round() changes behavior
		final double pixelMargin = Math.round(imageSize * margin);
		final double afterMargin = Math.round(imageSize * (1.0 - (margin * 2.0)));
		final double cornerRounding = Math.round((imageSize * margin) / 2.0);
		pen.fillRoundRect(((int) pixelMargin) + 1, ((int) pixelMargin) + 1,
			(int) afterMargin, (int) afterMargin, (int) cornerRounding, (int) cornerRounding);
		pen.setColor(saveColor);
		final double newMargin = Math.round((imageSize / 2.0) - (imageSize * margin));
		final double newAfterMargin = Math.round(imageSize * margin * 2.0);
		final double newCorner = Math.round((imageSize * margin) / 2.0);
		pen.fillRoundRect(((int) newMargin) + 1, ((int) newMargin) + 1,
			(int) newAfterMargin, (int) newAfterMargin, (int) newCorner, (int) newCorner);
		pen.dispose();
		return new ImageIcon(temp);
	}

	/**
	 * Set a component's height given a fixed with.
	 *
	 * @author http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
	 *
	 * FIXME: Move to third-party module
	 */
	private static void setComponentPreferredSize(final JComponent component, final int width) {
		final View view = (View) component.getClientProperty(BasicHTML.propertyKey);
		view.setSize((float) width, (float) 0.0);
		final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
		final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
		component.setPreferredSize(new Dimension(wid, height)); // TODO: Use Dimension constructor taking doubles instead?
	}

	private static final Icon DEFAULT_FIXTURE_ICON = createDefaultFixtureIcon();

	private Icon getIcon(final HasImage obj) {
		final String image = obj.getImage();
		final String actualImage;
		if (image.isEmpty() || MISSING_FILENAMES.contains(image)) {
			actualImage = obj.getDefaultImage();
		} else {
			actualImage = image;
		}
		if (MISSING_FILENAMES.contains(actualImage)) {
			return DEFAULT_FIXTURE_ICON;
		}
		try {
			return ImageLoader.loadIcon(actualImage);
		} catch (final FileNotFoundException|NoSuchFileException except) {
			LOGGER.severe(String.format("image file images/%s not found", actualImage));
			LOGGER.log(Level.FINE, "With stack trace", except);
			MISSING_FILENAMES.add(actualImage);
			return DEFAULT_FIXTURE_ICON;
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading image", except);
			return DEFAULT_FIXTURE_ICON;
		}
	}

	@Override
	public Component getListCellRendererComponent(final JList<? extends TileFixture> list,
	                                              final TileFixture val, final int index, final boolean isSelected, final boolean cellHasFocus) {
		final JLabel component = (JLabel) DEFAULT_RENDERER.getListCellRendererComponent(list,
			val, index, isSelected, cellHasFocus);
		component.setText(String.format("<html><p>%s</p></html>", val.getShortDescription()));
		if (val instanceof HasImage) {
			component.setIcon(getIcon((HasImage) val));
		} else {
			component.setIcon(DEFAULT_FIXTURE_ICON);
		}
		component.setMaximumSize(new Dimension((int) component.getMaximumSize().getWidth(),
			(int) (component.getMaximumSize().getHeight() * 2)));
		setComponentPreferredSize(component, list.getWidth());
		return component;
	}
}
