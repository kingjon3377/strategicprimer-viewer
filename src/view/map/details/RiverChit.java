package view.map.details;

import static view.util.DrawingNumericConstants.EIGHT;
import static view.util.DrawingNumericConstants.FOUR;
import static view.util.DrawingNumericConstants.SEVEN_SIXTEENTHS;
import static view.util.DrawingNumericConstants.TWO;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import model.map.River;
import model.map.events.RiverFixture;
import model.viewer.FixtureTransferable;

/**
 * A chit to represent the rivers on a tile.
 * @author Jonathan Lovelace
 */
public class RiverChit extends Chit {
	/**
	 * A description of the rivers.
	 */
	private final String desc;
	/**
	 * @return a description of the rivers.
	 */
	@Override
	public String describe() {
		return desc;
	}
	/**
	 * Constructor.
	 * @param rivers the rivers this chit represents
	 * @param listener the object listening for clicks on this chit.
	 */
	public RiverChit(final RiverFixture rivers, final MouseListener listener) {
		super(listener, new FixtureTransferable(rivers));
		final StringBuilder sbuild = new StringBuilder("<html><p>Rivers: ");
		boolean first = true;
		for (River river : rivers) {
			if (first) {
				first = false;
			} else {
				sbuild.append(", ");
			}
			sbuild.append(river.toString());
		}
		sbuild.append("</p></html>");
		desc = sbuild.toString();
		fix = rivers;
	}
	/**
	 * The rivers this chit represents. We save them for ease of painting.
	 */
	private final RiverFixture fix;
	/**
	 * Paint the chit.
	 * @param pen the graphics context.
	 */
	@Override
	public void paint(final Graphics pen) {
		final Color save = pen.getColor();
		pen.setColor(Color.blue);
		for (River river : fix) {
			drawRiver(pen, river, 0, 0, getWidth(), getHeight());
		}
		pen.setColor(save);
		super.paint(pen);
	}
	/**
	 * Draw a river.
	 * 
	 * @param pen
	 *            the graphics context---again, origin at tile's upper-left
	 *            corner
	 * @param river
	 *            the river to draw
	 * @param xCoord
	 *            the left boundary of the tile
	 * @param yCoord
	 *            the upper boundary of the tile
	 * @param width
	 *            the width of the tile's drawing-space
	 * @param height
	 *            the height of the tile's drawing-space
	 */
	private static void drawRiver(final Graphics pen, final River river,
			final int xCoord, final int yCoord, final int width,
			final int height) {
		switch (river) {
		case East:
			pen.fillRect((int) (width / TWO) + xCoord,
					(int) (height * SEVEN_SIXTEENTHS) + yCoord,
					(int) (width / TWO), (int) (height / EIGHT));
			break;
		case Lake:
			pen.fillOval((int) (width / FOUR) + xCoord, (int) (height / FOUR)
					+ yCoord, (int) (width / TWO), (int) (height / TWO));
			break;
		case North:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS) + xCoord, yCoord,
					(int) (width / EIGHT), (int) (height / TWO));
			break;
		case South:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS) + xCoord,
					(int) (height / TWO) + yCoord, (int) (width / EIGHT),
					(int) (height / TWO));
			break;
		case West:
			pen.fillRect(xCoord, (int) (height * SEVEN_SIXTEENTHS) + yCoord,
					(int) (width / TWO), (int) (height / EIGHT));
			break;
		default:
			// Shouldn't get here, but let's ignore it anyway
			break;
		}
	}
}
