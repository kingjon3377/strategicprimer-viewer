package lovelace.util;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.io.Serial;

/**
 * A {@link JPanel} laid out by a {@link BoxLayout}.
 */
public final class BoxPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The possible axes that a {@link BoxLayout} can be laid out on.
	 */
	public enum BoxAxis {
		LineAxis(BoxLayout.LINE_AXIS, "LINE_AXIS"),
		PageAxis(BoxLayout.PAGE_AXIS, "PAGE_AXIS");
		public final int axis;
		private final String str;

		BoxAxis(final int axis, final String str) {
			this.axis = axis;
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	/**
	 * Which direction the panel is laid out, for use in the helper methods.
	 */
	private final BoxAxis axis;

	@SuppressWarnings("MagicConstant")
	public BoxPanel(final BoxAxis layoutAxis) {
		axis = layoutAxis;
		setLayout(new BoxLayout(this, axis.axis));
	}

	/**
	 * Add "glue" (elasticity) between components.
	 */
	public void addGlue() {
		switch (axis) {
			case LineAxis -> add(Box.createHorizontalGlue());
			case PageAxis -> add(Box.createVerticalGlue());
		}
	}

	/**
	 * Add a rigid (fixed-size) area between components.
	 */
	public void addRigidArea(final int dimension) {
		final Dimension dimensionObject = switch (axis) {
			case LineAxis -> new Dimension(dimension, 0);
			case PageAxis -> new Dimension(0, dimension);
		};
		add(Box.createRigidArea(dimensionObject));
	}

	/* Create a panel laid out by a {@link BoxLayout} on the line axis, with glue at each end and a
	small rigid area between each component. */
	public static BoxPanel centeredHorizontalBox(final Component... items) {
		final BoxPanel retval = new BoxPanel(BoxAxis.LineAxis);
		retval.addGlue();
		boolean first = true;
		for (final Component component : items) {
			if (first) {
				first = false;
			} else {
				retval.addRigidArea(2);
			}
			retval.add(component);
		}
		retval.addGlue();
		return retval;
	}

	/**
	 * Create a panel laid out by a {@link BoxLayout} in the page axis, with glue at each end and between each
	 * component.
	 */
	public static BoxPanel verticalBox(final Component... items) {
		final BoxPanel retval = new BoxPanel(BoxAxis.PageAxis);
		retval.addGlue();
		boolean first = true;
		for (final Component component : items) {
			if (first) {
				first = false;
			} else {
				retval.addRigidArea(2);
			}
			retval.add(component);
		}
		retval.addGlue();
		return retval;
	}

	@Override
	public String toString() {
		return "BoxPanel{axis=%s}".formatted(axis);
	}
}
