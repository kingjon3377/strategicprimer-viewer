package drivers.map_viewer;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;

import lovelace.util.Platform;

import org.javatuples.Pair;

import java.util.Arrays;

/**
 * A collection of code to help set up the map viewer to listen to the arrow
 * keys, the numeric keypad, and other motion keys.
 */
/* package */ final class ArrowListenerInitializer {
	private ArrowListenerInitializer() {
	}

	/**
	 * Key-codes for arrow keys and the numeric keypad to Strings we will use to represent them.
	 */
	private static final List<Pair<Integer, String>> ARROW_INPUTS = List.of(Pair.with(KeyEvent.VK_UP, "up"),
			Pair.with(KeyEvent.VK_DOWN, "down"), Pair.with(KeyEvent.VK_RIGHT, "right"),
			Pair.with(KeyEvent.VK_LEFT, "left"), Pair.with(KeyEvent.VK_KP_DOWN, "down"),
			Pair.with(KeyEvent.VK_NUMPAD2, "down"), Pair.with(KeyEvent.VK_KP_RIGHT, "right"),
			Pair.with(KeyEvent.VK_NUMPAD6, "right"), Pair.with(KeyEvent.VK_KP_UP, "up"),
			Pair.with(KeyEvent.VK_NUMPAD8, "up"), Pair.with(KeyEvent.VK_KP_LEFT, "left"),
			Pair.with(KeyEvent.VK_NUMPAD4, "left"), Pair.with(KeyEvent.VK_NUMPAD9, "up-right"),
			Pair.with(KeyEvent.VK_NUMPAD7, "up-left"), Pair.with(KeyEvent.VK_NUMPAD3, "down-right"),
			Pair.with(KeyEvent.VK_NUMPAD1, "down-left"));

	private static Runnable join(final Runnable first, final Runnable second) {
		return () -> {
			first.run();
			second.run();
		};
	}

	/**
	 * Key-codes that are used, when modified with a platgform-specific
	 * modifier, for "jumping," and the Strings we'll use to represent
	 * them.
	 */
	private static final List<Pair<Integer, String>> JUMP_INPUTS = List.of(Pair.with(KeyEvent.VK_HOME, "ctrl-home"),
			Pair.with(KeyEvent.VK_END, "ctrl-end"));

	/**
	 * Key-codes that are used, when modified with the appropriate
	 * modifier, for "jumping" only on the Mac platform, and the Strings
	 * we'll use to represent them.
	 */
	private static final List<Pair<Integer, String>> MAC_JUMP_INPUTS = List.of(Pair.with(KeyEvent.VK_UP, "home"),
			Pair.with(KeyEvent.VK_KP_UP, "home"), Pair.with(KeyEvent.VK_NUMPAD8, "home"),
			Pair.with(KeyEvent.VK_DOWN, "end"), Pair.with(KeyEvent.VK_KP_DOWN, "end"),
			Pair.with(KeyEvent.VK_NUMPAD2, "end"), Pair.with(KeyEvent.VK_LEFT, "caret"),
			Pair.with(KeyEvent.VK_KP_LEFT, "caret"), Pair.with(KeyEvent.VK_NUMPAD4, "caret"),
			Pair.with(KeyEvent.VK_RIGHT, "dollar"), Pair.with(KeyEvent.VK_KP_RIGHT, "dollar"),
			Pair.with(KeyEvent.VK_NUMPAD6, "dollar"));

	/**
	 * Other key-codes and the Strings we'll use to represent them
	 */
	private static final List<Pair<Integer, String>> OTHER_INPUTS = List.of(Pair.with(KeyEvent.VK_HOME, "home"),
			Pair.with(KeyEvent.VK_0, "home"), Pair.with(KeyEvent.VK_NUMPAD0, "home"), Pair.with(KeyEvent.VK_END, "end"),
			Pair.with(KeyEvent.VK_NUMBER_SIGN, "end"), Pair.with(KeyEvent.VK_DOLLAR, "dollar"),
			Pair.with(KeyEvent.VK_CIRCUMFLEX, "caret"), Pair.with((int) '#', "end"), Pair.with((int) '^', "caret"));

	protected static void repeatVoid(final Runnable func, final int times) {
		for (int i = 0; i < times; i++) {
			func.run();
		}
	}

	private static final class DirectionListener extends AbstractAction {
		@Serial
		private static final long serialVersionUID = 1L;
		private final Runnable action;
		private final int num;

		public DirectionListener(final Runnable action, final int num) {
			this.action = action;
			this.num = num;
		}

		public DirectionListener(final Runnable action) {
			this(action, 1);
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			repeatVoid(action, num); // TODO: inline that here?
		}

		@Serial
		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			throw new NotSerializableException("drivers.map_viewer.ArrowListenerInitializer.DirectionListener");
		}

		@Serial
		private void writeObject(final ObjectOutputStream out) throws IOException {
			throw new NotSerializableException("drivers.map_viewer.ArrowListenerInitializer.DirectionListener");
		}
	}

	@SuppressWarnings("MagicConstant")
	public static void setUpArrowListeners(final DirectionSelectionChanger selListener,
										   final InputMap inputMap, final ActionMap actionMap) {
		final int fiveMask =
				(Platform.SYSTEM_IS_MAC) ? InputEvent.ALT_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
		for (final Pair<Integer, String> pair : ARROW_INPUTS) {
			final int stroke = pair.getValue0();
			final String action = pair.getValue1();
			inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
			inputMap.put(KeyStroke.getKeyStroke(stroke, fiveMask), "ctrl-" + action);
		}

		/*
		  Strings representing arrow-key key codes and the actions
		  that should be mapped to them. Not inlined to avoid having to repeat the
		  type parameters.
		 */
		final List<Pair<String, Runnable>> arrowActions = Arrays.asList(
				Pair.with("up", selListener::up),
				Pair.with("down", selListener::down),
				Pair.with("left", selListener::left),
				Pair.with("right", selListener::right),
				Pair.with("up-right", join(selListener::up, selListener::right)),
				Pair.with("up-left", join(selListener::up, selListener::left)),
				Pair.with("down-right", join(selListener::down, selListener::right)),
				Pair.with("down-left", join(selListener::down, selListener::left)));

		for (final Pair<String, Runnable> pair : arrowActions) {
			final String action = pair.getValue0();
			final Runnable consumer = pair.getValue1();
			actionMap.put(action, new DirectionListener(consumer));
			actionMap.put("ctrl-" + action, new DirectionListener(consumer, 5));
		}

		final int jumpModifier = Platform.SHORTCUT_MASK;
		for (final Pair<Integer, String> pair : JUMP_INPUTS) {
			final int stroke = pair.getValue0();
			final String action = pair.getValue1();
			inputMap.put(KeyStroke.getKeyStroke(stroke, jumpModifier), action);
		}

		if (Platform.SYSTEM_IS_MAC) {
			for (final Pair<Integer, String> pair : MAC_JUMP_INPUTS) {
				final int stroke = pair.getValue0();
				final String action = pair.getValue1();
				inputMap.put(KeyStroke.getKeyStroke(stroke, jumpModifier), action);
			}
		}

		for (final Pair<Integer, String> pair : OTHER_INPUTS) {
			final int stroke = pair.getValue0();
			final String action = pair.getValue1();
			inputMap.put(KeyStroke.getKeyStroke(stroke, 0), action);
		}

		// TODO: make a list of "shift inputs"?
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.SHIFT_DOWN_MASK),
				"end");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.SHIFT_DOWN_MASK),
				"caret");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.SHIFT_DOWN_MASK),
				"dollar");

		actionMap.put("ctrl-home", new DirectionListener(join(selListener::jumpUp,
				selListener::jumpLeft)));
		actionMap.put("home", new DirectionListener(selListener::jumpUp));
		actionMap.put("ctrl-end", new DirectionListener(join(selListener::jumpDown,
				selListener::jumpRight)));
		actionMap.put("end", new DirectionListener(selListener::jumpDown));
		actionMap.put("caret", new DirectionListener(selListener::jumpLeft));
		actionMap.put("dollar", new DirectionListener(selListener::jumpRight));
	}
}
