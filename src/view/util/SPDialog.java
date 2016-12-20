package view.util;

import controller.map.misc.WindowCloser;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import org.eclipse.jdt.annotation.Nullable;
import util.ActionWrapper;
import util.OnMac;

/**
 * A superclass to perform setup common to dialogs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class SPDialog extends JDialog implements HotKeyCreator {
	/**
	 * Constructor.
	 * @param parent the parent component
	 * @param title the title of the dialog
	 */
	protected SPDialog(final @Nullable Frame parent, final String title) {
		super(parent, title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		createHotKey(getRootPane(), "close", new ActionWrapper(new WindowCloser(this)),
				JComponent.WHEN_IN_FOCUSED_WINDOW,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, OnMac.SHORTCUT_MASK),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	}
}
