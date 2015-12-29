package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * A class to handle setting up listeners for the arrow keys.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ArrowKeyListener {
	/**
	 * Whether this is running on a Mac.
	 */
	private static final boolean ON_MAC =
			System.getProperty("os.name").toLowerCase().startsWith("mac os x");

	/**
	 * Set up listeners.
	 *
	 * @param selListener The actual listener whose methods have to be attached.
	 * @param inputMap    An input map to set up the keybindings.
	 * @param actionMap   The action map we'll be putting the glue listeners into.
	 */
	public static void setUpListeners(
			                                 final DirectionSelectionChanger selListener,
			                                 final InputMap inputMap,
			                                 final ActionMap actionMap) {
		inputMap.put(getKeyStroke(KeyEvent.VK_UP, 0), "up");
		inputMap.put(getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		inputMap.put(getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		inputMap.put(getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "down");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD2, 0), "down");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "right");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD6, 0), "right");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_UP, 0), "up");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD8, 0), "up");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "left");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD4, 0), "left");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD9, 0), "up-right");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD7, 0), "up-left");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD3, 0), "down-right");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD1, 0), "down-left");
		actionMap.put("up", new DirectionListener(evt -> selListener.up()));
		actionMap.put("down", new DirectionListener(evt -> selListener.down()));
		actionMap.put("left", new DirectionListener(evt -> selListener.left()));
		actionMap.put("right", new DirectionListener(evt -> selListener.right()));
		actionMap.put("up-right", new DirectionListener(evt -> {
			selListener.up();
			selListener.right();
		}));
		actionMap.put("up-left", new DirectionListener(evt -> {
			selListener.up();
			selListener.left();
		}));
		actionMap.put("down-right", new DirectionListener(evt -> {
			selListener.down();
			selListener.right();
		}));
		actionMap.put("down-left", new DirectionListener(evt -> {
			selListener.down();
			selListener.left();
		}));
		final int fiveMask;
		if (ON_MAC) {
			fiveMask = InputEvent.ALT_DOWN_MASK;
		} else {
			fiveMask = CTRL_DOWN_MASK;
		}
		inputMap.put(getKeyStroke(KeyEvent.VK_UP, fiveMask), "ctrlUp");
		inputMap.put(getKeyStroke(KeyEvent.VK_DOWN, fiveMask), "ctrlDown");
		inputMap.put(getKeyStroke(KeyEvent.VK_RIGHT, fiveMask),
				"ctrlRight");
		inputMap.put(getKeyStroke(KeyEvent.VK_LEFT, fiveMask), "ctrlLeft");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_DOWN, fiveMask), "ctrlDown");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD2, fiveMask),
				"ctrlDown");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_RIGHT, fiveMask), "ctrlRight");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD6, fiveMask),
				"ctrlRight");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_UP, fiveMask), "ctrlUp");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD8, fiveMask),
				"ctrlUp");
		inputMap.put(getKeyStroke(KeyEvent.VK_KP_LEFT, fiveMask), "ctrlLeft");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD4, fiveMask),
				"ctrlLeft");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD9, fiveMask),
				"ctrl-up-right");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD7, fiveMask),
				"ctrl-up-left");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD3, fiveMask),
				"ctrl-down-right");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD1, fiveMask),
				"ctrl-down-left");
		actionMap.put("ctrlUp", new DirectionListener(evt -> selListener.up(), 5));
		actionMap.put("ctrlDown", new DirectionListener(evt -> selListener.down(), 5));
		actionMap.put("ctrlLeft", new DirectionListener(evt -> selListener.left(), 5));
		actionMap.put("ctrlRight", new DirectionListener(evt -> selListener.right(), 5));
		actionMap.put("ctrl-up-right", new DirectionListener(evt -> {
			selListener.up();
			selListener.right();
		}, 5));
		actionMap.put("ctrl-up-left", new DirectionListener(evt -> {
			selListener.up();
			selListener.left();
		}, 5));
		actionMap.put("ctrl-down-right", new DirectionListener(evt -> {
			selListener.down();
			selListener.right();
		}, 5));
		actionMap.put("ctrl-down-left", new DirectionListener(evt -> {
			selListener.down();
			selListener.left();
		}, 5));
		if (ON_MAC) {
			inputMap.put(getKeyStroke(KeyEvent.VK_HOME, InputEvent.META_DOWN_MASK),
					"ctrl-home");
			inputMap.put(getKeyStroke(KeyEvent.VK_END, InputEvent.META_DOWN_MASK),
					"ctrl-end");
			inputMap.put(getKeyStroke(KeyEvent.VK_UP, InputEvent.META_DOWN_MASK),
					"home");
			inputMap.put(getKeyStroke(KeyEvent.VK_KP_UP, InputEvent.META_DOWN_MASK),
					"home");
			inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD8, InputEvent.META_DOWN_MASK),
					"home");
			inputMap.put(getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_DOWN_MASK),
					"end");
			inputMap.put(getKeyStroke(KeyEvent.VK_KP_DOWN, InputEvent.META_DOWN_MASK),
					"end");
			inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD2, InputEvent.META_DOWN_MASK),
					"end");
			inputMap.put(getKeyStroke(KeyEvent.VK_LEFT, InputEvent.META_DOWN_MASK),
					"caret");
			inputMap.put(getKeyStroke(KeyEvent.VK_KP_LEFT, InputEvent.META_DOWN_MASK),
					"caret");
			inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD4, InputEvent.META_DOWN_MASK),
					"caret");
			inputMap.put(getKeyStroke(KeyEvent.VK_KP_RIGHT, InputEvent.META_DOWN_MASK),
					"dollar");
			inputMap.put(getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK),
					"dollar");
			inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD6, InputEvent.META_DOWN_MASK),
					"dollar");
		} else {
			inputMap.put(getKeyStroke(KeyEvent.VK_HOME, CTRL_DOWN_MASK), "ctrl-home");
			inputMap.put(getKeyStroke(KeyEvent.VK_END, CTRL_DOWN_MASK), "ctrl-end");
		}
		inputMap.put(getKeyStroke(KeyEvent.VK_HOME, 0), "home");
		inputMap.put(getKeyStroke(KeyEvent.VK_0, 0), "home");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "home");
		inputMap.put(getKeyStroke(KeyEvent.VK_END, 0), "end");
		inputMap.put(getKeyStroke(KeyEvent.VK_NUMBER_SIGN, 0), "end");
		inputMap.put(getKeyStroke(KeyEvent.VK_3, SHIFT_DOWN_MASK), "end");
		inputMap.put(getKeyStroke('#', 0), "end");
		inputMap.put(getKeyStroke(KeyEvent.VK_6, SHIFT_DOWN_MASK), "caret");
		inputMap.put(getKeyStroke('^', 0), "caret");
		inputMap.put(getKeyStroke(KeyEvent.VK_DOLLAR, 0), "dollar");
		inputMap.put(getKeyStroke(KeyEvent.VK_4, SHIFT_DOWN_MASK), "dollar");
		actionMap.put("ctrl-home", new DirectionListener(evt -> {
			selListener.jumpUp();
			selListener.jumpLeft();
		}));
		actionMap.put("home", new DirectionListener(evt -> selListener.jumpUp()));
		actionMap.put("ctrl-end", new DirectionListener(evt -> {
			selListener.jumpDown();
			selListener.jumpRight();
		}));
		actionMap.put("end", new DirectionListener(evt -> selListener.jumpDown()));
		actionMap.put("caret", new DirectionListener(evt -> selListener.jumpLeft()));
		actionMap.put("dollar", new DirectionListener(evt -> selListener.jumpRight()));
	}

	/**
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ArrowKeyListener";
	}
	/**
	 * A listener to move the cursor in a direction. Wraps an ActionListener.
	 */
	private static class DirectionListener extends AbstractAction {
		/**
		 * The wrapped action.
		 */
		private final ActionListener wrapped;
		/**
		 * How many times to repeat the action on each user action.
		 */
		private final int count;
		/**
		 * @param action the wrapped action
		 * @param num how many times to repeat it on each user action
		 */
		protected DirectionListener(final ActionListener action, final int num) {
			wrapped = action;
			count = num;
		}
		/**
		 * @param action the wrapped action
		 */
		protected DirectionListener(final ActionListener action) {
			this(action, 1);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			for (int i = 0; i < count; i++) {
				wrapped.actionPerformed(e);
			}
		}
	}

}
