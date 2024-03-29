package lovelace.util;

import javax.swing.JLabel;
import java.io.Serial;

/**
 * A JLabel that takes a format string in its constructor and later takes format-string arguments to produce its text.
 */
public class FormattedLabel extends JLabel {
	@Serial
	private static final long serialVersionUID = 1;
	private final String formatString;

	/**
	 * @param formatString     The format string to use to produce the label's text.
	 * @param defaultArguments The arguments to plug into the format string
	 *                         to produce the label's initial text.
	 */
	public FormattedLabel(final String formatString, final Object... defaultArguments) {
		super(formatString.formatted(defaultArguments));
		this.formatString = formatString;
	}

	/**
	 * Change the arguments and regenerate the label's text.
	 */
	public void setArguments(final Object... arguments) {
		setText(formatString.formatted(arguments));
	}

	@Override
	public String toString() {
		return "FormattedLabel with formatString='" + formatString + "'}";
	}
}
