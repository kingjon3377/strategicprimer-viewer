package view.map.main;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static javax.swing.KeyStroke.getKeyStroke;

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
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), "down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), "right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), "up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), "left");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), "up-right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0), "up-left");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), "down-right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), "down-left");
		actionMap.put("up", new UpListener(selListener, 1));
		actionMap.put("down", new DownListener(selListener, 1));
		actionMap.put("left", new LeftListener(selListener, 1));
		actionMap.put("right", new RightListener(selListener, 1));
		actionMap.put("up-right", new UpRightListener(selListener, 1));
		actionMap.put("up-left", new UpLeftListener(selListener, 1));
		actionMap.put("down-right", new DownRightListener(selListener, 1));
		actionMap.put("down-left", new DownLeftListener(selListener, 1));
		inputMap.put(getKeyStroke(KeyEvent.VK_UP, CTRL_DOWN_MASK), "ctrlUp");
		inputMap.put(getKeyStroke(KeyEvent.VK_DOWN, CTRL_DOWN_MASK), "ctrlDown");
		inputMap.put(getKeyStroke(KeyEvent.VK_RIGHT, CTRL_DOWN_MASK),
				"ctrlRight");
		inputMap.put(getKeyStroke(KeyEvent.VK_LEFT, CTRL_DOWN_MASK), "ctrlLeft");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD2, CTRL_DOWN_MASK),
				"ctrlDown");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD6, CTRL_DOWN_MASK),
				"ctrlRight");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD8, CTRL_DOWN_MASK),
				"ctrlUp");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD4, CTRL_DOWN_MASK),
				"ctrlLeft");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD9, CTRL_DOWN_MASK),
				"ctrl-up-right");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD7, CTRL_DOWN_MASK),
				"ctrl-up-left");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD3, CTRL_DOWN_MASK),
				"ctrl-down-right");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD1, CTRL_DOWN_MASK),
				"ctrl-down-left");
		actionMap.put("ctrlUp", new UpListener(selListener, 5));
		actionMap.put("ctrlDown", new DownListener(selListener, 5));
		actionMap.put("ctrlLeft", new LeftListener(selListener, 5));
		actionMap.put("ctrlRight", new RightListener(selListener, 5));
		actionMap.put("ctrl-up-right", new UpRightListener(selListener, 5));
		actionMap.put("ctrl-up-left", new UpLeftListener(selListener, 5));
		actionMap.put("ctrl-down-right", new DownRightListener(selListener, 5));
		actionMap.put("ctrl-down-left", new DownLeftListener(selListener, 5));
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
	 * @author Jonathan Lovelace
	 */
	private abstract static class AbstractDirListener extends AbstractAction {
		/**
		 * How many times to repeat the motion on each user action.
		 */
		private final int count;
		/**
		 * Do the actual motion.
		 */
		protected abstract void move();
		/**
		 * @param countNum how many times to move on each user action
		 */
		protected AbstractDirListener(final int countNum) {
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
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "AbstractDirListener";
		}
	}

	/**
	 * A listener to move the cursor up.
	 * @author Jonathan Lovelace
	 */
	// ESCA-JAVA0237:
	private static class UpListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected UpListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
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
	 * @author Jonathan Lovelace
	 */
	// ESCA-JAVA0237:
	private static class DownListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected DownListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
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
	 * @author Jonathan Lovelace
	 */
	// ESCA-JAVA0237:
	private static class LeftListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected LeftListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
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
	 * @author Jonathan Lovelace
	 */
	// ESCA-JAVA0237:
	private static class RightListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected RightListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			dsc.right();
		}
	}
	/**
	 * A listener to move the cursor up and right.
	 * @uathor Jonathan Lovelace
	 */
	private static class UpRightListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected UpRightListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			// TODO: Perhaps add proper support to DirectionSelectionChanger so we don't have to make two calls here
			dsc.up();
			dsc.right();
		}
	}
	/**
	 * A listener to move the cursor up and left.
	 * @uathor Jonathan Lovelace
	 */
	private static class UpLeftListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected UpLeftListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			// TODO: Perhaps add proper support to DirectionSelectionChanger so we don't have to make two calls here
			dsc.up();
			dsc.left();
		}
	}
	/**
	 * A listener to move the cursor down and right.
	 * @uathor Jonathan Lovelace
	 */
	private static class DownRightListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected DownRightListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			// TODO: Perhaps add proper support to DirectionSelectionChanger so we don't have to make two calls here
			dsc.down();
			dsc.right();
		}
	}
	/**
	 * A listener to move the cursor down and left.
	 * @uathor Jonathan Lovelace
	 */
	private static class DownLeftListener extends AbstractDirListener {
		/**
		 * The listener that handles the motion.
		 */
		private final DirectionSelectionChanger dsc;
		/**
		 * @param selListener the listener that handles the motion
		 * @param countNum how many times to move on each user action
		 */
		protected DownLeftListener(final DirectionSelectionChanger selListener,
				final int countNum) {
			super(countNum);
			dsc = selListener;
		}

		/**
		 * Do the motion.
		 */
		@Override
		protected void move() {
			// TODO: Perhaps add proper support to DirectionSelectionChanger so we don't have to make two calls here
			dsc.down();
			dsc.left();
		}
	}
}
