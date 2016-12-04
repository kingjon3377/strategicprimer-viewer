package view.map.main;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ObjIntConsumer;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import util.ActionWrapper;
import util.OnMac;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * A class to handle setting up listeners for the arrow keys.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ArrowKeyListener {
	/**
	 * A map from key-codes to Strings we'll use to represent them.
	 */
	private static final Map<Integer, String> INPUTS = new HashMap<>();
	static {
		final ObjIntConsumer<String> add =
				(str, value) -> INPUTS.put(Integer.valueOf(value), str);
		add.accept("up", KeyEvent.VK_UP);
		add.accept("down", KeyEvent.VK_DOWN);
		add.accept("right", KeyEvent.VK_RIGHT);
		add.accept("left", KeyEvent.VK_LEFT);
		add.accept("down", KeyEvent.VK_KP_DOWN);
		add.accept("down", KeyEvent.VK_NUMPAD2);
		add.accept("right", KeyEvent.VK_KP_RIGHT);
		add.accept("right", KeyEvent.VK_NUMPAD6);
		add.accept("up", KeyEvent.VK_KP_UP);
		add.accept("up", KeyEvent.VK_NUMPAD8);
		add.accept("left", KeyEvent.VK_KP_LEFT);
		add.accept("left", KeyEvent.VK_NUMPAD4);
		add.accept("up-right",KeyEvent.VK_NUMPAD9);
		add.accept("up-left", KeyEvent.VK_NUMPAD7);
		add.accept("down-right", KeyEvent.VK_NUMPAD3);
		add.accept("down-left", KeyEvent.VK_NUMPAD1);
	}
	/**
	 * @param consumer a reference to a DirectionSelectionChanger method
	 * @return it wrapped in an ActionListener
	 */
	private static ActionListener wrap(final Runnable consumer) {
		return evt -> consumer.run();
	}
	/**
	 * @param first one reference to a DirectionSelectionChanger method
	 * @param second a second such reference
	 * @return an ActionListener that ignores its parameter and calls the first
	 * reference, then the second.
	 */
	private static ActionListener wrap(final Runnable first, final Runnable second) {
		return evt -> {
			first.run();
			second.run();
		};
	}
	/**
	 * Set up listeners.
	 *
	 * @param selListener The actual listener whose methods have to be attached.
	 * @param inputMap    An input map to set up the key bindings.
	 * @param actionMap   The action map we'll be putting the glue listeners into.
	 */
	public static void setUpListeners(final DirectionSelectionChanger selListener,
									  final InputMap inputMap,
									  final ActionMap actionMap) {
		final int fiveMask;
		if (OnMac.SYSTEM_IS_MAC) {
			fiveMask = InputEvent.ALT_DOWN_MASK;
		} else {
			fiveMask = CTRL_DOWN_MASK;
		}
		for (final Map.Entry<Integer, String> entry : INPUTS.entrySet()) {
			inputMap.put(getKeyStroke(entry.getKey().intValue(), 0), entry.getValue());
			inputMap.put(getKeyStroke(entry.getKey().intValue(), fiveMask),
					"ctrl-" + entry.getValue());
		}
		actionMap.put("up", new DirectionListener(wrap(selListener::up)));
		actionMap.put("down", new DirectionListener(wrap(selListener::down)));
		actionMap.put("left", new DirectionListener(wrap(selListener::left)));
		actionMap.put("right", new DirectionListener(wrap(selListener::right)));
		actionMap.put("up-right",
				new DirectionListener(wrap(selListener::up, selListener::right)));
		actionMap.put("up-left",
				new DirectionListener(wrap(selListener::up, selListener::left)));
		actionMap.put("down-right",
				new DirectionListener(wrap(selListener::down, selListener::right)));
		actionMap.put("down-left",
				new DirectionListener(wrap(selListener::down, selListener::left)));
		actionMap.put("ctrl-up", new DirectionListener(wrap(selListener::up), 5));
		actionMap.put("ctrl-down", new DirectionListener(wrap(selListener::down), 5));
		actionMap.put("ctrl-left", new DirectionListener(wrap(selListener::left), 5));
		actionMap.put("ctrl-right", new DirectionListener(wrap(selListener::right), 5));
		actionMap.put("ctrl-up-right",
				new DirectionListener(wrap(selListener::up, selListener::right), 5));
		actionMap.put("ctrl-up-left",
				new DirectionListener(wrap(selListener::up, selListener::right), 5));
		actionMap.put("ctrl-down-right",
				new DirectionListener(wrap(selListener::down, selListener::right), 5));
		actionMap.put("ctrl-down-left",
				new DirectionListener(wrap(selListener::down, selListener::left), 5));
		if (OnMac.SYSTEM_IS_MAC) {
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
		actionMap.put("ctrl-home",
				new DirectionListener(wrap(selListener::jumpUp, selListener::jumpLeft)));
		actionMap.put("home", new DirectionListener(wrap(selListener::jumpUp)));
		actionMap.put("ctrl-end", new DirectionListener(wrap(selListener::jumpDown,
				selListener::jumpRight)));
		actionMap.put("end", new DirectionListener(wrap(selListener::jumpDown)));
		actionMap.put("caret", new DirectionListener(wrap(selListener::jumpLeft)));
		actionMap.put("dollar", new DirectionListener(wrap(selListener::jumpRight)));
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
	@SuppressWarnings({"CloneableClassInSecureContext", "CloneableClassWithoutClone"})
	private static class DirectionListener extends ActionWrapper {
		/**
		 * @param action the wrapped action
		 * @param num    how many times to repeat it on each user action
		 */
		protected DirectionListener(final ActionListener action, final int num) {
			super(evt -> {
				for (int i = 0; i < num; i++) {
					action.actionPerformed(evt);
				}
			});
		}

		/**
		 * @param action the wrapped action
		 */
		protected DirectionListener(final ActionListener action) {
			this(action, 1);
		}

		/**
		 * Prevent serialization.
		 *
		 * @param out ignored
		 * @throws IOException always
		 */
		@SuppressWarnings({"unused", "static-method"})
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("Serialization is not allowed");
		}

		/**
		 * Prevent serialization
		 *
		 * @param in ignored
		 * @throws IOException            always
		 * @throws ClassNotFoundException never
		 */
		@SuppressWarnings({"unused", "static-method"})
		private void readObject(final ObjectInputStream in)
				throws IOException, ClassNotFoundException {
			throw new NotSerializableException("Serialization is not allowed");
		}
	}

}
