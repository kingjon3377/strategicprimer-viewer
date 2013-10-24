package controller.map.drivers;

import model.map.HasName;

/**
 * An interface for drivers, so one main() method can start different ones
 * depending on options.
 *
 * @author Jonathan Lovelace
 *
 */
public interface ISPDriver extends HasName {
	/**
	 * Run the driver. If the driver is a GUIDriver, this should use
	 * SwingUtilities.invokeLater(); if it's a CLIDriver, that's not necessary.
	 *
	 * @param args any command-line arguments that should be passed to the
	 *        driver.
	 * @throws DriverFailedException if it's impossible for the driver to start.
	 */
	void startDriver(final String... args) throws DriverFailedException;

	/**
	 * An exception to throw when the driver fails ... such as if the map is
	 * improperly formatted, etc. This means we don't have to declare a long
	 * list of possible exceptional circumstances.
	 * @author Jonathan Lovelace
	 */
	class DriverFailedException extends Exception { // $codepro.audit.disable
		/**
		 * Constructor.
		 *
		 * @param cause the exception we're wrapping. Should *not* be null.
		 */
		public DriverFailedException(final Throwable cause) {
			super("The driver could not start because of an exception:", cause);
		}

		/**
		 * Constructor.
		 *
		 * @param string a custom error string
		 * @param cause the cause. Should not be null.
		 */
		public DriverFailedException(final String string, final Throwable cause) {
			super(string, cause);
		}
	}

	/**
	 * @return an object indicating how to use and invoke the driver.
	 */
	DriverUsage usage();

	/**
	 * A class to represent usage information for drivers, for use in the
	 * AppStarter and in help text.
	 *
	 * @author Jonathan Lovelace
	 */
	class DriverUsage { // $codepro.audit.disable
		/**
		 * Possible numbers of (non-option?) parameters a driver might want.
		 */
		public static enum ParamCount {
			/**
			 * None at all.
			 */
			None,
			/**
			 * One.
			 */
			One,
			/**
			 * At least two.
			 */
			Many;
		}

		/**
		 * Constructor.
		 *
		 * @param graph whether this driver is graphical or not
		 * @param shortOpt the short (generally one character) option to give to
		 *        AppStarter to get this driver
		 * @param longOpt the long option to give to AppStarter to get this
		 *        driver
		 * @param params how many parameters the driver wants
		 * @param shortDesc a short description of the driver
		 * @param longDesc a longer description of the driver.
		 * @param driver the Class object referring to the type of driver this
		 *        describes
		 */
		// ESCA-JAVA0138:
		public DriverUsage(final boolean graph, final String shortOpt,
				final String longOpt, final ParamCount params,
				final String shortDesc, final String longDesc,
				final Class<? extends ISPDriver> driver) {
			graphical = graph;
			shortOption = shortOpt;
			longOption = longOpt;
			paramsWanted = params;
			shortDescription = shortDesc;
			longDescription = longDesc;
			driverClass = driver;
		}

		/**
		 * Whether the driver is graphical or not.
		 */
		private final boolean graphical;
		/**
		 * The short option to give to AppStarter to get this driver.
		 */
		private final String shortOption;
		/**
		 * The long option to give to AppStarter to get this driver.
		 */
		private final String longOption;
		/**
		 * How many parameters this driver wants.
		 */
		private final ParamCount paramsWanted;
		/**
		 * A short description of the driver.
		 */
		private final String shortDescription;
		/**
		 * A longer description of the driver.
		 */
		private final String longDescription;
		/**
		 * The type of the driver this describes.
		 */
		private final Class<? extends ISPDriver> driverClass;

		/**
		 * @return whether the driver is graphical or not.
		 */
		public boolean isGraphical() {
			return graphical;
		}

		/**
		 * @return the short option to give to AppStarter to get this driver
		 */
		public String getShortOption() {
			return shortOption;
		}

		/**
		 * @return the long option to give to AppStarter to get this driver
		 */
		public String getLongOption() {
			return longOption;
		}

		/**
		 * @return how many parameters this driver wants
		 */
		public ParamCount getParamsWanted() {
			return paramsWanted;
		}

		/**
		 * @return a short (one-line) description of the driver.
		 */
		public String getShortDescription() {
			return shortDescription;
		}

		/**
		 * @return a long(er) description of the driver.
		 */
		public String getLongDescription() {
			return longDescription;
		}

		/**
		 * @return the type this driver describes.
		 */
		public Class<? extends ISPDriver> getDriverClass() {
			return driverClass;
		}
	}
}
