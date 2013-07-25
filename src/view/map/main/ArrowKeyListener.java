// $codepro.audit.disable com.instantiations.assist.eclipse.analysis.avoidInnerClasses
package view.map.main;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

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
	public static void setUpListeners(final DirectionSelectionChanger selListener,
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
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, CTRL_DOWN_MASK), "ctrlUp");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, CTRL_DOWN_MASK), "ctrlDown");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, CTRL_DOWN_MASK), "ctrlRight");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, CTRL_DOWN_MASK), "ctrlLeft");
		actionMap.put("ctrlUp", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.up();
				selListener.up();
				selListener.up();
				selListener.up();
				selListener.up();
			}
		});
		actionMap.put("ctrlDown", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.down();
				selListener.down();
				selListener.down();
				selListener.down();
				selListener.down();
			}
		});
		actionMap.put("ctrlLeft", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.left();
				selListener.left();
				selListener.left();
				selListener.left();
				selListener.left();
			}
		});
		actionMap.put("ctrlRight", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.right();
				selListener.right();
				selListener.right();
				selListener.right();
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
