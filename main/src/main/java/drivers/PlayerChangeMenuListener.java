package drivers;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import drivers.common.IDriverModel;
import drivers.common.PlayerChangeListener;
import drivers.common.IWorkerModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import drivers.exploration.PlayerChangeSource;
import java.util.List;
import java.util.ArrayList;
import java.awt.Frame;
import java.awt.Component;
import common.map.Player;

/**
 * A class to respond to "change current player" menu items.
 */
public class PlayerChangeMenuListener implements ActionListener, PlayerChangeSource {
	public PlayerChangeMenuListener(final IDriverModel model) {
		this.model = model;
	}

	private final IDriverModel model;

	private final List<PlayerChangeListener> listeners = new ArrayList<>();

	@Override
	public void addPlayerChangeListener(final PlayerChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removePlayerChangeListener(final PlayerChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Get the window containing the given component. This goes beyond
	 * {@link lovelace.util:ComponentParentStream} in that we get the
	 * invoker of any {@link JPopupMenu pop-up menu}, and we also throw
	 * away the results of the intermediate steps.
	 */
	private static @Nullable Frame getContainingFrame(final @Nullable Component component) {
		Component temp = component;
		while (temp != null) {
			if (temp instanceof Frame f) {
				return f;
			} else if (temp instanceof JPopupMenu menu) {
				temp = menu.getInvoker();
			} else {
				temp = temp.getParent();
			}
		}
		return null;
	}

	/**
	 * Handle the event caused by the player choosing the menu item: show a
	 * dialog asking the user to choose the new current player. Once the
	 * user has done so, notify all listeners of the change.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final Player currentPlayer;
		final Iterable<Player> players;
		if (model instanceof IWorkerModel wm) {
			currentPlayer = wm.getCurrentPlayer();
			players = wm.getPlayers();
		} else {
			currentPlayer = model.getMap().getCurrentPlayer();
			players = model.getMap().getPlayers();
		}
		final Object retval = JOptionPane.showInputDialog(
			getContainingFrame(Optional.ofNullable(event.getSource())
				.filter(Component.class::isInstance).map(Component.class::cast).orElse(null)),
			"Player to view:", "Choose New Player:", JOptionPane.PLAIN_MESSAGE, null,
			StreamSupport.stream(players.spliterator(), false).toArray(Player[]::new),
			currentPlayer);
		if (retval instanceof Player p) {
			if (model instanceof IWorkerModel wm) {
				wm.setCurrentPlayer(p);
			}
			for (final PlayerChangeListener listener : listeners) {
				listener.playerChanged(currentPlayer, p);
			}
		}
	}
}
