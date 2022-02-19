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

import javax.swing.JCheckBoxMenuItem;

/**
 * This menu item calls {@code Frame.toFront()} when the item is selected.
 *
 */
public class SummonMenuItem extends JCheckBoxMenuItem {

	private static final long serialVersionUID = 1L;

	private final Frame frame;

	/**
	 * Create a new {@code SummonMenuItem}.
	 *
	 * @param f
	 *            the frame to bring to front when this menu item is activated
	 */
	public SummonMenuItem(final Frame f) {
		super();
		frame = f;
		addActionListener(ignored -> { // TODO: convert to class method?
			frame.toFront();
			if (frame.getExtendedState() == Frame.ICONIFIED)
				frame.setExtendedState(Frame.NORMAL);
			setSelected(true);
		});
		updateText();

		frame.addPropertyChangeListener("title", e -> updateText());

		// this UI is buggy, and has issues.
		// the main issue is that it won't even show up on Macs
		// if you use the screen menubar, and since the goal
		// is to emulate macs: why bother?
		// if(frame instanceof JFrame)
		// setUI(new FrameMenuItemUI((JFrame)frame));
	}

	private void updateText() {
		String text = frame.getTitle();
		if (text == null || text.trim().isEmpty())
			text = "Untitled";
		setText(text);
	}
}
