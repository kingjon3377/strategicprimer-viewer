package lovelace.util;

import javax.swing.JSplitPane;

import java.awt.Component;

/**
 * A version of {@link JSplitPane} that takes the divider location and resize
 * weight, as well as other parameters, in the same operation, and doesn't
 * require the caller to remember whether 'true' means a horizontal or vertical
 * split.
 */
public final class FunctionalSplitPane extends JSplitPane {
	private static final long serialVersionUID = 1;
	private FunctionalSplitPane(int orientation, Component left, Component right,
			double dividerLocation, double resizeWeight) {
		super(orientation, true, left, right);
		setDividerLocation(dividerLocation);
		setResizeWeight(resizeWeight);
	}

	public static FunctionalSplitPane horizontalSplit(Component left, Component right,
			double dividerLocation, double resizeWeight) {
		return new FunctionalSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right, dividerLocation, resizeWeight);
	}
	public static FunctionalSplitPane horizontalSplit(Component left, Component right,
			double dividerLocation) {
		return horizontalSplit(left, right, dividerLocation, dividerLocation);
	}
	public static FunctionalSplitPane horizontalSplit(Component left, Component right) {
		return horizontalSplit(left, right, 0.5);
	}

	public static FunctionalSplitPane verticalSplit(Component left, Component right,
			double dividerLocation, double resizeWeight) {
		return new FunctionalSplitPane(JSplitPane.VERTICAL_SPLIT, left, right, dividerLocation, resizeWeight);
	}
	public static FunctionalSplitPane verticalSplit(Component left, Component right,
			double dividerLocation) {
		return verticalSplit(left, right, dividerLocation, dividerLocation);
	}
	public static FunctionalSplitPane verticalSplit(Component left, Component right) {
		return verticalSplit(left, right, 0.5);
	}

}
