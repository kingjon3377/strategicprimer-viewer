package view.util;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

/**
 * A panel laid out by a BorderLayout, with helper methods to assign components
 * to its different sectors in a more functional style.
 *
 * @author Jonathan Lovelace
 *
 */
public class BorderedPanel extends JPanel {
	/**
	 * Constructor.
	 */
	public BorderedPanel() {
		super(new BorderLayout());
	}
	/**
	 * Constructor.
	 * @param north the north component. Ignored if null.
	 * @param south the south component. Ignored if null.
	 * @param east the east component. Ignored if null.
	 * @param west the west component. Ignored if null.
	 * @param center the central component. Ignored if null.
	 */
	public BorderedPanel(final Component center, final Component north,
			final Component south, final Component east, final Component west) {
		this();
		if (center != null) {
			setCenter(center);
		}
		if (north != null) {
			setNorth(north);
		}
		if (south != null) {
			setSouth(south);
		}
		if (east != null) {
			setEast(east);
		}
		if (west != null) {
			setWest(west);
		}
	}
	/**
	 * @param component a component to place to the north
	 * @return this
	 */
	public final BorderedPanel setNorth(final Component component) {
		add(component, BorderLayout.NORTH);
		return this;
	}
	/**
	 * @param component a component to place to the south
	 * @return this
	 */
	public final BorderedPanel setSouth(final Component component) {
		add(component, BorderLayout.SOUTH);
		return this;
	}
	/**
	 * @param component a component to place to the east
	 * @return this
	 */
	public final BorderedPanel setEast(final Component component) {
		add(component, BorderLayout.EAST);
		return this;
	}
	/**
	 * @param component a component to place to the west
	 * @return this
	 */
	public final BorderedPanel setWest(final Component component) {
		add(component, BorderLayout.WEST);
		return this;
	}
	/**
	 * @param component a component to place in the center
	 * @return this
	 */
	public final BorderedPanel setCenter(final Component component) {
		add(component, BorderLayout.CENTER);
		return this;
	}
	/**
	 * @param component a component to place at line-start.
	 * @return this
	 */
	public final BorderedPanel setLineStart(final Component component) {
		add(component, BorderLayout.LINE_START);
		return this;
	}
	/**
	 * @param component a component to place at line-end.
	 * @return this
	 */
	public final BorderedPanel setLineEnd(final Component component) {
		add(component, BorderLayout.LINE_END);
		return this;
	}
}
