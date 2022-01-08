package drivers.gui.common;

import java.nio.file.Path;
import org.jetbrains.annotations.Nullable;
import javax.swing.JFileChooser;
import lovelace.util.FileChooser;
import lovelace.util.Platform;
import lovelace.util.Either;

import java.awt.FileDialog;
import java.awt.Frame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FilenameFilter;
import java.io.File;

/**
 * An extension of the more-generic {@link FileChooser} class that, by default,
 * filters the view to show only files that (by their extension) might be SP
 * map files, and that chooses between {@link JFileChooser Swing} and {@link
 * FileDialog AWT} based on what platform the code is running on.
 */
public class SPFileChooser extends FileChooser {
	private static final FileFilter MAP_EXTENSIONS_FILTER = new FileNameExtensionFilter(
		"Strategic Primer world map files", "map", "xml", "db");

	/**
	 * A factory method for {@link JFileChooser} (or AWT {@link
	 * FileDialog}) taking a {@link FileFilter} to apply in the same operation.
	 *
	 * TODO: Move functionality into FileChooser somehow? Or else convert to class.
	 *
	 * @param allowMultiple Whether to allow multi-selection.
	 */
	public static Either<JFileChooser, FileDialog> filteredFileChooser(boolean allowMultiple) {
		return filteredFileChooser(allowMultiple, ".");
	}

	/**
	 * A factory method for {@link JFileChooser} (or AWT {@link
	 * FileDialog}) taking a {@link FileFilter} to apply in the same operation.
	 *
	 * TODO: Move functionality into FileChooser somehow? Or else convert to class.
	 *
	 * @param allowMultiple Whether to allow multi-selection.
	 * @param current The current directory. TODO: Take as Path instead?
	 */
	public static Either<JFileChooser, FileDialog> filteredFileChooser(boolean allowMultiple,
			String current) {
		return filteredFileChooser(allowMultiple, current, MAP_EXTENSIONS_FILTER);
	}

	/**
	 * A factory method for {@link JFileChooser} (or AWT {@link
	 * FileDialog}) taking a {@link FileFilter} to apply in the same operation.
	 *
	 * TODO: Move functionality into FileChooser somehow?
	 *
	 * @param allowMultiple Whether to allow multi-selection.
	 * @param current The current directory. TODO: Take as Path instead?
	 * @param filter The filter to apply, if any.
	 */
	public static Either<JFileChooser, FileDialog> filteredFileChooser(boolean allowMultiple,
			String current, @Nullable FileFilter filter) {
		if (Platform.SYSTEM_IS_MAC) {
			FileDialog retval = new FileDialog((Frame) null);
			if (filter != null) {
				retval.setFilenameFilter((dir, name) -> filter.accept(new File(dir, name)));
			}
			return Either.right(retval);
		} else {
			JFileChooser retval = new JFileChooser(current);
			if (filter != null) {
				retval.setFileFilter(filter);
			}
			return Either.left(retval);
		}
	}

	protected SPFileChooser(ChooserMode mode, JFileChooser fileChooser, String approveText,
			@Nullable Path loc) {
		super(mode, fileChooser, approveText, loc);
	}

	protected SPFileChooser(ChooserMode mode, JFileChooser fileChooser, @Nullable Path loc) {
		super(mode, fileChooser, loc);
	}

	protected SPFileChooser(ChooserMode mode, FileDialog fileChooser, @Nullable Path loc) {
		super(mode, fileChooser, loc);
	}

	/**
	 * Constructor for a filtered "Open" dialog.
	 */
	public static SPFileChooser open(@Nullable Path loc, JFileChooser fileChooser) {
		return new SPFileChooser(ChooserMode.Open, fileChooser, loc);
	}

	/**
	 * Constructor for a filtered "Open" dialog.
	 */
	public static SPFileChooser open(@Nullable Path loc, FileDialog fileChooser) {
		return new SPFileChooser(ChooserMode.Open, fileChooser, loc);
	}

	/**
	 * Constructor for a filtered "Open" dialog.
	 */
	public static SPFileChooser open(@Nullable Path loc) {
		Either<JFileChooser, FileDialog> fc = filteredFileChooser(true);
		if (fc.fromLeft().isPresent()) {
			return open(loc, fc.fromLeft().get());
		} else {
			return open(loc, fc.fromRight().get());
		}
	}

	/**
	 * Constructor for a filtered "Open" dialog.
	 */
	public static SPFileChooser open() {
		return open((Path) null);
	}

	/**
	 * Constructor for a filtered "Save" dialog.
	 */
	public static SPFileChooser save(@Nullable Path loc, JFileChooser fileChooser) {
		return new SPFileChooser(ChooserMode.Save, fileChooser, loc);
	}

	/**
	 * Constructor for a filtered "Save" dialog.
	 */
	public static SPFileChooser save(@Nullable Path loc, FileDialog fileChooser) {
		return new SPFileChooser(ChooserMode.Save, fileChooser, loc);
	}

	/**
	 * Constructor for a filtered "Save" dialog.
	 */
	public static SPFileChooser save(@Nullable Path loc, Either<JFileChooser, FileDialog> fileChooser) {
		if (fileChooser.fromLeft().isPresent()) {
			return save(loc, fileChooser.fromLeft().get());
		} else {
			return save(loc, fileChooser.fromRight().get());
		}
	}

	/**
	 * Constructor for a filtered "Save" dialog.
	 */
	public static SPFileChooser save(@Nullable Path loc) {
		return save(loc, filteredFileChooser(true));
	}

	/**
	 * Constructor for a filtered custom-verb dialog.
	 */
	public static SPFileChooser custom(@Nullable Path loc, String approveText,
			JFileChooser fileChooser) {
		return new SPFileChooser(ChooserMode.Custom, fileChooser, approveText, loc);
	}

	/**
	 * Constructor for a filtered custom-verb dialog. For the convenience
	 * of callers, on platforms where AWT is more style-guide-compliant, we
	 * return a standard save dialog instead of failing (since AWT doesn't
	 * support custom-verb dialogs).
	 */
	public static SPFileChooser custom(@Nullable Path loc, String approveText) {
		Either<JFileChooser, FileDialog> fc = filteredFileChooser(true);
		if (fc.fromLeft().isPresent()) {
			return custom(loc, approveText, fc.fromLeft().get());
		} else {
			return save(loc, fc.fromRight().get());
		}
	}
}
