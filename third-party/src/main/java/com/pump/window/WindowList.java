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

import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This keeps track of the origin and layering of windows and frames within this
 * JVM.
 * <P>
 * By "origin" I mean: when that window was first activated. This serves as a
 * running list of windows in the order they were used.
 * <P>
 * All windows are tracked by {@code WeakReferences}, just to make sure
 * this static monitor doesn't accidentally let windows linger in memory that
 * should otherwise be marked for garbage collection.
 * <P>
 * The layering is monitored by an {@code AWTEventListener} that listens
 * for all {@code WindowEvent.WINDOW_ACTIVATED} events. Whenever a window
 * is activated: it gets put on the top of the stack of windows.
 * <P>
 * When any change occurs to any of the lists in this class: the
 * {@code ChangeListeners} are notified.
 * <P>
 * This can't run inside a Java sandbox because it invokes
 * {@code Toolkit.getDefaultToolkit().addAWTEventListener(..)}.
 *
 */
public final class WindowList {

	private static final ArrayList<ChangeListener> changeListeners = new ArrayList<>();

	private static final AWTEventListener windowListener = new AWTEventListener() {
		WeakReference<?>[] visibleWindows = new WeakReference<?>[0];
		WeakReference<?>[] invisibleWindows = new WeakReference<?>[0];
		WeakReference<?>[] visibleFrames = new WeakReference<?>[0];
		WeakReference<?>[] invisibleFrames = new WeakReference<?>[0];
		WeakReference<?>[] iconifiedFrames = new WeakReference<?>[0];

		public void eventDispatched(final AWTEvent e) {
			if (e instanceof WindowEvent) {

				boolean changed = false;

				if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
					final Window window = (Window) (e.getSource());

					updateWindowList: {
						for (final Reference<Window> r : windowList) {
							if (r.get() == window) {
								break updateWindowList;
							}
						}
						windowList.add(new WeakReference<>(window));
						changed = true;
					}

					updateWindowLayerList: {
						int a = 0;
						while (a < windowLayerList.size()) {
							final Reference<Window> r = windowLayerList.get(a);
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

				/*
				 * Run this test later. If we received a WINDOW_CLOSING event,
				 * then a window is not yet invisible... but it will by the time
				 * this runnable runs.
				 */
				SwingUtilities.invokeLater(proofRunnable);
			}
		}

		final Runnable proofRunnable = () -> {
			boolean changed = false;

			Window[] newVisibleWindows = getWindows(false, false);
			Window[] newInvisibleWindows = getWindows(false, true);
			Frame[] newVisibleFrames = getFrames(false, false, false);
			Frame[] newInvisibleFrames = getFrames(false, true, false);
			Frame[] newIconifiedFrames = getFrames(false, false, true);

			if (!arrayEquals(newVisibleWindows, visibleWindows))
				changed = true;
			if (!arrayEquals(newInvisibleWindows, invisibleWindows))
				changed = true;
			if (!arrayEquals(newVisibleFrames, visibleFrames))
				changed = true;
			if (!arrayEquals(newInvisibleFrames, invisibleFrames))
				changed = true;
			if (!arrayEquals(newIconifiedFrames, iconifiedFrames))
				changed = true;

			visibleWindows = wrap(newVisibleWindows);
			invisibleWindows = wrap(newInvisibleWindows);
			visibleFrames = wrap(newVisibleFrames);
			invisibleFrames = wrap(newInvisibleFrames);
			iconifiedFrames = wrap(newIconifiedFrames);

			if (changed) {
				fireChangeListeners();
			}
		};

		private WeakReference<?>[] wrap(final Object[] array) {
			final WeakReference<?>[] references = new WeakReference[array.length];
			for (int a = 0; a < references.length; a++) {
				references[a] = new WeakReference<>(array[a]);
			}
			return references;
		}

		private boolean arrayEquals(final Object[] obj1, final WeakReference<?>[] obj2) {
			if (obj1.length != obj2.length)
				return false;
			for (int a = 0; a < obj1.length; a++) {
				if (obj1[a] != obj2[a].get())
					return false;
			}
			return true;
		}
	};

	/**
	 * References to every Window that has been activated, in order of when they
	 * were made active.
	 */
	private static final ArrayList<WeakReference<Window>> windowLayerList = new ArrayList<>();
	/**
	 * References to every Window that has been activated, in order of
	 * z-layering.
	 */
	private static final ArrayList<WeakReference<Window>> windowList = new ArrayList<>();

	static {
		Toolkit.getDefaultToolkit().addAWTEventListener(windowListener,
				AWTEvent.WINDOW_EVENT_MASK);
		final Window[] windows = Window.getWindows();
		for (final Window window : windows) {
			windowLayerList.add(new WeakReference<>(window));
			windowList.add(new WeakReference<>(window));
		}
	}

	/**
	 * Returns a list of windows.
	 *
	 * @param sortByLayer
	 *            whether to sort by layer or origin. If this is true, then the
	 *            lowest index corresponds to the window farthest behind; the
	 *            highest index represents the highest window. If this is false,
	 *            then the lowest index corresponds to the first window
	 *            activated, and the highest index is the most recently
	 *            activated window.
	 * @param includeInvisible
	 *            if this is false then only visible Windows will be returned.
	 *            Otherwise all Windows will be returned.
	 */
	public static Window[] getWindows(final boolean sortByLayer,
	                                  final boolean includeInvisible) {
		final ArrayList<WeakReference<Window>> list = sortByLayer ? windowLayerList
				: windowList;
		final List<Window> returnValue = new ArrayList<>();
		int a = 0;
		while (a < list.size()) {
			final WeakReference<Window> r = list.get(a);
			final Window w = r.get();
			if (w == null) {
				list.remove(a);
			} else {
				if (includeInvisible || w.isVisible()) {
					returnValue.add(w);
				}
				a++;
			}
		}
		return returnValue.toArray(Window[]::new);
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
	 *            if this is false then visible Frames will be returned. If this
	 *            is true then all Frames will be returned, so the next argument
	 *            is meaningless.
	 * @param includeIconified
	 *            if this is true then iconified Frames will be returned.
	 */
	public static Frame[] getFrames(final boolean sortByLayer,
	                                final boolean includeInvisible, final boolean includeIconified) {
		final ArrayList<WeakReference<Window>> list = sortByLayer ? windowLayerList
				: windowList;
		final List<Frame> returnValue = new ArrayList<>();
		int a = 0;
		while (a < list.size()) {
			final WeakReference<Window> r = list.get(a);
			final Window w = r.get();
			if (w == null) {
				list.remove(a);
			} else {
				if (w instanceof final Frame f) {
					if (includeInvisible || f.isVisible()) {
						returnValue.add(f);
					} else if (includeIconified
							&& f.getExtendedState() == Frame.ICONIFIED) {
						returnValue.add(f);
					}
				}
				a++;
			}
		}
		return returnValue.toArray(Frame[]::new);
	}

	/**
	 * Add a ChangeListener.
	 * <P>
	 * This listener will be notified when new windows are activated, layering
	 * of windows changes, or windows close.
	 *
	 * @param l
	 *            a new ChangeListener.
	 */
	public static void addChangeListener(final ChangeListener l) {
		if (changeListeners.contains(l))
			return;
		changeListeners.add(l);
	}

	/**
	 * Remove a ChangeListener.
	 */
	public static void removeChangeListener(final ChangeListener l) {
		changeListeners.remove(l);
	}

	static void fireChangeListeners() {
		for (final ChangeListener l : changeListeners) {
			try {
				l.stateChanged(new ChangeEvent(WindowList.class));
			} catch (final Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private WindowList() {
	}
}
