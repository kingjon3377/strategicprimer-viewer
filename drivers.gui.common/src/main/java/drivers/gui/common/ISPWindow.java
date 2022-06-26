package drivers.gui.common;

/**
 * An interface for top-level windows in assistive programs.
 */
public interface ISPWindow {
	/**
	 * The name of this window. This method should <em>not</em> return a
	 * string including the loaded file, since it is used only in the About
	 * dialog to "personalize" it for the particular app.
	 */
	String getWindowName();
}
