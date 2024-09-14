package drivers;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
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

import java.util.ArrayList;
import java.awt.Frame;
import java.awt.Component;

import legacy.map.Player;

/**
 * A class to respond to "change current player" menu items.
 */
public class PlayerChangeMenuListener implements ActionListener, PlayerChangeSource {
	public PlayerChangeMenuListener(final IDriverModel model) {
		this.model = model;
	}

	private final IDriverModel model;

	private final Collection<PlayerChangeListener> listeners = new ArrayList<>();

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
		while (!Objects.isNull(temp)) {
			switch (temp) {
				case final Frame f -> {
					return f;
				}
				case final JPopupMenu menu -> temp = menu.getInvoker();
				default -> temp = temp.getParent();
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
		if (model instanceof final IWorkerModel wm) {
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
		if (retval instanceof final Player p) {
			if (model instanceof final IWorkerModel wm) {
				wm.setCurrentPlayer(p);
			}
			for (final PlayerChangeListener listener : listeners) {
				listener.playerChanged(currentPlayer, p);
			}
		}
	}
}
