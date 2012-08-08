package view.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A class to handle Apply and Revert button presses.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ApplyButtonHandler implements ActionListener {
	/**
	 * Constructor.
	 * 
	 * @param app the Applyable that does the heavy lifting.
	 */
	public ApplyButtonHandler(final Applyable app) {
		obj = app;
	}

	/**
	 * The Applyable that does the heavy lifting.
	 */
	private final Applyable obj;

	/**
	 * Handle button presses.
	 * 
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("Apply".equals(evt.getActionCommand())) {
			obj.apply();
		} else if ("Revert".equals(evt.getActionCommand())) {
			obj.revert();
		}
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ApplyButtonHandler";
	}
}
