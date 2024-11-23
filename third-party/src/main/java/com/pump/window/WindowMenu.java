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

import org.jetbrains.annotations.Nullable;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import java.io.Serial;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

/**
 * This is a Window menu resembling <A HREF=
 * "http://developer.apple.com/documentation/UserExperience/Conceptual/AppleHIGuidelines/XHIGMenus/chapter_17_section_4.html#//apple_ref/doc/uid/TP30000356-TPXREF106"
 * >the menu</A> found in most Cocoa applications.
 *
 * This menu will automatically update itself to always list all visible Frames.
 * Their title will appear in this menu, or the text "Untitled" will be used if
 * no frame title is available.
 *
 * This uses the {@link com.pump.window.WindowList} to keep track of frames,
 * their order, and their layering.
 *
 * As of this version, this class is not a perfect replica of Apple's menu. It
 * lacks a few key elements: <BR>
 * 1. The "Zoom" menu item. In Java it is not directly possible to emulate this
 * behavior. Probably a JNI-based approach would be simplest way to add this
 * feature. <BR>
 * 2. Window titles do not have a bullet displayed next to their name when they
 * have unsaved changes, or a diamond displayed next to their name when
 * minimized. I started to develop a {@code FrameMenuItemUI} to address
 * this problem, but then realized that if a Java program on Mac uses the screen
 * menubar (which is the preferred behavior): customized MenuItemUI's are
 * ignored. Apple does some slight-of-hand and maps every JMenuItem to some sort
 * of Cocoa peer, so the UI is ignored. <BR>
 * 3. Holding down the option/alt key doesn't toggle menu items like "Minimize".
 * I was able to implement this when a JMenuBar is placed in the JFrame, but not
 * when the screen menubar is used.
 *
 * So ironically: I can get more Mac-like behavior on non-Macs. (Which defeats
 * the purpose.) But in the mean time: really all I personally need from my
 * Window menu is a list of available frames, so this meets my needs for now.
 *
 * This can't run inside a Java sandbox because it refers to the WindowList
 * which invokes
 * {@code Toolkit.getDefaultToolkit().addAWTEventListener(..)}.
 *
 * @see <a href=
 *      "https://javagraphics.blogspot.com/2008/11/windows-adding-window-menu.html">Windows:
 *      Adding a Window Menu</a>
 */
public class WindowMenu extends JMenu {
	@Serial
	private static final long serialVersionUID = 1L;

	/** The menu item that minimizes this window. */
	private final JMenuItem minimizeItem = new JMenuItem("Minimize");

	/**
	 * The "Bring All to Front" menu item. TODO: this is implemented hackish-ly
	 * and causes windows to flicker over one another. I'm not sure it's worth
	 * keeping; for now the lines that add it to the menu are commented out.
	 */
	private final JMenuItem bringItem = new JMenuItem("Bring All To Front");

	private void update() {
		removeAll();
		add(minimizeItem);
		if (customItems.length != 0) {
			addSeparator();
			Stream.of(customItems).filter(Objects::nonNull).forEach(this::add);
		}
		addSeparator();
		add(bringItem);
		addSeparator();
		final Frame[] frames = WindowList.getFrames(WindowList.WindowSorting.Origin,
				EnumSet.of(WindowList.WindowFiltering.Iconified));
		for (final Frame frame : frames) {
			final JCheckBoxMenuItem item = new SummonMenuItem(frame);
			item.setSelected(frame == myFrame);
			add(item);
		}
	}

	private final JFrame myFrame;

	private final @Nullable JMenuItem [] customItems;

	/**
	 * Creates a new WindowMenu for a specific JFrame.
	 *
	 * @param frame
	 *            the frame that this menu belongs to.
	 */
	public WindowMenu(final JFrame frame) {
		this(frame, new JMenuItem[]{});
	}

	/**
	 * Creates a new WindowMenu for a specific JFrame.
	 *
	 * @param frame
	 *            the frame that this menu belongs to.
	 * @param extraItems
	 *            an optional array of extra items to put in this menu.
	 */
	public WindowMenu(final JFrame frame, final JMenuItem... extraItems) {
		super("Window");
		myFrame = frame;
		minimizeItem.addActionListener(ignored -> myFrame.setExtendedState(Frame.ICONIFIED));
		final ActionListener bringToFrontListener = e -> {
			final Frame[] frames = WindowList.getFrames(WindowList.WindowSorting.Origin,
					EnumSet.of(WindowList.WindowFiltering.Iconified));
			for (final Frame w : frames) {
				if (w.isVisible() || frame
						.getExtendedState() == Frame.ICONIFIED) {
					w.toFront();
					if (w.getExtendedState() == Frame.ICONIFIED)
						w.setExtendedState(Frame.NORMAL);
				}
			}
		};
		bringItem.addActionListener(bringToFrontListener);

		customItems = new JMenuItem[extraItems.length];
		System.arraycopy(extraItems, 0, customItems, 0, extraItems.length);

		minimizeItem.setAccelerator(KeyStroke.getKeyStroke('M',
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		final ChangeListener changeListener = e -> SwingUtilities.invokeLater(this::update);
		WindowList.addChangeListener(changeListener);
		update();
	}
}
