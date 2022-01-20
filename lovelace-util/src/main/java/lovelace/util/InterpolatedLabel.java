package lovelace.util;

import javax.swing.JLabel;
import java.util.function.Function;

/**
 * A JLabel that takes a function to produce its text as constructor parameters
 * and later takes an argument to pass to that function.
 *
 * @param <T> the type of the argument
 * @deprecated Generally prefer {@link FormattedLabel}, right?
 */
@Deprecated
public class InterpolatedLabel<T> extends JLabel {
	private static final long serialVersionUID = 1;
	private final Function<? super T, String> function;
	/**
	 * @param function The function to use to produce the label's text.
	 * @param defaultArgument The argument to pass to the function to
	 * produce the label's initial text.
	 */
	public InterpolatedLabel(Function<? super T, String> function, T defaultArgument) {
		super(function.apply(defaultArgument));
		this.function = function;
	}

	public void setArgument(T argument) {
		setText(function.apply(argument));
	}
}
