// $codepro.audit.disable com.instantiations.assist.eclipse.analysis.avoidInnerClasses
package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 * A class to handle setting up listeners for the arrow keys.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ArrowKeyListener {
	/**
	 * Set up listeners.
	 * 
	 * @param selListener The actual listener whose methods have to be attached.
	 * @param inputMap An input map to set up the keybindings.
	 * @param actionMap The action map we'll be putting the glue listeners into.
	 */
	public void setUpListeners(final DirectionSelectionChanger selListener,
			final InputMap inputMap, final ActionMap actionMap) {
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
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

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ArrowKeyListener";
	}

}
