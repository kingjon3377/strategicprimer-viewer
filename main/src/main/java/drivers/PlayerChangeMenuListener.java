package drivers;

import org.jetbrains.annotations.Nullable;

import javax.swing.JOptionPane;
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
	public PlayerChangeMenuListener(IDriverModel model) {
		this.model = model;
	}

	private final IDriverModel model;

	private final List<PlayerChangeListener> listeners = new ArrayList<>();

	@Override
	public void addPlayerChangeListener(PlayerChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removePlayerChangeListener(PlayerChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Get the window containing the given component. This goes beyond
	 * {@link lovelace.util:ComponentParentStream} in that we get the
	 * invoker of any {@link JPopupMenu pop-up menu}, and we also throw
	 * away the results of the intermediate steps.
	 */
	@Nullable
	private Frame getContainingFrame(@Nullable Component component) {
		Component temp = component;
		while (temp != null) {
			if (temp instanceof Frame) {
				return (Frame) temp;
			} else if (temp instanceof JPopupMenu) {
				temp = ((JPopupMenu) temp).getInvoker();
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
	public void actionPerformed(ActionEvent event) {
		Player currentPlayer;
		Iterable<Player> players;
		if (model instanceof IWorkerModel) {
			currentPlayer = ((IWorkerModel) model).getCurrentPlayer();
			players = ((IWorkerModel) model).getPlayers();
		} else {
			currentPlayer = model.getMap().getCurrentPlayer();
			players = model.getMap().getPlayers();
		}
		Object retval = JOptionPane.showInputDialog(
			getContainingFrame(Optional.ofNullable(event.getSource())
				.filter(Component.class::isInstance).map(Component.class::cast).orElse(null)),
			"Player to view:", "Choose New Player:", JOptionPane.PLAIN_MESSAGE, null,
			StreamSupport.stream(players.spliterator(), false).toArray(Player[]::new),
			currentPlayer);
		if (retval instanceof Player) {
			if (model instanceof IWorkerModel) {
				((IWorkerModel) model).setCurrentPlayer((Player) retval);
			}
			for (PlayerChangeListener listener : listeners) {
				listener.playerChanged(currentPlayer, (Player) retval);
			}
		}
	}
}