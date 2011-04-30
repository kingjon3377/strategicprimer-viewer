package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import model.viewer.Unit;
/**
 * A chit to represent a unit.
 * @author Jonathan Lovelace
 */
public class UnitChit extends Chit {
	private static final double MARGIN = 0.25;
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 3528975373388842318L;
	/**
	 * Constructor.
	 * @param unit the unit this chit represents
	 * @param listener the object listening for clicks on this chit.
	 */
	public UnitChit(final Unit unit, final MouseListener listener) {
		super(listener);
		final StringBuilder sbuild = new StringBuilder("<html><p>Unit ");
		if (unit.getName() != null) {
			sbuild.append(unit.getName());
			sbuild.append(", ");
		}
		if (unit.getType() != null) {
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
	 * Paint the chit
	 * @param pen the graphics context
	 *
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		final Color saveColor = pen.getColor();
		pen.setColor(UNIT_COLOR);
		pen.fillOval(((int) (getWidth() * MARGIN)), ((int) (getHeight() * MARGIN)),
				((int) (getWidth() * (1.0 - MARGIN * 2.0))), ((int) (getHeight() * (1.0 - MARGIN * 2.0))));
		pen.setColor(saveColor);
	}
	private static final Color UNIT_COLOR = Color.BLACK;
}
