package view.map;

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
	 * @see view.map.Chit#describe()
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
	public void paint(Graphics pen) {
		super.paint(pen);
		final Color saveColor = pen.getColor();
		pen.setColor(FORT_COLOR);
		pen.fillRect(((int) (getWidth() * 0.1)), ((int) (getHeight() * 0.1)),
				((int) (getWidth() * 0.9)), ((int) (getHeight() * 0.9)));
		pen.setColor(saveColor);
	}

	private static final Color FORT_COLOR = new Color(160, 82, 45);
}
