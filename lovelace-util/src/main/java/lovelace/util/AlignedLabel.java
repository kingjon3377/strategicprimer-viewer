package lovelace.util;

import javax.swing.JLabel;

/**
 * A {@link JLabel} that takes its alignment configuration as initializer parameters.
 */
public class AlignedLabel extends JLabel {
    private static final long serialVersionUID = 1L;

    public AlignedLabel(final String text, final float alignmentX, final float alignmentY) {
        super(text);
        setAlignmentX(alignmentX);
        setAlignmentY(alignmentY);
    }
}
