package view.worker;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import model.listeners.PlayerChangeListener;
import model.listeners.PlayerChangeSource;
import model.map.Player;
import model.misc.IDriverModel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Listens for the 'Change current player' menu item and lets the player choose
 * a new player to look at, updating listeners with the new player.
 *
 * @author Jonathan Lovelace
 *
 */
public class PlayerChooserHandler implements ActionListener,
		PlayerChangeSource {
	/**
	 * The frame to attach the dialog to.
	 */
	private final Component parent;
	/**
	 * The driver model to get the players and current player from.
	 */
	private final IDriverModel model;
	/**
	 * The current player.
	 */
	private Player player;
	/**
	 * Constructor.
	 * @param outer the component to attach the dialog to
	 * @param dmodel the driver model
	 */
	public PlayerChooserHandler(final Component outer, final IDriverModel dmodel) {
		parent = outer;
		model = dmodel;
		player = dmodel.getMap().getPlayers().getCurrentPlayer();
	}
	/**
	 * The menu item we listen for.
	 */
	public static final String MENU_ITEM = "Change current player";
	/**
	 * The list of listeners.
	 */
	private final List<PlayerChangeListener> listeners = new ArrayList<>();
	/**
	 * @param list a listener to add
	 */
	@Override
	public void addPlayerChangeListener(final PlayerChangeListener list) {
		listeners.add(list);
	}
	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removePlayerChangeListener(final PlayerChangeListener list) {
		listeners.remove(list);
	}
	/**
	 * Handle menu item.
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt != null && MENU_ITEM.equals(evt.getActionCommand())) {
			final Player retval = (Player) JOptionPane.showInputDialog(parent,
					"Player to view:", "Choose New Player",
					JOptionPane.PLAIN_MESSAGE, null, model.getMap()
							.getPlayers().asArray(), player);
			if (retval != null) {
				for (final PlayerChangeListener list : listeners) {
					list.playerChanged(player, retval);
				}
				player = retval;
			}
		}
	}
	/**
	 * Should only be called once per object lifetime. Notify all listeners, as
	 * if the current player had changed from null to its current value.
	 */
	public void notifyListeners() {
		for (final PlayerChangeListener list : listeners) {
			list.playerChanged(null, player);
		}
	}
}
