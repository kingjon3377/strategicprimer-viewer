package goldberg;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.awt.Component;
import java.io.Serial;

import javax.swing.JComboBox;
import javax.swing.ComboBoxModel;

/**
 * An extension to {@link JComboBox} to improve it by making the Tab key do what one expects.
 */
public class ImprovedComboBox<Element> extends JComboBox<Element> {
	@Serial
	private static final long serialVersionUID = 1L;

	public ImprovedComboBox() {
		setEditable(true);
	}

	public ImprovedComboBox(final ComboBoxModel<Element> boxModel) {
		super(boxModel);
		setEditable(true);
	}

	/**
	 * Overridden to make final.
	 */
	@Override
	public final void setEditable(final boolean aFlag) {
		super.setEditable(aFlag);
	}

	/**
	 * Handle a key-press. If Tab is pressed when the pop-up list is
	 * visible, treat it like Enter.
	 *
	 * @author Joshua Goldberg http://stackoverflow.com/a/24336768
	 */
	@Override
	public void processKeyEvent(final KeyEvent event) {
		if (event.getID() != KeyEvent.KEY_PRESSED || event.getKeyCode() != KeyEvent.VK_TAB) {
			super.processKeyEvent(event);
			return;
		}
		if (isPopupVisible()) {
			final Object source = event.getSource();
			if (source instanceof final Component c) {
				super.processKeyEvent(new KeyEvent(c, event.getID(),
						event.getWhen(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
			} else {
				throw new IllegalStateException("Source is not a component");
			}
		}
		// I *think* I deliberately chose to use == here so pressing 'shift+ctrl+tab' wouldn't
		// have any effect on which field has the focus.
		if (event.getModifiers() == 0) {
			transferFocus();
		} else if (event.getModifiers() == InputEvent.SHIFT_MASK) {
			transferFocusBackward();
		}
	}
}
