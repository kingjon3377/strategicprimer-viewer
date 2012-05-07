package view.map.details;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import model.map.fixtures.Fortress;
import model.viewer.FixtureTransferable;

/**
 * A chit to represent a fortress.
 * 
 * @author Jonathan Lovelace
 */
public class FortChit extends Chit {
	/**
	 * The margin we allow around the chit itself.
	 */
	private static final double MARGIN = 0.25;

	/**
	 * Constructor.
	 * 
	 * @param fort
	 *            the fortress this chit represents
	 * @param listener
	 *            the object listening for clicks on this chit.
	 */
	public FortChit(final Fortress fort, final MouseListener listener) {
		super(listener, new FixtureTransferable(fort));
		final StringBuilder sbuild = new StringBuilder("<html><p>Fortress ");
		if (!"".equals(fort.getName())) {
			sbuild.append(fort.getName());
			sbuild.append(", ");
		}
		sbuild.append("owned by ");
		sbuild.append(fort.getOwner());
		sbuild.append("</p></html>");
		desc = sbuild.toString();
	}

	/**
	 * A description of the fortress.
	 */
	private final String desc;

	/**
	 * @return a description of the fortress, to show the user.
	 */
	@Override
	public String describe() {
		return desc;
	}

	/**
	 * Paint the chit.
	 * 
	 * @param pen
	 *            the graphics context
	 * 
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		final Graphics copy = pen.create();
		try {
			copy.setColor(FORT_COLOR);
			copy.fillRect(((int) (getWidth() * MARGIN)) + 1,
				((int) (getHeight() * MARGIN)) + 1,
				((int) (getWidth() * (1.0 - MARGIN * 2.0))),
				((int) (getHeight() * (1.0 - MARGIN * 2.0))));
		} finally {
			copy.dispose();
		}
	}

	/**
	 * The color of the chit.
	 */
	private static final Color FORT_COLOR = new Color(160, 82, 45);
}
