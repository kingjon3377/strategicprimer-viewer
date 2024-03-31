package lovelace.util;

import javax.swing.JLabel;
import java.io.Serial;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

/**
 * A JLabel that takes a function to produce its text as constructor parameters
 * and later takes an argument to pass to that function.
 *
 * @param <T> the type of the argument
 * @deprecated Generally prefer {@link FormattedLabel}, right?
 */
@Deprecated
public final class InterpolatedLabel<T> extends JLabel {
	@Serial
	private static final long serialVersionUID = 1;
	private final Function<? super T, String> function;

	/**
	 * @param function        The function to use to produce the label's text.
	 * @param defaultArgument The argument to pass to the function to
	 *                        produce the label's initial text.
	 */
	public InterpolatedLabel(final Function<? super T, String> function, final @Nullable T defaultArgument) {
		super(function.apply(defaultArgument));
		this.function = function;
	}

	public void setArgument(final @Nullable T argument) {
		setText(function.apply(argument));
	}
}
