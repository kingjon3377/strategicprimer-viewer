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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This keeps track of the origin and layering of windows and frames within this
 * JVM.
 *
 * By "origin" I mean: when that window was first activated. This serves as a
 * running list of windows in the order they were used.
 *
 * All windows are tracked by {@code WeakReferences}, just to make sure
 * this static monitor doesn't accidentally let windows linger in memory that
 * should otherwise be marked for garbage collection.
 *
 * The layering is monitored by an {@code AWTEventListener} that listens
 * for all {@code WindowEvent.WINDOW_ACTIVATED} events. Whenever a window
 * is activated: it gets put on the top of the stack of windows.
 *
 * When any change occurs to any of the lists in this class: the
 * {@code ChangeListeners} are notified.
 *
 * This can't run inside a Java sandbox because it invokes
 * {@code Toolkit.getDefaultToolkit().addAWTEventListener(..)}.
 *
 */
public final class WindowList {

	private static final Collection<ChangeListener> changeListeners = new ArrayList<>();

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

					if (windowList.stream().noneMatch(r -> r.get() == window)) {
						windowList.add(new WeakReference<>(window));
						changed = true;
					}

					int a = 0;
					boolean found = false;
					while (a < windowLayerList.size()) {
						final Reference<Window> r = windowLayerList.get(a);
						if (r.get() == window) {
							if (a == windowLayerList.size() - 1) {
								found = true;
								break;
							}
							windowLayerList.remove(a);
						} else {
							a++;
						}
					}
					if (found) {
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

			Window[] newVisibleWindows = getWindows(WindowSorting.Origin, EnumSet.noneOf(WindowFiltering.class));
			Window[] newInvisibleWindows = getWindows(WindowSorting.Origin, EnumSet.of(WindowFiltering.Invisible));
			Frame[] newVisibleFrames = getFrames(WindowSorting.Origin, EnumSet.noneOf(WindowFiltering.class))
					.toArray(Frame[]::new);
			Frame[] newInvisibleFrames = getFrames(WindowSorting.Origin, EnumSet.of(WindowFiltering.Invisible))
					.toArray(Frame[]::new);
			Frame[] newIconifiedFrames = getFrames(WindowSorting.Origin, EnumSet.of(WindowFiltering.Iconified))
					.toArray(Frame[]::new);

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

		private static WeakReference<?>[] wrap(final Object[] array) {
			final WeakReference<?>[] references = new WeakReference[array.length];
			for (int a = 0; a < references.length; a++) {
				references[a] = new WeakReference<>(array[a]);
			}
			return references;
		}

		private static boolean arrayEquals(final Object[] obj1, final WeakReference<?>[] obj2) {
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
	private static final List<WeakReference<Window>> windowLayerList = new ArrayList<>();
	/**
	 * References to every Window that has been activated, in order of
	 * z-layering.
	 */
	private static final List<WeakReference<Window>> windowList = new ArrayList<>();

	static {
		Toolkit.getDefaultToolkit().addAWTEventListener(windowListener,
				AWTEvent.WINDOW_EVENT_MASK);
		final Window[] windows = Window.getWindows();
		for (final Window window : windows) {
			windowLayerList.add(new WeakReference<>(window));
			windowList.add(new WeakReference<>(window));
		}
	}

	public enum WindowSorting {
		/**
		 * Sort so that the lowest index corresponds to the window farthest behind, and
		 * the highest index represents the highest window.
		 */
		Layer,
		/**
		 * Sort so that the lowest index corresponds to the first window activated, and
		 * the highest index is the most recently activated window.
		 */
		Origin
	}

	public enum WindowFiltering {
		/**
		 * Include even not-visible windows/frames.
		 */
		Invisible,
		/**
		 * Include windows/frames that have been iconified.
		 */
		Iconified
	}

	/**
	 * Returns a list of windows.
	 *
	 * @param sorting
	 *            how to sort: by layer (furthest behind to highest) or origin (first window activated to most recently
	 *            activated).
	 * @param filtering
	 *            what not-obviously-visible windows to include, if any. Only invisible windows are supported by this
	 *            method.
	 */
	public static Window[] getWindows(final WindowSorting sorting,
									  final Set<WindowFiltering> filtering) {
		final List<WeakReference<Window>> list = switch (sorting) {
			case Layer -> windowLayerList;
			case Origin -> windowList;
		};
		final boolean includeInvisible = filtering.contains(WindowFiltering.Invisible);
		final Collection<Window> returnValue = new ArrayList<>();
		int a = 0;
		while (a < list.size()) {
			final WeakReference<Window> r = list.get(a);
			final Window w = r.get();
			if (Objects.isNull(w)) {
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
	 * @param bitField a field in which one or more bits may be set
	 * @param bitToCheck a specific bit
	 * @return whether the 'bitToCheck' field is set in 'bitField'
	 */
	private static boolean containsBit(final int bitField, final int bitToCheck) {
		return (bitField & bitToCheck) == bitToCheck;
	}

	/**
	 * Returns a list of frames.
	 *
	 * @param sorting
	 *            how to sort: by layer or origin (furthest behind to highest) or origin (first frame activated to most
	 *            recent).
	 * @param filtering Which not-obviously-visible frames to include, if any
	 */
	public static Collection<Frame> getFrames(final WindowSorting sorting, final Set<WindowFiltering> filtering) {
		final List<WeakReference<Window>> list = switch (sorting) {
			case Layer -> windowLayerList;
			case Origin -> windowList;
		};
		final boolean includeInvisible = filtering.contains(WindowFiltering.Invisible);
		final boolean includeIconified = filtering.contains(WindowFiltering.Iconified);
		final Collection<Frame> returnValue = new ArrayList<>();
		int a = 0;
		while (a < list.size()) {
			final WeakReference<Window> r = list.get(a);
			final Window w = r.get();
			if (Objects.isNull(w)) {
				list.remove(a);
			} else {
				if (w instanceof final Frame f) {
					if (includeInvisible || f.isVisible()) {
						returnValue.add(f);
					} else if (includeIconified
							&& containsBit(f.getExtendedState(), Frame.ICONIFIED)) {
						returnValue.add(f);
					}
				}
				a++;
			}
		}
		return Collections.unmodifiableCollection(returnValue);
	}

	/**
	 * Add a ChangeListener.
	 *
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
