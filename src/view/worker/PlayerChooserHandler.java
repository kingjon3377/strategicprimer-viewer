package view.worker;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JOptionPane;

import model.map.Player;
import model.misc.IDriverModel;
import util.PropertyChangeSource;

/**
 * Listens for the 'Change current player' menu item and lets the player choose
 * a new player to look at, updating listeners with the new player.
 *
 * @author Jonathan Lovelace
 *
 */
public class PlayerChooserHandler implements ActionListener,
		PropertyChangeSource {
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
	 * A support object to handle the details of our listeners.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * @param list a listener to add
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}
	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}
	/**
	 * Handle menu item.
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if (MENU_ITEM.equals(evt.getActionCommand())) {
			final Player retval = (Player) JOptionPane.showInputDialog(parent,
					"Player to view:", "Choose New Player",
					JOptionPane.PLAIN_MESSAGE, null, model.getMap()
							.getPlayers().asArray(), player);
			if (retval != null) {
				pcs.firePropertyChange("player", player, retval);
				player = retval;
			}
		}
	}

}
