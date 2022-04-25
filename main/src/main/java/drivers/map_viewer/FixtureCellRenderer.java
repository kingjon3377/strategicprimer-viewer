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

import lovelace.util.LovelaceLogger;

/**
 * A cell renderer for tile-details GUIs.
 */
/* package */ class FixtureCellRenderer implements ListCellRenderer<TileFixture> {
	private static final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

	private static final Set<String> MISSING_FILENAMES = new HashSet<>();

	// Image size is 24, margin is 15%.
	private static Icon createDefaultFixtureIcon() {
		final int imageSize = 24;
		final BufferedImage temp = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		final int pixelMargin = 4; // imageSize * margin
		final int afterMargin = 17; // imageSize * (1 - margin * 2)
		final int cornerRounding = 2; // (imageSize * margin) / 2
		pen.fillRoundRect(pixelMargin + 1, pixelMargin + 1,
				afterMargin, afterMargin, cornerRounding, cornerRounding);
		pen.setColor(saveColor);
		final int newMargin = 8; // (imageSize / 2) - (imageSize * margin)
		final int newAfterMargin = 7; // imageSize * margin * 2
		final int newCorner = 2; // imageSize * margin / 2
		pen.fillRoundRect(newMargin + 1, newMargin + 1,
				newAfterMargin, newAfterMargin, newCorner, newCorner);
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

	private static Icon getIcon(final HasImage obj) {
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
			LovelaceLogger.error("image file images/%s not found", actualImage);
			LovelaceLogger.debug(except, "With stack trace");
			MISSING_FILENAMES.add(actualImage);
			return DEFAULT_FIXTURE_ICON;
		} catch (final IOException except) {
			LovelaceLogger.error(except, "I/O error reading image");
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
