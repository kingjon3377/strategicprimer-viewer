/*
 * @(#)WindowMenu.java
 *
 * $Date: 2014-05-06 14:07:47 -0500 (Tue, 06 May 2014) $
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

import static util.NullCleaner.assertNotNull;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This is a Window menu resembling <A HREF=
 * "http://developer.apple.com/documentation/UserExperience/Conceptual/AppleHIGuidelines/XHIGMenus/chapter_17_section_4.html#//apple_ref/doc/uid/TP30000356-TPXREF106"
 * >the menu</A> found in most Cocoa applications. This menu will automatically
 * update itself to always list all visible Frames. Their title will appear in
 * this menu, or the text "Untitled" will be used if no frame title is
 * available.
 *
 * This uses the {@link com.bric.window.WindowList} to keep track of frames,
 * their order, and their layering.
 *
 * As of this version, this class is not a perfect replica of Apple's menu. It
 * lacks a few key elements:
 *
 * 1. The "Zoom" menu item. In Java it is not directly possible to emulate this
 * behavior. Probably a JNI-based approach would be simplest way to add this
 * feature.
 *
 * 2. Window titles do not have a bullet displayed next to their name when they
 * have unsaved changes, or a diamond displayed next to their name when
 * minimized. I started to develop a <code>FrameMenuItemUI</code> to address
 * this problem, but then realized that if a Java program on Mac uses the screen
 * menubar (which is the preferred behavior): customized MenuItemUI's are
 * ignored. Apple does some slight-of-hand and maps every JMenuItem to some sort
 * of Cocoa peer, so the UI is ignored.
 *
 * 3. Holding down the option/alt key doesn't toggle menu items like "Minimize".
 * I was able to implement this when a JMenuBar is placed in the JFrame, but not
 * when the screen menubar is used.
 *
 * So ironically: I can get more Mac-like behavior on non-Macs. (Which defeats
 * the purpose.) But in the mean time: really all I personally need from my
 * Window menu is a list of available frames, so this meets my needs for now.
 *
 * @author Jeremy Wood
 * @author Jonathan Lovelace (fixups to match repository standards; hacks to fit
 *         our needs)
 */
public class WindowMenu extends JMenu {
	/** The menu item that minimizes this window. */
	protected final JMenuItem minimizeItem = new JMenuItem("Minimize");

	/** The "Bring All to Front" menu item.
	 * TODO: this is implemented hackish-ly and causes windows
	 * to flicker over one another.  I'm not sure it's worth
	 * keeping; for now the lines that add it to the menu
	 * are commented out.
	 */
	protected final JMenuItem bringItem = new JMenuItem("Bring All To Front");

	/**
	 * The Runnable to update the menu.
	 */
	protected final Runnable updateRunnable = new Runnable() {
		@Override
		public void run() {
			removeAll();
			add(minimizeItem);
			if (customItems.length != 0) {
				addSeparator();
				for (int a = 0; a < customItems.length; a++) {
					add(customItems[a]);
				}
			}
			addSeparator();
			add(bringItem);
			addSeparator();
			Frame[] frames = WindowList.getFrames(false, false, true);
			for (int a = 0; a < frames.length; a++) {
				Frame temp = frames[a];
				if (temp == null) {
					continue;
				}
				JCheckBoxMenuItem item = new SummonMenuItem(temp);
				item.setSelected(temp == myFrame);
				add(item);
			}
			fixMenuBar(myFrame, WindowMenu.this);
		}
	};

	/** On Mac often the menus won't really update without this hack-ish
	 * twist: remove the menu and re-add it.  Voila!  It's both unnecessary
	 * and crucial at the same time.
	 * @param f the frame whose menubar you need to update.
	 * @param menu the menu you need to update.
	 */
	static void fixMenuBar(final JFrame f, final JMenu menu) {
		JMenuBar mb = f.getJMenuBar();
		if (mb != null) {
			JMenu[] menus = new JMenu[mb.getMenuCount()];
			int i = -1;
			for (int a = 0; a < menus.length; a++) {
				menus[a] = mb.getMenu(a);
				if (menus[a] == menu) {
					i = a;
				}

				if (i != -1) {
					mb.remove(i);
					mb.add(menus[a]);
				}
			}
		}
	}
	/**
	 * The current frame?
	 */
	protected JFrame myFrame;

	/**
	 * The items in the Window menu other than those representing windows.
	 */
	protected final JMenuItem[] customItems;

	/** Creates a new WindowMenu for a specific JFrame.
	 *
	 * @param frame the frame that this menu belongs to.
	 */
	public WindowMenu(final JFrame frame) {
		this(frame, new JMenuItem[] {});
	}

	/** Creates a new WindowMenu for a specific JFrame.
	 *
	 * @param frame the frame that this menu belongs to.
	 * @param extraItems an optional array of extra items to put in
	 * this menu.
	 * */
	public WindowMenu(final JFrame frame, final JMenuItem[] extraItems) {
		super("Window");
		changeListener = new ChangeListener() {
			@Override
			public void stateChanged(@Nullable final ChangeEvent e) {
				SwingUtilities.invokeLater(updateRunnable);
			}
		};
		actionListener = new ActionListener() {
			@Override
			public void actionPerformed(@Nullable final ActionEvent e) {
				if (e == null) {
					return;
				}
				Object src = e.getSource();
				if (src == minimizeItem) {
					myFrame.setExtendedState(Frame.ICONIFIED);
				} else if (src == bringItem) {
					Frame[] frames = WindowList.getFrames(false, false, true);
					for (int a = 0; a < frames.length; a++) {
						if (frames[a].isVisible()
								|| frames[a].getExtendedState() == Frame.ICONIFIED) {
							frames[a].toFront();
							if (frames[a].getExtendedState() == Frame.ICONIFIED) {
								frames[a].setExtendedState(Frame.NORMAL);
							}
						}
					}
				}
			}
		};
		minimizeItem.addActionListener(actionListener);
		bringItem.addActionListener(actionListener);

		customItems = new JMenuItem[extraItems.length];
		System.arraycopy(extraItems, 0, customItems, 0, extraItems.length);

		myFrame = frame;
		minimizeItem.setAccelerator(KeyStroke.getKeyStroke('M', Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));

		WindowList.addChangeListener(assertNotNull(changeListener));
		updateRunnable.run();
	}
}
