package view.map.main;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;

/**
 * A class to handle setting up listeners for the arrow keys.
 * @author Jonathan Lovelace
 *
 */
public class ArrowKeyListener {
	/**
	 * Set up listeners.
	 * @param selListener The actual listener whose methods have to be attached.
	 * @param actionMap The action map we'll be putting the glue listeners into.
	 */
	public void setUpListeners(final DirectionSelectionChanger selListener,
			final ActionMap actionMap) {
		actionMap.put("up", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.up();
			}
		});
		actionMap.put("down", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.down();
			}
		});
		actionMap.put("left", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.left();
			}
		});
		actionMap.put("right", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.right();
			}
		});
	}
}
