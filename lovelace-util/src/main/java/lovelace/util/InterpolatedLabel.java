package lovelace.util;

import javax.swing.JLabel;
import java.io.Serial;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

/**
 * A JLabel that takes a function to produce its text as constructor parameters
 * and later takes an argument to pass to that function. {@link FormattedLabel}
 * should be used instead in cases where the label can be passed a value that
 * is directly rendered, but this is needed for cases where the label needs to
 * <i>calculate</i> the value from a provided argument.
 *
 * @param <T> the type of the argument
 */
public final class InterpolatedLabel<T> extends JLabel {
	@Serial
	private static final long serialVersionUID = 1L;
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

	@Override
	public String toString() {
		return "InterpolatedLabel currently showing " + getText();
	}
}
