package view.map.details;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import model.map.TileFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import view.map.main.Selectable;

/**
 * A "piece" to represent a unit or fortress (or something else ...) in the
 * details-panel.
 *
 * @author Jonathan Lovelace
 *
 */
public abstract class Chit extends Selectable { // NOPMD
	/**
	 *
	 * @return the description of the unit or fortress this chit represents, to
	 *         report to the user
	 */
	public abstract String describe();

	/**
	 * The maximum size of a chit.
	 */
	private static final int SIZE = 24;
	/**
	 * The maximum size of a chit, in the form other objects will want.
	 */
	private static final Dimension MAX_SIZE = new Dimension(SIZE, SIZE);

	/**
	 *
	 * @return the maximum size of a chit
	 */
	@Override
	public Dimension getMaximumSize() {
		return MAX_SIZE;
	}

	/**
	 *
	 * @return the minimum size of a chit
	 */
	@Override
	public Dimension getMinimumSize() {
		return MAX_SIZE;
	}

	/**
	 *
	 * @return the preferred size of a chit
	 */
	@Override
	public Dimension getPreferredSize() {
		return MAX_SIZE;
	}

	/**
	 * Constructor.
	 *
	 * @param listener a listener to detect clicks on the chit.
	 * @param transferable a Transferable encapsulating the data the Chit
	 *        represents.
	 */
	protected Chit(final MouseListener listener, final Transferable transferable) {
		super();
		setOpaque(false);
		addMouseListener(listener);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this, DnDConstants.ACTION_COPY,
				new ChitDragGestureListener(transferable));
	}

	/**
	 * Create a backup image, special-casing forests and mountains (and perhaps
	 * others).
	 *
	 * @param fix the fixture it'll represent
	 * @return a default image
	 */
	public static Image createDefaultImage(final TileFixture fix) {
		/**
		 * The margin we allow around the chit itself in the default image.
		 */
		final double margin = 0.15; // NOPMD
		final int imageSize = 24; // NOPMD
		final BufferedImage temp = new BufferedImage(imageSize, imageSize,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		if (fix instanceof Mountain) {
			pen.setColor(Color.orange);
			// ESCA-JAVA0076:
			pen.fillPolygon(new int[] { 0, 12, 24 }, new int[] { 24, 0, 24 }, 3);
		} else if (fix instanceof Forest) {
			pen.setColor(Color.green);
			// ESCA-JAVA0076:
			pen.fillPolygon(new int[] { 8, 12, 16 }, new int[] { 24, 0, 24 }, 3);
		} else {
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
		}
		return temp;
	}

}
