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
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import util.ActionWrapper;
import util.OnMac;

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
	 * A map from key-codes for arrow keys and the numeric keypad to Strings we'll use
	 * to represent them.
	 */
	private static final Map<Integer, String> ARROW_INPUTS = new HashMap<>();
	/**
	 * A map from Strings representing arrow-key key codes to the actions that should
	 * be mapped to them.
	 */
	private static final Map<String, Consumer<DirectionSelectionChanger>> ARROW_ACTIONS =
			new HashMap<>();
	/**
	 * A map from key-codes that are used, when modified with a platform-specific
	 * modifier, for "jumping," to the Strings we'll use to represent them.
	 */
	private static final Map<Integer, String> JUMP_INPUTS = new HashMap<>();
	/**
	 * A map from other key-codes to the Strings we'll use to represent them.
	 */
	private static final Map<Integer, String> INPUTS = new HashMap<>();

	static {
		final ObjIntConsumer<String> arrow =
				(str, value) -> ARROW_INPUTS.put(Integer.valueOf(value), str);
		arrow.accept("up", KeyEvent.VK_UP);
		arrow.accept("down", KeyEvent.VK_DOWN);
		arrow.accept("right", KeyEvent.VK_RIGHT);
		arrow.accept("left", KeyEvent.VK_LEFT);
		arrow.accept("down", KeyEvent.VK_KP_DOWN);
		arrow.accept("down", KeyEvent.VK_NUMPAD2);
		arrow.accept("right", KeyEvent.VK_KP_RIGHT);
		arrow.accept("right", KeyEvent.VK_NUMPAD6);
		arrow.accept("up", KeyEvent.VK_KP_UP);
		arrow.accept("up", KeyEvent.VK_NUMPAD8);
		arrow.accept("left", KeyEvent.VK_KP_LEFT);
		arrow.accept("left", KeyEvent.VK_NUMPAD4);
		arrow.accept("up-right",KeyEvent.VK_NUMPAD9);
		arrow.accept("up-left", KeyEvent.VK_NUMPAD7);
		arrow.accept("down-right", KeyEvent.VK_NUMPAD3);
		arrow.accept("down-left", KeyEvent.VK_NUMPAD1);

		ARROW_ACTIONS.put("up", DirectionSelectionChanger::up);
		ARROW_ACTIONS.put("down", DirectionSelectionChanger::down);
		ARROW_ACTIONS.put("left", DirectionSelectionChanger::left);
		ARROW_ACTIONS.put("right", DirectionSelectionChanger::right);
		ARROW_ACTIONS.put("up-right",
				join(DirectionSelectionChanger::up, DirectionSelectionChanger::right));
		ARROW_ACTIONS.put("up-left",
				join(DirectionSelectionChanger::up, DirectionSelectionChanger::left));
		ARROW_ACTIONS.put("down-right",
				join(DirectionSelectionChanger::down, DirectionSelectionChanger::right));
		ARROW_ACTIONS.put("down-left",
				join(DirectionSelectionChanger::down, DirectionSelectionChanger::left));

		final ObjIntConsumer<String> other =
				(str, value) -> INPUTS.put(Integer.valueOf(value), str);
		other.accept("home", KeyEvent.VK_HOME);
		other.accept("home", KeyEvent.VK_0);
		other.accept("home", KeyEvent.VK_NUMPAD0);
		other.accept("end", KeyEvent.VK_END);
		other.accept("end", KeyEvent.VK_NUMBER_SIGN);
		other.accept("dollar", KeyEvent.VK_DOLLAR);
		other.accept("caret", KeyEvent.VK_CIRCUMFLEX);
		other.accept("end", Character.getNumericValue('#'));
		other.accept("caret", Character.getNumericValue('^'));

		final ObjIntConsumer<String> jumps =
				(str, value) -> JUMP_INPUTS.put(Integer.valueOf(value), str);
		jumps.accept("ctrl-home", KeyEvent.VK_HOME);
		jumps.accept("ctrl-end", KeyEvent.VK_END);
		if (OnMac.SYSTEM_IS_MAC) {
			jumps.accept("home", KeyEvent.VK_UP);
			jumps.accept("home", KeyEvent.VK_KP_UP);
			jumps.accept("home", KeyEvent.VK_NUMPAD8);
			jumps.accept("end", KeyEvent.VK_DOWN);
			jumps.accept("end", KeyEvent.VK_KP_DOWN);
			jumps.accept("end", KeyEvent.VK_NUMPAD2);
			jumps.accept("caret", KeyEvent.VK_LEFT);
			jumps.accept("caret", KeyEvent.VK_KP_LEFT);
			jumps.accept("caret", KeyEvent.VK_NUMPAD4);
			jumps.accept("dollar", KeyEvent.VK_RIGHT);
			jumps.accept("dollar", KeyEvent.VK_KP_RIGHT);
			jumps.accept("dollar", KeyEvent.VK_NUMPAD6);
		}
	}

	/**
	 * @param first one method reference
	 * @param second a second such reference
	 * @return a reference combining the two
	 */
	private static <T> Consumer<T> join(final Consumer<T> first,
										final Consumer<T> second) {
		return first.andThen(second);
	}

	/**
	 * @param selListener the listener we're setting up
	 * @param consumer a method reference to that class
	 * @return an ActionListener that calls that method reference on that instance
	 */
	private static ActionListener wrap(final DirectionSelectionChanger selListener,
								  final Consumer<DirectionSelectionChanger> consumer) {
		return evt -> consumer.accept(selListener);
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
			fiveMask = InputEvent.CTRL_DOWN_MASK;
		}
		for (final Map.Entry<Integer, String> entry : ARROW_INPUTS.entrySet()) {
			inputMap.put(getKeyStroke(entry.getKey().intValue(), 0), entry.getValue());
			inputMap.put(getKeyStroke(entry.getKey().intValue(), fiveMask),
					"ctrl-" + entry.getValue());
		}
		for (final Map.Entry<String, Consumer<DirectionSelectionChanger>> entry :
				ARROW_ACTIONS.entrySet()) {
			actionMap.put(entry.getKey(),
					new DirectionListener(wrap(selListener, entry.getValue())));
			actionMap.put("ctrl-" + entry.getKey(),
					new DirectionListener(wrap(selListener, entry.getValue()), 5));
		}
		final int jumpModifier;
		if (OnMac.SYSTEM_IS_MAC) {
			jumpModifier = InputEvent.META_DOWN_MASK;
		} else {
			jumpModifier = InputEvent.CTRL_DOWN_MASK;
		}
		for (final Map.Entry<Integer, String> entry : JUMP_INPUTS.entrySet()) {
			inputMap.put(getKeyStroke(entry.getKey(), jumpModifier), entry.getValue());
		}
		for (final Map.Entry<Integer, String> entry : INPUTS.entrySet()) {
			inputMap.put(getKeyStroke(entry.getKey().intValue(), 0), entry.getValue());
		}
		inputMap.put(getKeyStroke(KeyEvent.VK_3, SHIFT_DOWN_MASK), "end");
		inputMap.put(getKeyStroke(KeyEvent.VK_6, SHIFT_DOWN_MASK), "caret");
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
