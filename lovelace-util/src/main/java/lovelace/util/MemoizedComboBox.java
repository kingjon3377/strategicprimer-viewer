package lovelace.util;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * Extends {@link ImprovedComboBox} to keep a running collection of values.
 */
public class MemoizedComboBox extends ImprovedComboBox<String> {

	private static final Logger LOGGER = Logger.getLogger(MemoizedComboBox.class.getName());

	private final Consumer<String> logger;

	public MemoizedComboBox(final Consumer<String> logger) {
		this.logger = logger;
	}

	public MemoizedComboBox() {
		this(LOGGER::severe);
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
		final Object selectedItem = getSelectedItem();
		if (selectedItem == null) {
			return;
		} else if (selectedItem instanceof String) {
			String item = ((String) selectedItem).trim();
			if (!item.isEmpty() && !values.contains(item)) {
				values.add(item);
				addItem(item);
			}
			setSelectedItem(null);
		} else {
			throw new IllegalStateException("Only supports Strings");
		}
	}

	@Override
	@Nullable
	public String getSelectedItem() {
		Object retval = super.getSelectedItem();
		if (retval == null) {
			return null;
		} else if (retval instanceof String) {
			return ((String) retval).trim();
		} else {
			return retval.toString().trim();
		}
	}

	@Override
	public void setSelectedItem(@Nullable final Object selectedItem) {
		if (selectedItem instanceof String || selectedItem == null) {
			super.setSelectedItem(selectedItem);
		} else {
			logger.accept("Failed to set selectedItem: must be a String.");
		}
	}

	public String getSelectedString() {
		Object inner = getEditor().getEditorComponent();
		if (inner instanceof JTextField) {
			String text = ((JTextField) inner).getText().trim();
			if (!text.isEmpty()) {
				setSelectedItem(text);
				return text;
			}
		}
		return getSelectedItem();
	}

	public void addSubmitListener(final ActionListener listener) {
		Object inner = getEditor().getEditorComponent();
		if (inner instanceof JTextField) {
			((JTextField) inner).addActionListener(listener);
		} else {
			logger.accept("Editor wasn't a text field, but a " + inner.getClass().getName());
		}
	}
}
