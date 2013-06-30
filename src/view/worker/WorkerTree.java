package view.worker;

import javax.swing.JTree;

import model.map.Player;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerTreeModel;
/**
 * A tree of a player's units.
 * @author Jonathan Lovelace
 *
 */
public class WorkerTree extends JTree {
	/**
	 * @param player the player whose units we want to see
	 * @param model the driver model to build on
	 */
	public WorkerTree(final Player player, final IWorkerModel model) {
		super(new WorkerTreeModel(player, model));
		setRootVisible(false);
		setDragEnabled(true);
	}
}
