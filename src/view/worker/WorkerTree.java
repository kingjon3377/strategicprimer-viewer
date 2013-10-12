package view.worker;

import static model.map.fixtures.mobile.worker.WorkerStats.getModifierString;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.eclipse.jdt.annotation.Nullable;

import model.map.IFixture;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.WorkerTreeModelAlt;
import util.PropertyChangeSource;
import view.map.details.FixtureEditMenu;
/**
 * A tree of a player's units.
 * @author Jonathan Lovelace
 *
 */
public class WorkerTree extends JTree implements PropertyChangeSource {
	/**
	 * @param player the player whose units we want to see
	 * @param model the driver model to build on
	 * @param sources things for the model to listen to for property changes
	 */
	public WorkerTree(final Player player, final IWorkerModel model,
			final PropertyChangeSource... sources) {
		super(new WorkerTreeModelAlt(player, model));
		final WorkerTreeModelAlt tmodel = (WorkerTreeModelAlt) getModel();
		for (final PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(tmodel);
		}
		setRootVisible(false);
		setDragEnabled(true);
		setShowsRootHandles(true);
		setTransferHandler(new WorkerTreeTransferHandler(getSelectionModel(), (IWorkerTreeModel) getModel()));
		setCellRenderer(new UnitMemberCellRenderer());
		addMouseListener(new TreeMouseListener(model.getMap().getPlayers()));
		ToolTipManager.sharedInstance().registerComponent(this);
		addTreeSelectionListener(new WorkerTreeSelectionListener());
	}
	/**
	 * A listener to set up pop-up menus.
	 */
	private class TreeMouseListener extends MouseAdapter {
		/**
		 * The collection of players in the map.
		 */
		private final PlayerCollection players;
		/**
		 * Constructor.
		 * @param playerColl the collection of players in the map
		 */
		TreeMouseListener(final PlayerCollection playerColl) {
			players = playerColl;
		}
		/**
		 * @param event the event to handle
		 */
		@Override
		public void mouseClicked(@Nullable final MouseEvent event) {
			handleMouseEvent(event);
		}
		/**
		 * @param event the event to handle
		 */
		@Override
		public void mousePressed(@Nullable final MouseEvent event) {
			handleMouseEvent(event);
		}
		/**
		 * @param event the event to handle
		 */
		@Override
		public void mouseReleased(@Nullable final MouseEvent event) {
			handleMouseEvent(event);
		}
		/**
		 * @param event the event to handle. Marked @Nullable so we only have to handle the null-event case once.
		 */
		private void handleMouseEvent(@Nullable final MouseEvent event) {
			if (event != null && event.isPopupTrigger() && event.getClickCount() == 1) {
				final Object obj = ((IWorkerTreeModel) getModel())
						.getModelObject(getClosestPathForLocation(event.getX(),
								event.getY()).getLastPathComponent());
				if (obj instanceof IFixture) {
					new FixtureEditMenu((IFixture) obj, players).show(
							event.getComponent(), event.getX(), event.getY());
				}
			}
		}
	}
	/**
	 * @param evt an event indicating the mouse cursor
	 * @return a tooltip if over a worker, null otherwise
	 */
	@Override
	@Nullable public String getToolTipText(@Nullable final MouseEvent evt) {
	    if (evt == null || getRowForLocation(evt.getX(), evt.getY()) == -1) {
			return null; // NOPMD
		}
	    return getStatsToolTip(getPathForLocation(evt.getX(), evt.getY()).getLastPathComponent());
	  }
	/**
	 * @param node a node in the tree
	 * @return a tooltip if it's a worker or a worker node, null otherwise
	 */
	@Nullable private String getStatsToolTip(final Object node) {
		final Object localNode = ((IWorkerTreeModel) getModel()).getModelObject(node);
		if (localNode instanceof Worker) {
			final WorkerStats stats = ((Worker) localNode).getStats();
			if (stats != null) {
				return new StringBuilder(92)// NOPMD
						.append("<html><p>Str ")
						.append(getModifierString(stats.getStrength()))
						.append(", Dex ")
						.append(getModifierString(stats.getDexterity()))
						.append(", Con ")
						.append(getModifierString(stats.getConstitution()))
						.append(", Int ")
						.append(getModifierString(stats.getIntelligence()))
						.append(", Wis ")
						.append(getModifierString(stats.getWisdom()))
						.append(", Cha ")
						.append(getModifierString(stats.getCharisma()))
						.append("</p></html>").toString();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	/**
	 * A selection listener.
	 */
	private class WorkerTreeSelectionListener implements TreeSelectionListener {
		/**
		 * Constuctor.
		 */
		WorkerTreeSelectionListener() {
			// Needed to change visibility.
		}
		/**
		 * @param evt the event to handle
		 */
		@Override
		public void valueChanged(@Nullable final TreeSelectionEvent evt) {
			if (evt != null) {
				handleSelection(((IWorkerTreeModel) getModel()).getModelObject(evt
						.getNewLeadSelectionPath().getLastPathComponent()));
			}
		}
		/**
		 * Handle a selection.
		 * @param sel the new selection
		 */
		@SuppressWarnings("synthetic-access") // TODO: fix this properly
		private void handleSelection(final Object sel) {
			if (sel instanceof UnitMember || sel == null) {
				firePropertyChange("member", null, sel);
			}
			if (sel instanceof Unit || sel == null) {
				firePropertyChange("selUnit", null, sel);
			}
		}
	}
}
