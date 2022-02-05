package lovelace.util;

import org.jetbrains.annotations.NotNull;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;

/**
 * A {@link JPanel} laid out by a {@link BoxLayout}.
 */
public class BoxPanel extends JPanel {
	/**
	 * The possible axes that a {@link BoxLayout} can be laid out on.
	 */
	public enum BoxAxis {
		LineAxis(BoxLayout.LINE_AXIS),
		PageAxis(BoxLayout.PAGE_AXIS);
		public final int axis;

		BoxAxis(final int axis) {
			this.axis = axis;
		}
	}

	/**
	 * Which direction the panel is laid out, for use in the helper methods.
	 */
	@NotNull
	private final BoxAxis axis;

	public BoxPanel(@NotNull final BoxAxis layoutAxis) {
		this.axis = layoutAxis;
		setLayout(new BoxLayout(this, axis.axis));
	}

	/**
	 * Add "glue" (elasticity) between components.
	 */
	public final void addGlue() {
		switch (axis) {
			case LineAxis:
				add(Box.createHorizontalGlue());
				break;
			case PageAxis:
				add(Box.createVerticalGlue());
				break;
		}
	}

	/**
	 * Add a rigid (fixed-size) area between components.
	 */
	public final void addRigidArea(final int dimension) {
		Dimension dimensionObject;
		switch (axis) {
			case LineAxis:
				dimensionObject = new Dimension(dimension, 0);
				break;
			case PageAxis:
				dimensionObject = new Dimension(0, dimension);
				break;
			default:
				throw new IllegalStateException("Default case in exhaustive switch");
		}
		add(Box.createRigidArea(dimensionObject));
	}

	/* Create a panel laid out by a {@link }BoxLayout} on the line axis, with glue at each end and a
	small rigid area between each component. */
	public static BoxPanel centeredHorizontalBox(final Component... items) {
		final BoxPanel retval = new BoxPanel(BoxAxis.LineAxis);
		retval.addGlue();
		boolean first = true;
		for (Component component : items) {
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
		for (Component component : items) {
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
}
