package lovelace.util;

import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.JFileChooser;
import java.io.File;
import java.awt.FileDialog;
import java.awt.Component;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.ToIntFunction;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.function.Consumer;
import either.Either;

/**
 * A wrapper around the {@link JFileChooser Swing} and {@link FileDialog AWT} file-choosers.
 *
 * On most platforms, {@link JFileChooser the Swing JFileChooser} is close
 * enough to the native widget in appearance and functionality; on macOS, it is
 * decidedly <em>not</em>, and it's impossible to conform to the platform HIG
 * without using {@link FIleDialog the AWT FileDialog} class instead. This
 * class leaves the choice of which one to use to its callers, but abstracts
 * over the differences between them.
 */
public class FileChooser {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FileChooser.class.getName());

	/**
	 * An exception to throw when the user cancels the file-chooser.
	 */
	public static class ChoiceInterruptedException extends Exception {
		private static final long serialVersionUID = 1L;
		public ChoiceInterruptedException() {
			super("No file was selected");
		}

		public ChoiceInterruptedException(@Nullable final Throwable cause) {
			super(cause == null ? "No file was selected" :
				"Choice of a file was interrupted by an exception:", cause);
		}
	}

	/**
	 * Convert the type returned by the file-chooser to the type we expose in return types.
	 */
	private static Path fileToPath(final File file) {
		return file.toPath();
	}

	/**
	 * The method to call to show the caller's chosen dialog.
	 */
	private final ToIntFunction<@Nullable Component> chooserFunction;

	/**
	 * The file(s) either passed in to the constructor or chosen by the user.
	 */
	private List<Path> storedFile;

	/**
	 * The file-chooser widget that will actually ask the user to choose a file or files.
	 */
	private final Either<JFileChooser, FileDialog> chooser;

	public enum ChooserMode {
		/**
		 * "Open" dialogs allow the user to choose multiple files, but
		 * only existing files.
		 */
		Open,
		/**
		 * "Save" dialogs only allow the user to choose existing files,
		 * but (when they work properly) allow the user to create a new file.
		 */
		Save,
		/**
		 * A "custom" dialog allows the caller to provide a custom verb
		 * (to replace "open" or "save"), but otherwise acts like a
		 * "Save" dialog. This feature only actually works in Swing, so
		 * on AWT we fall back to the Save dialog.
		 */
		Custom
	}

	// We don't provide a constructor taking FileDialog and approveText, as it's (unfortunately)
	// not possible to use a "custom" action with the AWT interface.
	protected FileChooser(final ChooserMode mode, final JFileChooser fileChooser, final String approveText,
	                      @Nullable final Path loc) {
		switch (mode) {
		case Open: case Save:
			throw new IllegalArgumentException("Approve text only supported for custom dialog");
		case Custom:
			LOGGER.fine("FileChooser invoked for a custom dialog with Swing JFileChooser");
			chooser = Either.left(fileChooser);
			chooserFunction = (component) -> fileChooser.showDialog(component, approveText);
			if (loc == null) {
				LOGGER.fine("No file was passed in");
				storedFile = Collections.emptyList();
			} else {
				LOGGER.fine("A file was passed in");
				storedFile = Collections.singletonList(loc);
			}
			break;
		default:
			throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	protected FileChooser(final ChooserMode mode, final JFileChooser fileChooser, @Nullable final Path loc) {
		switch (mode) {
		case Open:
			LOGGER.fine("FileChooser invoked for the Open dialog using Swing JFileChooser");
			chooser = Either.left(fileChooser);
			chooserFunction = fileChooser::showOpenDialog;
			fileChooser.setMultiSelectionEnabled(true);
			if (loc == null) {
				LOGGER.fine("No file was passed in");
				storedFile = Collections.emptyList();
			} else {
				LOGGER.fine("A file was passed in");
				storedFile = Collections.singletonList(loc);
			}
			break;
		case Save:
			LOGGER.fine("FileChooser invoked for Save dialog using Swing JFileChooser");
			chooserFunction = fileChooser::showSaveDialog;
			chooser = Either.left(fileChooser);
			if (loc == null) {
				LOGGER.fine("No file was passed in");
				storedFile = Collections.emptyList();
			} else {
				LOGGER.fine("A file was passed in");
				storedFile = Collections.singletonList(loc);
			}
			break;
		case Custom:
			throw new IllegalArgumentException("Approval text required for custom dialog");
		default:
			throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	protected FileChooser(final ChooserMode mode, final FileDialog fileChooser, @Nullable final Path loc) {
		switch (mode) {
		case Open:
			LOGGER.fine("FileChooser invoked for the Open dialog using AWT FileDialog");
			fileChooser.setMode(FileDialog.LOAD);
			chooser = Either.right(fileChooser);
			chooserFunction = (component) -> {
				fileChooser.setVisible(true);
				return 0;
			};
			fileChooser.setMultipleMode(true);
			if (loc == null) {
				LOGGER.fine("No file was passed in");
				storedFile = Collections.emptyList();
			} else {
				LOGGER.fine("A file was passed in");
				storedFile = Collections.singletonList(loc);
			}
			break;
		case Save:
			LOGGER.fine("FileChooser invoked for Save dialog using AWT FileDialog");
			fileChooser.setMode(FileDialog.SAVE);
			chooser = Either.right(fileChooser);
			chooserFunction = (component) -> {
				fileChooser.setVisible(true);
				return 0;
			};
			if (loc == null) {
				LOGGER.fine("No file was passed in");
				storedFile = Collections.emptyList();
			} else {
				LOGGER.fine("A file was passed in");
				storedFile = Collections.singletonList(loc);
			}
			break;
		case Custom:
			throw new IllegalArgumentException("Custom dialog not supported in AWT");
		default:
			throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	public static FileChooser open(final JFileChooser fileChooser, @Nullable final Path loc) {
		return new FileChooser(ChooserMode.Open, fileChooser, loc);
	}

	public static FileChooser open(final FileDialog fileChooser, @Nullable final Path loc) {
		return new FileChooser(ChooserMode.Open, fileChooser, loc);
	}

	public static FileChooser open(final JFileChooser fileChooser) {
		return new FileChooser(ChooserMode.Open, fileChooser, null);
	}

	public static FileChooser open(final FileDialog fileChooser) {
		return new FileChooser(ChooserMode.Open, fileChooser, null);
	}

	public static FileChooser save(final JFileChooser fileChooser, @Nullable final Path loc) {
		return new FileChooser(ChooserMode.Save, fileChooser, loc);
	}

	public static FileChooser save(final FileDialog fileChooser, @Nullable final Path loc) {
		return new FileChooser(ChooserMode.Save, fileChooser, loc);
	}

	public static FileChooser save(final JFileChooser fileChooser) {
		return new FileChooser(ChooserMode.Save, fileChooser, null);
	}

	public static FileChooser save(final FileDialog fileChooser) {
		return new FileChooser(ChooserMode.Save, fileChooser, null);
	}

	public static FileChooser custom(final JFileChooser fileChooser, final String approveText, @Nullable final Path loc) {
		return new FileChooser(ChooserMode.Custom, fileChooser, approveText, loc);
	}

	public static FileChooser custom(final JFileChooser fileChooser, final String approveText) {
		return new FileChooser(ChooserMode.Custom, fileChooser, approveText, null);
	}

	/**
	 * A helper method to run a function on the EDT, without leaking any of
	 * the implementation-detail exceptions that are commonly thrown to the
	 * caller.
	 */
	private static void invoke(final Runnable runnable) throws ChoiceInterruptedException {
		try {
			LOGGER.fine("FileChooser.invoke(): About to invoke the provided function");
			SwingUtilities.invokeAndWait(runnable);
		} catch (final InvocationTargetException except) {
			throw new ChoiceInterruptedException(
				Optional.ofNullable(except.getCause()).orElse(except));
		} catch (final InterruptedException except) {
			throw new ChoiceInterruptedException(except);
		}
	}

	/**
	 * Show the dialog to the user and update {@link storedFile} with his or her choice(s).
	 */
	void haveUserChooseFiles() {
		LOGGER.fine("In FileChooser.haveUserChooseFiles");
		final int status = chooserFunction.applyAsInt(null);
		LOGGER.fine("FileChooser: The AWT or Swing chooser returned");
		if (chooser.fromLeft().isPresent()) {
			final JFileChooser fc = chooser.fromLeft().get();
			if (JFileChooser.APPROVE_OPTION == status) {
				final List<Path> retval = Stream.of(fc.getSelectedFiles()).filter(Objects::nonNull)
					.map(File::toPath).collect(Collectors.toList());
				if (!retval.isEmpty()) {
					LOGGER.fine("Saving the file(s) the user chose via Swing");
					storedFile = retval;
				} else if (fc.getSelectedFile() != null) {
					final File selectedFile = fc.getSelectedFile();
					LOGGER.fine("Saving the singular file the user chose via Swing");
					storedFile = Collections.singletonList(selectedFile.toPath());
				} else {
					LOGGER.info("User pressed approve but selected no files");
					storedFile = Collections.emptyList();
				}
			} else {
				LOGGER.info("Chooser function returned " + status);
			}
		} else {
			final FileDialog fd = chooser.fromRight().get();
			final List<Path> retval = Stream.of(fd.getFiles()).filter(Objects::nonNull)
				.map(File::toPath).collect(Collectors.toList());
			if (!retval.isEmpty()) {
				LOGGER.fine("Saving the file(s) the user chose via AWT");
				storedFile = retval;
			} else if (fd.getFile() != null) {
				final String selectedFile = fd.getFile();
				LOGGER.fine("Saving the singular file the user chose via AWT");
				storedFile = Collections.singletonList(Paths.get(selectedFile));
			} else {
				LOGGER.info("User failed to choose?");
				LOGGER.fine(String.format("Returned iterable was %s (%s)", retval,
					retval.getClass()));
				storedFile = Collections.emptyList();
			}
		}
	}

	/**
	 * If a valid filename was, or multiple filenames were, passed in to
	 * the constructor, return an iterable containing it or them;
	 * otherwise, show a dialog for the user to select one or more
	 * filenames and return the filename(s) the user selected. Throws an
	 * exception if the choice is interrupted or the user declines to
	 * choose.
	 *
	 * TODO: Just return the empty list when user declines to choose (when
	 * we didn't catch an exception interrupting the choice)?
	 */
	public List<Path> getFiles() throws ChoiceInterruptedException {
		if (!storedFile.isEmpty()) {
			LOGGER.fine("FileChooser.files: A file was stored, so returning it");
			return storedFile;
		} else if (SwingUtilities.isEventDispatchThread()) {
			LOGGER.fine("FileChooser.files: Have to ask the user; on EDT");
			haveUserChooseFiles();
		} else {
			LOGGER.fine("FileChooser.files: Have to ask the user; not yet on EDT");
			invoke(this::haveUserChooseFiles);
		}
		if (storedFile.isEmpty()) {
			throw new ChoiceInterruptedException();
		} else {
			return storedFile;
		}
	}

	/**
	 * Set the stored file(s) to the given Iterable.
	 */
	public void setFiles(final Collection<Path> files) {
		storedFile = new ArrayList<>(files);
	}

	/**
	 * Allow the user to choose a file or files, if necessary, and pass
	 * each file to the given consumer. If the operation is canceled, do
	 * nothing.
	 */
	public void call(final Consumer<Path> consumer) {
		try {
			getFiles().forEach(consumer);
		} catch (final ChoiceInterruptedException exception) {
			LOGGER.log(Level.INFO, "Choice interrupted or user failed to choose", exception);
		}
	}
}
