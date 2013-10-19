package controller.map.misc;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import view.map.main.MapFileFilter;
import view.util.FilteredFileChooser;

/**
 * A class to hide the details of choosing a file from the caller.
 *
 * @author Jonathan Lovelace
 *
 */
public class FileChooser {
	/**
	 * The filename we'll return, or null.
	 */
	private String filename;
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
	 * @param file the filename to return.
	 */
	public FileChooser(final String file) {
		filename = "";
		setFilename(file);
	}

	/**
	 * No-arg constructor. We'll show a dialog unconditionally when the filename
	 * is asked for.
	 */
	public FileChooser() {
		this("");
	}

	/**
	 * If no valid filename was passed in, show a dialog for the user to select
	 * one; return the filename passed in or the filename the user selected.
	 *
	 * @return the file the caller or the user chose
	 * @throws ChoiceInterruptedException when the choice is interrupted or the
	 *         user declines to choose a file.
	 */
	public String getFilename() throws ChoiceInterruptedException {
		final FilteredFileChooser fileChooser = chooser;
		if (!shouldReturn) {
			if (SwingUtilities.isEventDispatchThread()) {
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					final String selFile = fileChooser.getSelectedFile().getPath();
					assert selFile != null;
					setFilename(selFile);
				}
			} else {
				invoke(new Runnable() {
					@Override
					public void run() {
						if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							final String selFile = fileChooser.getSelectedFile().getPath();
							assert selFile != null;
							setFilename(selFile);
						}
					}

				});
			}
		}
		if (filename.isEmpty()) {
			throw new ChoiceInterruptedException();
		} else {
			return filename;
		}
	}

	/**
	 * invokeAndWait(), and throw a ChoiceInterruptedException if interrupted or
	 * otherwise failing.
	 *
	 * @param runnable the runnable to run.
	 * @throws ChoiceInterruptedException on error
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
	 * (Re-)set the filename to return.
	 *
	 * @param file the filename to return
	 */
	public final void setFilename(final String file) {
		if (file.isEmpty()) {
			filename = "";
			shouldReturn = false;
		} else {
			filename = file;
			shouldReturn = true;
		}
	}

	/**
	 * An exception to throw when no selection was made or selection was
	 * interrupted by an exception.
	 */
	public static class ChoiceInterruptedException extends Exception {
		/**
		 * @param cause an exception that we caught that interrupted the choice
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
}
