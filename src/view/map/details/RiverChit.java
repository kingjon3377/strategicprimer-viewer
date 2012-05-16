package view.map.details;

import static view.util.DrawingNumericConstants.EIGHT;
import static view.util.DrawingNumericConstants.FOUR;
import static view.util.DrawingNumericConstants.SEVEN_SIXTEENTHS;
import static view.util.DrawingNumericConstants.TWO;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import model.map.River;
import model.map.fixtures.RiverFixture;
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
		final Graphics copy = pen.create();
		try {
			copy.setColor(Color.blue);
			for (River river : fix) {
				drawRiver(copy, river);
			}
		} finally {
			copy.dispose();
		}
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
	 */
	private void drawRiver(final Graphics pen, final River river) {
		switch (river) {
		case East:
			pen.fillRect((int) Math.round(getWidth() / TWO),
					(int) Math.round(getHeight() * SEVEN_SIXTEENTHS),
					(int) Math.round(getWidth() / TWO), (int) Math.round(getHeight() / EIGHT));
			break;
		case Lake:
			pen.fillOval((int) Math.round(getWidth() / FOUR), (int) Math.round(getHeight() / FOUR), 
					(int) Math.round(getWidth() / TWO), (int) Math.round(getHeight() / TWO));
			break;
		case North:
			pen.fillRect((int) Math.round(getWidth() * SEVEN_SIXTEENTHS), 0,
					(int) Math.round(getWidth() / EIGHT), (int) Math.round(getHeight() / TWO));
			break;
		case South:
			pen.fillRect((int) Math.round(getWidth() * SEVEN_SIXTEENTHS),
					(int) Math.round(getHeight() / TWO), (int) Math.round(getWidth() / EIGHT),
					(int) Math.round(getHeight() / TWO));
			break;
		case West:
			pen.fillRect(0, (int) Math.round(getHeight() * SEVEN_SIXTEENTHS),
					(int) Math.round(getWidth() / TWO), (int) Math.round(getHeight() / EIGHT));
			break;
		default:
			// Shouldn't get here, but let's ignore it anyway
			break;
		}
	}
}
