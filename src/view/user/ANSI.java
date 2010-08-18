package view.user;

/**
 * A collection of ANSI codes to change text color.
 * 
 * @author some guy on the Internet (the code)
 * @author Jonathan Lovelace (the documentation)
 */
public final class ANSI {
	/**
	 * Switch back to normal text.
	 */
	public static final String SANE = "\u001B[0m";
	/**
	 * Switch to high-intensity (bold) text.
	 */
	public static final String HIGH_INTENSITY = "\u001B[1m";
	/**
	 * Switch to low-intensity text.
	 */
	public static final String LOW_INTESITY = "\u001B[2m";
	/**
	 * Italicize text
	 */
	public static final String ITALIC = "\u001B[3m";
	/**
	 * Underline text
	 */
	public static final String UNDERLINE = "\u001B[4m";
	/**
	 * Blink text.
	 */
	public static final String BLINK = "\u001B[5m";
	/**
	 * Blink text rapidly
	 */
	public static final String RAPID_BLINK = "\u001B[6m";
	/**
	 * Reverse-video.
	 */
	public static final String REVERSE_VIDEO = "\u001B[7m";
	/**
	 * Make text invisile.
	 */
	public static final String INVISIBLE_TEXT = "\u001B[8m";
	/**
	 * Make text black.
	 */
	public static final String BLACK = "\u001B[30m";
	/**
	 * Make text red.
	 */
	public static final String RED = "\u001B[31m";
	/**
	 * Make text green
	 */
	public static final String GREEN = "\u001B[32m";
	/**
	 * Make text yellow
	 */
	public static final String YELLOW = "\u001B[33m";
	/**
	 * Make text blue
	 */
	public static final String BLUE = "\u001B[34m";
	/**
	 * Make text magenta
	 */
	public static final String MAGENTA = "\u001B[35m";
	/**
	 * Make text cyan
	 */
	public static final String CYAN = "\u001B[36m";
	/**
	 * Make text white
	 */
	public static final String WHITE = "\u001B[37m";
	/**
	 * Make background black
	 */
	public static final String BACKGROUND_BLACK = "\u001B[40m";
	/**
	 * Make background red
	 */
	public static final String BACKGROUND_RED = "\u001B[41m";
	/**
	 * Make background green
	 */
	public static final String BACKGROUND_GREEN = "\u001B[42m";
	/**
	 * Make background yellow
	 */
	public static final String BACKGROUND_YELLOW = "\u001B[43m";
	/**
	 * Make background blue
	 */
	public static final String BACKGROUND_BLUE = "\u001B[44m";
	/**
	 * Make background magenta
	 */
	public static final String BACKGRD_MAGENTA = "\u001B[45m";
	/**
	 * Make background cyan
	 */
	public static final String BACKGROUND_CYAN = "\u001B[46m";
	/**
	 * Make background white
	 */
	public static final String BACKGROUND_WHITE = "\u001B[47m";

	/**
	 * Do not instantiate this class.
	 */
	private ANSI() {
		// Do nohing
	}
}
