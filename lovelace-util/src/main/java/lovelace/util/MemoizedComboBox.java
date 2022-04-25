package lovelace.util;

import goldberg.ImprovedComboBox;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * Extends {@link ImprovedComboBox} to keep a running collection of values.
 */
public class MemoizedComboBox extends ImprovedComboBox<String> {
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
		if (selectedItem == null) {
			return;
		} else {
			final String item = selectedItem.trim();
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
		if (retval == null) {
			return null;
		} else if (retval instanceof String) {
			return ((String) retval).trim();
		} else {
			return retval.toString().trim();
		}
	}

	@Override
	public void setSelectedItem(final @Nullable Object selectedItem) {
		if (selectedItem instanceof String || selectedItem == null) {
			super.setSelectedItem(selectedItem);
		} else {
			logger.accept("Failed to set selectedItem: must be a String.");
		}
	}

	public String getSelectedString() {
		final Object inner = getEditor().getEditorComponent();
		if (inner instanceof JTextField) {
			final String text = ((JTextField) inner).getText().trim();
			if (!text.isEmpty()) {
				setSelectedItem(text);
				return text;
			}
		}
		return getSelectedItem();
	}

	public void addSubmitListener(final ActionListener listener) {
		final Object inner = getEditor().getEditorComponent();
		if (inner instanceof JTextField) {
			((JTextField) inner).addActionListener(listener);
		} else {
			logger.accept("Editor wasn't a text field, but a " + inner.getClass().getName());
		}
	}
}
