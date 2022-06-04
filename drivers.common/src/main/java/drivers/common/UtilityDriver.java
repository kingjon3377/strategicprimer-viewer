package drivers.common;

/**
 * An interface to allow utility drivers, which operate on files rather than a
 * map model, to be a single-abstract-method interface. (Except that that's not actually true,
 * given the method on the {@link ISPDriver} interface.)
 */
public interface UtilityDriver extends ISPDriver {
	/**
	 * Run the driver. If the driver is a GUI driver, this should use
	 * {@link javax.swing.SwingUtilities#invokeLater}; if it's a CLI driver, that's not
	 * necessary.
	 *
	 * @param args Any command-line arguments, such as filenames, that
	 * should be passed to the driver. This will not include options.
	 */
	void startDriver(String... args) throws DriverFailedException;
}
