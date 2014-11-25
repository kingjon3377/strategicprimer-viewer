/*
 * @(#)WindowList.java
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

import static util.NullCleaner.assertNotNull;

import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This keeps track of the origin and layering of windows and frames within this
 * JVM.
 *
 * By "origin" I mean: when that window was first activated. This serves as a
 * running list of windows in the order they were used.
 *
 * All windows are tracked by <code>WeakReferences</code>, just to make sure
 * this static monitor doesn't accidentally let windows linger in memory that
 * should otherwise be marked for garbage collection.
 *
 * The layering is monitored by an <code>AWTEventListener</code> that listens
 * for all <code>WindowEvent.WINDOW_ACTIVATED</code> events. Whenever a window
 * is activated: it gets put on the top of the stack of windows.
 *
 * When any change occurs to any of the lists in this class: the
 * <code>ChangeListeners</code> are notified.
 *
 * @author Jeremy Wood
 * @author Jonathan Lovelace (fixups to match repository standards; hacks to fit
 *         our needs)
 */
public final class WindowList {
	/**
	 * The objects listening for changes to any of the windows.
	 */
	private static ArrayList<ChangeListener> changeListeners =
			new ArrayList<>();

	/**
	 * The main listener object.
	 */
	private static AWTEventListener windowListener = new AWTEventListener() {
		/**
		 * Windows that are visible.
		 */
		protected WeakReference<?>[] visibleWindows = new WeakReference<?>[0];
		/**
		 * Windows that are invisible.
		 */
		protected WeakReference<?>[] invisibleWindows = new WeakReference<?>[0];
		/**
		 * Frames that are visible.
		 */
		protected WeakReference<?>[] visibleFrames = new WeakReference<?>[0];
		/**
		 * Frames that are invisible.
		 */
		protected WeakReference<?>[] invisibleFrames = new WeakReference<?>[0];
		/**
		 * Frames that are minimized.
		 */
		protected WeakReference<?>[] iconifiedFrames = new WeakReference<?>[0];
		@Override
		public void eventDispatched(@Nullable final AWTEvent e) {
			if (e instanceof WindowEvent) {
				boolean changed = false;

				if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
					Window window = (Window) (e.getSource());

					updateWindowList : {
						for (int a = 0; a < windowList.size(); a++) {
							final Reference<Window> r = windowList.get(a);
							if (r.get() == window) {
								break updateWindowList;
							}
						}
						windowList.add(new WeakReference<>(window));
						changed = true;
					}

					updateWindowLayerList : {
						int a = 0;
						while (a < windowLayerList.size()) {
							Reference<Window> r = windowLayerList.get(a);
							if (r.get() == window) {
								if (a == windowLayerList.size() - 1) {
									break updateWindowLayerList;
								}
								windowLayerList.remove(a);
							} else {
								a++;
							}
						}
						windowLayerList.add(new WeakReference<>(window));
						changed = true;
					}
				}

				if (changed) {
					fireChangeListeners();
				}

				proofRunnable.run();

				/** Run this test later.
				 * If we received a WINDOW_CLOSING event, then a window
				 * is not yet invisible... but it will by the time
				 * this runnable runs.
				 */
				SwingUtilities.invokeLater(proofRunnable);
			}
		}

		protected Runnable proofRunnable = new Runnable() {
			@Override
			public void run() {
				boolean changed = false;

				Window[] newVisibleWindows = getWindows(false, false);
				Window[] newInvisibleWindows = getWindows(false, true);
				Frame[] newVisibleFrames = getFrames(false, false, false);
				Frame[] newInvisibleFrames = getFrames(false, true, false);
				Frame[] newIconifiedFrames = getFrames(false, false, true);

				if (!arrayEquals(newVisibleWindows, visibleWindows)) {
					changed = true;
				}
				if (!arrayEquals(newInvisibleWindows, invisibleWindows)) {
					changed = true;
				}
				if (!arrayEquals(newVisibleFrames, visibleFrames)) {
					changed = true;
				}
				if (!arrayEquals(newInvisibleFrames, invisibleFrames)) {
					changed = true;
				}
				if (!arrayEquals(newIconifiedFrames, iconifiedFrames)) {
					changed = true;
				}

				visibleWindows = wrap(newVisibleWindows);
				invisibleWindows = wrap(newInvisibleWindows);
				visibleFrames = wrap(newVisibleFrames);
				invisibleFrames = wrap(newInvisibleFrames);
				iconifiedFrames = wrap(newIconifiedFrames);

				if (changed) {
					fireChangeListeners();
				}
			}
		};

		protected WeakReference<?>[] wrap(final Object[] array) {
			WeakReference<?>[] references = new WeakReference[array.length];
			for (int a = 0; a < references.length; a++) {
				references[a] = new WeakReference<>(array[a]);
			}
			return references;
		}

