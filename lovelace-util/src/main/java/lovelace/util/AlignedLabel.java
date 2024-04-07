package lovelace.util;

import javax.swing.JLabel;
import java.io.Serial;

/**
 * A {@link JLabel} that takes its alignment configuration as initializer parameters.
 */
public final class AlignedLabel extends JLabel {
	@Serial
	private static final long serialVersionUID = 1L;

	public AlignedLabel(final String text, final float alignmentX, final float alignmentY) {
		super(text);
		setAlignmentX(alignmentX);
		setAlignmentY(alignmentY);
	}
}
