/**
 * This software is released as part of the Pumpernickel project.
 *
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://raw.githubusercontent.com/mickleness/pumpernickel/master/License.txt
 *
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package com.pump.window;

import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.io.Serial;
import java.util.Objects;
import javax.swing.JCheckBoxMenuItem;

/**
 * This menu item calls {@code Frame.toFront()} when the item is selected.
 *
 */
public final class SummonMenuItem extends JCheckBoxMenuItem {

	@Serial
	private static final long serialVersionUID = 1L;

	private final Frame frame;

	/**
	 * Create a new {@code SummonMenuItem}.
	 *
	 * @param f
	 *            the frame to bring to front when this menu item is activated
	 */
	public SummonMenuItem(final Frame f) {
		frame = f;
		addActionListener(this::handler);
		updateText();

		frame.addPropertyChangeListener("title", e -> updateText());

		// this UI is buggy, and has issues.
		// the main issue is that it won't even show up on Macs
		// if you use the screen menubar, and since the goal
		// is to emulate macs: why bother?
		// if(frame instanceof JFrame f)
		// setUI(new FrameMenuItemUI(f));
	}

	private void updateText() {
		String text = frame.getTitle();
		if (Objects.isNull(text) || text.isBlank())
			text = "Untitled";
		setText(text);
	}

	private void handler(final ActionEvent ignored) {
		frame.toFront();
		if (frame.getExtendedState() == Frame.ICONIFIED)
			frame.setExtendedState(Frame.NORMAL);
		setSelected(true);
	}
}
