package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import model.viewer.Unit;

/**
 * A chit to represent a unit.
 * 
 * @author Jonathan Lovelace
 */
public class UnitChit extends Chit {
	/**
	 * The margin we allow around the chit.
	 */
	private static final double MARGIN = 0.25;
	/**
	 * Constructor.
	 * 
	 * @param unit
	 *            the unit this chit represents
	 * @param listener
	 *            the object listening for clicks on this chit.
	 */
	public UnitChit(final Unit unit, final MouseListener listener) {
		super(listener);
		final StringBuilder sbuild = new StringBuilder("<html><p>Unit ");
		if (!"".equals(unit.getName())) {
			sbuild.append(unit.getName());
			sbuild.append(", ");
		}
		if (!"".equals(unit.getType())) {
			sbuild.append("of type ");
			sbuild.append(unit.getType());
			sbuild.append(", ");
		}
		sbuild.append("owned by player ");
		sbuild.append(unit.getOwner());
		sbuild.append("</p></html>");
		desc = sbuild.toString();
	}

	/**
	 * A description of the unit.
	 */
	private final String desc;

	/**
	 * @return a description of the unit, to show the user.
	 * @see view.map.main.Chit#describe()
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
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		final Color saveColor = pen.getColor();
		pen.setColor(UNIT_COLOR);
		pen.fillOval(((int) (getWidth() * MARGIN)) + 1,
				((int) (getHeight() * MARGIN)) + 1,
				((int) (getWidth() * (1.0 - MARGIN * 2.0))),
				((int) (getHeight() * (1.0 - MARGIN * 2.0))));
		pen.setColor(saveColor);
	}
	/**
	 * The color that we draw the unit.
	 */
	private static final Color UNIT_COLOR = Color.BLACK;
}
