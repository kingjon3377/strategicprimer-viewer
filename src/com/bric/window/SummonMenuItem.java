/*
 * @(#)SummonMenuItem.java
 *
 * $Date: 2014-03-13 03:15:48 -0500 (Thu, 13 Mar 2014) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Jeremy Wood, a modified 3-clause BSD license.
 *
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 *
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.window;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBoxMenuItem;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This menu item calls <code>Frame.toFront()</code> when the item is selected.
 *
 * @author Jeremy Wood
 * @author Jonathan Lovelace (fixups to match repository standards; hacks to fit
 *         our needs)
 *
 */
public class SummonMenuItem extends JCheckBoxMenuItem {
	/**
	 * The window this menu item represents.
	 */
	protected final Frame frame;

	/** Create a new <code>SummonMenuItem</code>.
	 *
	 * @param f the frame to bring to front when this menu item is activated
	 */
	public SummonMenuItem(final Frame f) {
		super();
		frame = f;
		addActionListener(actionListener);
		updateText();

		frame.addPropertyChangeListener("title", new PropertyChangeListener() {
			@Override
			public void propertyChange(@Nullable final PropertyChangeEvent e) {
				updateText();
			}
		});
		actionListener = new ActionListener() {
			@Override
			public void actionPerformed(@Nullable final ActionEvent e) {
				frame.toFront();
				if (frame.getExtendedState() == Frame.ICONIFIED) {
					frame.setExtendedState(Frame.NORMAL);
				}
				setSelected(true);
			}
		};
	}
	/**
	 * Update the button to match the window's title.
	 */
	protected final void updateText() {
		String text = frame.getTitle();
		if (text == null || text.trim().length() == 0) {
			text = "Untitled";
		}
		setText(text);
	}
}
