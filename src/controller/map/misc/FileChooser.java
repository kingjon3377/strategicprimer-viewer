package controller.map.misc;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static util.NullCleaner.assertNotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import util.NullCleaner;
import view.map.main.MapFileFilter;
import view.util.FilteredFileChooser;

/**
 * A class to hide the details of choosing a file from the caller.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class FileChooser {
	/**
	 * The file we'll return, if valid.
	 */
	private File file;
	/**
	 * Whether we should return the filename (if not, we'll show the dialog,
	 * then throw an exception if that fails).
	 */
	private boolean shouldReturn;
	/**
	 * A file chooser.
	 */
	private final FilteredFileChooser chooser = new FilteredFileChooser(".",
			new MapFileFilter());
	/**
	 * Constructor. When the filename is asked for, if the given value is valid,
	 * we'll return it instead of showing a dialog.
	 *
	 * @param loc
	 *            the file to return.
	 */
	public FileChooser(final File loc) {
		file = new File("");
		setFile(loc);
	}

	/**
	 * No-arg constructor. We'll show a dialog unconditionally when the filename
	 * is asked for.
	 */
	public FileChooser() {
		this(new File(""));
	}

	/**
	 * If no valid filename was passed in, show a dialog for the user to select
	 * one; return the filename passed in or the filename the user selected.
	 *
	 * @return the file the caller or the user chose
	 * @throws ChoiceInterruptedException
	 *             when the choice is interrupted or the user declines to choose
	 *             a file.
	 */
	public File getFile() throws ChoiceInterruptedException {
		if (!shouldReturn) {
			if (SwingUtilities.isEventDispatchThread()) {
				if (chooser.showOpenDialog(null) == APPROVE_OPTION) {
					setFile(assertNotNull(chooser.getSelectedFile()));
				}
			} else {
				final FilteredFileChooser fileChooser = chooser;
				invoke(new Runnable() {
					@Override
					public void run() {
						if (fileChooser.showOpenDialog(null) == APPROVE_OPTION) {
							setFile(NullCleaner
									.valueOrDefault(
											fileChooser.getSelectedFile(),
											new File("")));
						}
					}

				});
			}
		}
		if (file.exists()) {
			return file;
		} else {
			throw new ChoiceInterruptedException();
		}
	}

	/**
	 * invokeAndWait(), and throw a ChoiceInterruptedException if interrupted or
	 * otherwise failing.
	 *
	 * @param runnable
	 *            the runnable to run.
	 * @throws ChoiceInterruptedException
	 *             on error
	 */
	private static void invoke(final Runnable runnable)
			throws ChoiceInterruptedException {
		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (final InvocationTargetException except) {
			final Throwable cause = except.getCause();
			if (cause == null) {
				throw new ChoiceInterruptedException(except);
			} else {
				throw new ChoiceInterruptedException(cause); // NOPMD
			}
		} catch (final InterruptedException except) {
			throw new ChoiceInterruptedException(except);
		}
	}
	/**
	 * (Re-)set the file to return.
	 *
	 * @param loc
	 *            the file to return
	 */
	public final void setFile(final File loc) {
		if (loc.exists()) {
			file = loc;
			shouldReturn = true;
		} else {
			file = new File("");
			shouldReturn = false;
		}
	}

	/**
	 * An exception to throw when no selection was made or selection was
	 * interrupted by an exception.
	 *
	 * @author Jonathan Lovelace
	 */
	public static class ChoiceInterruptedException extends Exception {
		/**
		 * @param cause
		 *            an exception that we caught that interrupted the choice
		 */
		public ChoiceInterruptedException(final Throwable cause) {
			super("Choice of a file was interrupted by an exception:", cause);
		}

		/**
		 * No-arg constructor.
		 */
		public ChoiceInterruptedException() {
			super("No file was selected");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FileChooser";
	}
}
