package view.util;

import java.awt.Component;

import javax.swing.JSplitPane;

/**
 * A version of JSplitPane that takes the divider location and resize weight, as
 * well as the other parameters, in its constructor.
 *
 * @author Jonathan Lovelace
 *
 */
public class SplitWithWeights extends JSplitPane {
	/**
	 * Constructor.
	 *
	 * @param orient the orientation of the panel.
	 * @param divLoc the divider location
	 * @param resWeight the resize weight
	 * @param left the left or top component
	 * @param right the right or bottom component
	 */
	public SplitWithWeights(final int orient, final double divLoc,
			final double resWeight, final Component left, final Component right) {
		super(orient, true, left, right);
		setDividerLocation(divLoc);
		setResizeWeight(resWeight);
	}
}
