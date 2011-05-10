package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import model.viewer.Fortress;

/**
 * A chit to represent a fortress.
 * 
 * @author Jonathan Lovelace
 */
public class FortChit extends Chit {
	private static final double MARGIN = 0.25;
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 3918628977804086020L;

	/**
	 * Constructor.
	 * 
	 * @param fort
	 *            the fortress this chit represents
	 * @param listener
	 *            the object listening for clicks on this chit.
	 */
	public FortChit(final Fortress fort, final MouseListener listener) {
		super(listener);
		final StringBuilder sbuild = new StringBuilder("<html><p>Fortress ");
		if (fort.getName() != null) {
			sbuild.append(fort.getName());
			sbuild.append(", ");
		}
		sbuild.append("owned by player ");
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
	 * @see view.map.main.Chit#describe()
	 */
	@Override
	public String describe() {
		return desc;
	}

	/**
	 * Paint the chit
	 * 
	 * @param pen
	 *            the graphics context
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		final Color saveColor = pen.getColor();
		pen.setColor(FORT_COLOR);
		pen.fillRect(((int) (getWidth() * MARGIN)),
				((int) (getHeight() * MARGIN)),
				((int) (getWidth() * (1.0 - MARGIN * 2.0))),
				((int) (getHeight() * (1.0 - MARGIN * 2.0))));
		pen.setColor(saveColor);
	}

	private static final Color FORT_COLOR = new Color(160, 82, 45);
}
