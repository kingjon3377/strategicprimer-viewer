package lovelace.util;

import goldberg.ImprovedComboBox;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * Extends {@link ImprovedComboBox} to keep a running collection of values.
 */
public class MemoizedComboBox extends ImprovedComboBox<String> {
	@Serial
	private static final long serialVersionUID = 1L;

	private final Consumer<String> logger;

	public MemoizedComboBox(final Consumer<String> logger) {
		this.logger = logger;
	}

	public MemoizedComboBox() {
		this(LovelaceLogger::error);
	}

	/**
	 * The values we've had in the past.
	 */
	private final Set<String> values = new HashSet<>();

	/**
	 * Clear the combo box, but if its value was one we haven't had
	 * previously, add it to the drop-down list.
	 */
	public void checkAndClear() {
		final String selectedItem = getSelectedItem();
		if (Objects.nonNull(selectedItem)) {
			final String item = selectedItem.strip();
			if (!item.isEmpty() && !values.contains(item)) {
				values.add(item);
				addItem(item);
			}
			setSelectedItem(null);
		}
	}

	@Override
	public @Nullable String getSelectedItem() {
		final Object retval = super.getSelectedItem();
		if (Objects.isNull(retval)) {
			return null;
		} else if (retval instanceof final String s) {
			return s.strip();
		} else {
			return retval.toString().strip();
		}
	}

	@Override
	public void setSelectedItem(final @Nullable Object selectedItem) {
		if (selectedItem instanceof String || Objects.isNull(selectedItem)) {
			super.setSelectedItem(selectedItem);
		} else {
			logger.accept("Failed to set selectedItem: must be a String.");
		}
	}

	public @Nullable String getSelectedString() {
		final Object inner = getEditor().getEditorComponent();
		if (inner instanceof final JTextField tf) {
			final String text = tf.getText().strip();
			if (!text.isEmpty()) {
				setSelectedItem(text);
				return text;
			}
		}
		return getSelectedItem();
	}

	public void addSubmitListener(final ActionListener listener) {
		final Object inner = getEditor().getEditorComponent();
		if (inner instanceof final JTextField tf) {
			tf.addActionListener(listener);
		} else {
			logger.accept("Editor wasn't a text field, but a " + inner.getClass().getName());
		}
	}
}
