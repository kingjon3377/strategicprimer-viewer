package lovelace.util;

import javax.swing.JLabel;

/**
 * A {@link JLabel} that takes its alignment configuration as initializer parameters.
 */
public class AlignedLabel extends JLabel {
	public AlignedLabel(String text, float alignmentX, float alignmentY) {
		super(text);
		super.setAlignmentX(alignmentX);
		super.setAlignmentY(alignmentY);
	}
}
