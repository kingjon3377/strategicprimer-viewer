package lovelace.util;

import javax.swing.JLabel;

/**
 * A {@link JLabel} that takes its alignment configuration as initializer parameters.
 */
public class AlignedLabel extends JLabel {
	public AlignedLabel(final String text, final float alignmentX, final float alignmentY) {
		super(text);
		super.setAlignmentX(alignmentX);
		super.setAlignmentY(alignmentY);
	}
}
