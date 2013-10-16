// $codepro.audit.disable com.instantiations.assist.eclipse.analysis.avoidInnerClasses
package view.map.main;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import org.eclipse.jdt.annotation.Nullable;

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
	public static void setUpListeners(
			final DirectionSelectionChanger selListener,
			final InputMap inputMap, final ActionMap actionMap) {
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		actionMap.put("up", new UpListener(selListener, 1));
		actionMap.put("down", new DownListener(selListener, 1));
		actionMap.put("left", new LeftListener(selListener, 1));
		actionMap.put("right", new RightListener(selListener, 1));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, CTRL_DOWN_MASK),
				"ctrlUp");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, CTRL_DOWN_MASK),
				"ctrlDown");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, CTRL_DOWN_MASK),
				"ctrlRight");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, CTRL_DOWN_MASK),
				"ctrlLeft");
		actionMap.put("ctrlUp", new UpListener(selListener, 5));
		actionMap.put("ctrlDown", new DownListener(selListener, 5));
		actionMap.put("ctrlLeft", new LeftListener(selListener, 5));
		actionMap.put("ctrlRight", new RightListener(selListener, 5));
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ArrowKeyListener";
	}

	/**
	 * A listener to move the cursor in a direction.
	 */
	private abstract static class AbstractDirListener extends AbstractAction {
		/**
		 * Do the actual motion.
		 */
		protected abstract void move();

		/**
		 * The listener that handles the motion.
		 */
		protected final DirectionSelectionChanger dsc;
		/**
		 * How many times to repeat the motion on each user action.
		 */
		private final int count;

		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		AbstractDirListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			dsc = selListener;
			count = countNum;
		}

		/**
		 * Handle user action.
		 *
		 * @param event the event to handle
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent event) {
			for (int i = 0; i < count; i++) {
				move();
			}
		}
	}

	/**
	 * A listener to move the cursor up.
	 */
	// ESCA-JAVA0237:
	private static class UpListener extends AbstractDirListener {
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		UpListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(selListener, countNum);
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			dsc.up();
		}
	}

	/**
	 * A listener to move the cursor down.
	 */
	// ESCA-JAVA0237:
	private static class DownListener extends AbstractDirListener {
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		DownListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(selListener, countNum);
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			dsc.down();
		}
	}

	/**
	 * A listener to move the cursor left.
	 */
	// ESCA-JAVA0237:
	private static class LeftListener extends AbstractDirListener {
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		LeftListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(selListener, countNum);
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			dsc.left();
		}
	}

	/**
	 * A listener to move the cursor right.
	 */
	// ESCA-JAVA0237:
	private static class RightListener extends AbstractDirListener {
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		RightListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(selListener, countNum);
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			dsc.right();
		}
	}
}
