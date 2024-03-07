package lovelace.util;

import javax.swing.JSplitPane;

import java.awt.Component;
import java.io.Serial;

/**
 * A version of {@link JSplitPane} that takes the divider location and resize
 * weight, as well as other parameters, in the same operation, and doesn't
 * require the caller to remember whether 'true' means a horizontal or vertical
 * split.
 */
public final class FunctionalSplitPane extends JSplitPane {
	@Serial
	private static final long serialVersionUID = 1;

	private FunctionalSplitPane(final int orientation, final Component left, final Component right,
								final double dividerLocation, final double resizeWeight) {
		super(orientation, true, left, right);
		setDividerLocation(dividerLocation);
		setResizeWeight(resizeWeight);
	}

	public static FunctionalSplitPane horizontalSplit(final Component left, final Component right,
													  final double dividerLocation, final double resizeWeight) {
		return new FunctionalSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right, dividerLocation, resizeWeight);
	}

	public static FunctionalSplitPane horizontalSplit(final Component left, final Component right,
													  final double dividerLocation) {
		return horizontalSplit(left, right, dividerLocation, dividerLocation);
	}

	public static FunctionalSplitPane horizontalSplit(final Component left, final Component right) {
		return horizontalSplit(left, right, 0.5);
	}

	public static FunctionalSplitPane verticalSplit(final Component left, final Component right,
													final double dividerLocation, final double resizeWeight) {
		return new FunctionalSplitPane(JSplitPane.VERTICAL_SPLIT, left, right, dividerLocation, resizeWeight);
	}

	public static FunctionalSplitPane verticalSplit(final Component left, final Component right,
													final double dividerLocation) {
		return verticalSplit(left, right, dividerLocation, dividerLocation);
	}

	public static FunctionalSplitPane verticalSplit(final Component left, final Component right) {
		return verticalSplit(left, right, 0.5);
	}

}
