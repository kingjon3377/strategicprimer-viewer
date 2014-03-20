package view.util;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A JPanel laid out by a BoxLayout, with helper methods.
 *
 * @author Jonathan Lovelace
 *
 */
public class BoxPanel extends JPanel {
	/**
	 * Constructor.
	 *
	 * @param horiz If true, the panel is laid out on the line axis; if false,
	 *        on the page axis.
	 */
	public BoxPanel(final boolean horiz) {
		super();
		horizontal = horiz;
		if (horizontal) {
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		} else {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		}
	}

	/**
	 * If true, the panel is laid out on the line axis; if false, on the page
	 * axis.
	 */
	private final boolean horizontal;

	/**
	 * Add "glue" between components.
	 */
	public final void addGlue() {
		if (horizontal) {
			add(Box.createHorizontalGlue());
		} else {
			add(Box.createVerticalGlue());
		}
	}

	/**
	 * Add a rigid area between components.
	 *
	 * @param dim how big to make it in the dimension that counts.
	 */
	public final void addRigidArea(final int dim) {
		if (horizontal) {
			add(Box.createRigidArea(new Dimension(dim, 0)));
		} else {
			add(Box.createRigidArea(new Dimension(0, dim)));
		}
	}
}