		protected boolean arrayEquals(final Object[] obj1,
				final WeakReference<?>[] obj2) {
			if (obj1.length != obj2.length) {
				return false;
			}
			for (int a = 0; a < obj1.length; a++) {
				if (obj1[a] != obj2[a].get()) {
					return false;
				}
			}
			return true;
		}
	};

	/**
	 * References to every Window that has been activated, in order of when they
	 * were made active.
	 */
	protected static ArrayList<WeakReference<Window>> windowLayerList =
			new ArrayList<>();
	/**
	 * References to every Window that has been activated, in order of
	 * z-layering.
	 */
	protected static ArrayList<WeakReference<Window>> windowList =
			new ArrayList<>();

	static {
		Toolkit.getDefaultToolkit().addAWTEventListener(windowListener,
				AWTEvent.WINDOW_EVENT_MASK);
	}

	/** Returns a list of windows.
	 *
	 * @param sortByLayer whether to sort by layer or origin.
	 * If this is true, then the lowest index corresponds to the window
	 * farthest behind; the highest index represents the highest window.
	 * If this is false, then the lowest index corresponds to the first
	 * window activated, and the highest index is the most recently
	 * activated window.
	 * @param includeInvisible include invisible Windows.
	 * @return all Windows if includeInvisible, otherwise all visible Windows.
	 */
	public static Window[] getWindows(final boolean sortByLayer,
			final boolean includeInvisible) {
		final ArrayList<WeakReference<Window>> list;
		if (sortByLayer) {
			list = windowLayerList;
		} else {
			list = windowList;
		}
		ArrayList<Window> returnValue = new ArrayList<>();
		int a = 0;
		while (a < list.size()) {
			WeakReference<Window> r = list.get(a);
			Window w = r.get();
			if (w == null) {
				list.remove(a);
			} else {
				if (includeInvisible || w.isVisible()) {
					returnValue.add(w);
				}
				a++;
			}
		}
		return assertNotNull(returnValue.toArray(new Window[returnValue.size()]));
	}

	/**
	 * Returns a list of frames.
	 *
	 * @param sortByLayer
	 *            whether to sort by layer or origin. If this is true, then the
	 *            lowest index corresponds to the frame farthest behind; the
	 *            highest index represents the highest frame. If this is false,
	 *            then the lowest index corresponds to the first frame
	 *            activated, and the highest index is the most recently
	 *            activated frame.
	 * @param includeInvisible
	 *            whether to include all Frames, even those that are invisible
	 * @param includeIconified
	 *            whether to include iconified Frames if invisible ones are not
	 *            included
	 * @return all Frames if includeInvisible; all visible and iconified ones if
	 *         includeIconified but not includeInvisible; all visible Frames
	 *         otherwise.
	 */
	public static Frame[] getFrames(final boolean sortByLayer,
			final boolean includeInvisible, final boolean includeIconified) {
		final ArrayList<WeakReference<Window>> list;
		if (sortByLayer) {
			list = windowLayerList;
		} else {
			list = windowList;
		}
		ArrayList<Frame> returnValue = new ArrayList<>();
		for (int a = 0; a < list.size(); a++) {
			WeakReference<Window> r = list.get(a);
			Window w = r.get();
			if (w == null) {
				list.remove(a);
			} else {
				if (w instanceof Frame) {
					Frame f = (Frame) w;
					if (includeInvisible || f.isVisible()) {
						returnValue.add(f);
					} else if (includeIconified
							&& f.getExtendedState() == Frame.ICONIFIED) {
						returnValue.add(f);
					}
				}
			}
		}
		return assertNotNull(returnValue.toArray(new Frame[returnValue.size()]));
	}

	/**
	 * Add a ChangeListener. This listener will be notified when new windows are
	 * activated, layering of windows changes, or windows close.
	 *
	 * @param l
	 *            a new ChangeListener.
	 */
	public static void addChangeListener(final ChangeListener l) {
		if (changeListeners.contains(l)) {
			return;
		}
		changeListeners.add(l);
	}

	/** Remove a ChangeListener.
	 * @param l the listener to remove
	 */
	public static void removeChangeListener(final ChangeListener l) {
		changeListeners.remove(l);
	}
	/**
	 * Notify listeners of changes.
	 */
	protected static void fireChangeListeners() {
		for (int a = 0; a < changeListeners.size(); a++) {
			ChangeListener l = changeListeners.get(a);
			try {
				l.stateChanged(new ChangeEvent(WindowList.class));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	/**
	 * Singleton, managed statically.
	 */
	private WindowList() {
		// Singleton.
	}
}
