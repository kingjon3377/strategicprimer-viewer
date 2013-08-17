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
	 * @param component a component to place to the north
	 * @return this
	 */
	public BorderedPanel setNorth(final Component component) {
		add(component, BorderLayout.NORTH);
		return this;
	}
	/**
	 * @param component a component to place to the south
	 * @return this
	 */
	public BorderedPanel setSouth(final Component component) {
		add(component, BorderLayout.SOUTH);
		return this;
	}
	/**
	 * @param component a component to place to the east
	 * @return this
	 */
	public BorderedPanel setEast(final Component component) {
		add(component, BorderLayout.EAST);
		return this;
	}
	/**
	 * @param component a component to place to the west
	 * @return this
	 */
	public BorderedPanel setWest(final Component component) {
		add(component, BorderLayout.WEST);
		return this;
	}
	/**
	 * @param component a component to place in the center
	 * @return this
	 */
	public BorderedPanel setCenter(final Component component) {
		add(component, BorderLayout.CENTER);
		return this;
	}
	/**
	 * @param component a component to place at line-start.
	 * @return this
	 */
	public BorderedPanel setLineStart(final Component component) {
		add(component, BorderLayout.LINE_START);
		return this;
	}
	/**
	 * @param component a component to place at line-end.
	 * @return this
	 */
	public BorderedPanel setLineEnd(final Component component) {
		add(component, BorderLayout.LINE_END);
		return this;
	}
}
