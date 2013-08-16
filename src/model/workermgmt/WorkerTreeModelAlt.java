package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import util.IteratorWrapper;
/**
 * An alternative implementation of the worker tree model.
 * @author Jonathan Lovelace
 *
 */
public class WorkerTreeModelAlt extends DefaultTreeModel implements
		IWorkerTreeModel {
	/**
	 * Constructor.
	 * @param player the player whose units and workers will be shown in the tree
	 * @param wmodel the driver model
	 */
	public WorkerTreeModelAlt(final Player player, final IWorkerModel wmodel) {
		super(new PlayerNode(player, wmodel), true);
		model = wmodel;
	}
	/**
	 * The driver model.
	 */
	protected final IWorkerModel model;
	/**
	 * Move a member between units.
	 * @param member a unit member
	 * @param old the prior owner
	 * @param newOwner the new owner
	 */
	@Override
	public void moveMember(final UnitMember member, final Unit old, final Unit newOwner) {
		final PlayerNode pnode = (PlayerNode) root;
		final Player player = (Player) pnode.getUserObject();
		final List<Unit> units = model.getUnits(player);
		final UnitNode oldNode = (UnitNode) pnode.getChildAt(units.indexOf(old));
		final UnitNode newNode = (UnitNode) pnode.getChildAt(units.indexOf(newOwner));
		final Iterable<TreeNode> iter = new IteratorWrapper<>(new EnumerationWrapper<TreeNode>(oldNode.children()));
		int index = -1;
		for (TreeNode node : iter) {
			if (node instanceof UnitMemberNode && ((UnitMemberNode) node).getUserObject().equals(member)) {
				index = oldNode.getIndex(node);
			}
		}
		final UnitMemberNode node = (UnitMemberNode) oldNode.getChildAt(index);
		oldNode.remove(node);
		fireTreeNodesRemoved(this, new Object[] { pnode, oldNode } , new int[] { index }, new Object[] { node });
		old.removeMember(member);
		newNode.add(node);
		fireTreeNodesInserted(this, new Object[] { pnode, newNode }, new int[] { newNode.getIndex(node) }, new Object[] { node });
		newOwner.addMember(member);
	}
	/**
	 * A node representing the player.
	 */
	public static class PlayerNode extends DefaultMutableTreeNode {
		/**
		 * Constructor.
		 * @param player the player the node represents
		 * @param model the worker model we're drawing from
		 */
		public PlayerNode(final Player player, final IWorkerModel model) {
			super(player, true);
			int index = 0;
			for (final Unit unit : model.getUnits(player)) {
				insert(new UnitNode(unit), index); // NOPMD
				index++;
			}
		}
	}
	/**
	 * A node representing a unit.
	 */
	public static class UnitNode extends DefaultMutableTreeNode {
		/**
		 * Constructor.
		 * @param unit the unit we represent.
		 */
		public UnitNode(final Unit unit) {
			super(unit, true);
			int index = 0;
			for (final UnitMember member : unit) {
				insert(new UnitMemberNode(member), index); // NOPMD
				index++;
			}
		}
	}
	/**
	 * A node representing a unit member.
	 */
	public static class UnitMemberNode extends DefaultMutableTreeNode {
		/**
		 * Constructor.
		 * @param member the unit member we represent.
		 */
		public UnitMemberNode(final UnitMember member) {
			super(member, false);
		}
	}
	/**
	 * A wrapper around an Enumeration to make it fit the Iterable interface.
	 * @param <T> the type parameter
	 */
	public static class EnumerationWrapper<T> implements Iterator<T> {
		/**
		 * @param enumer the object we're wrapping.
		 */
		public EnumerationWrapper(final Enumeration<T> enumer) {
			wrapped = enumer;
		}
		/**
		 * The object we're wrapping.
		 */
		private final Enumeration<T> wrapped;
		/**
		 * @return whether there are more elements
		 */
		@Override
		public boolean hasNext() {
			return wrapped.hasMoreElements();
		}
		/**
		 * @return the next element
		 * @throws NoSuchElementException if no more elements
		 */
		// ESCA-JAVA0126:
		// ESCA-JAVA0277:
		@Override
		public T next() throws NoSuchElementException { // NOPMD: throws clause is required by superclass
			return wrapped.nextElement();
		}
		/**
		 * Not supported.
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported by Enumeration");
		}

	}
	/**
	 * Add a unit.
	 * @param unit the unit to add
	 */
	@Override
	public void addUnit(final Unit unit) {
		model.addUnit(unit);
		final UnitNode node = new UnitNode(unit);
		((DefaultMutableTreeNode) getRoot()).add(node);
		fireTreeNodesInserted(this, new Object[] { root },
				new int[] { ((DefaultMutableTreeNode) getRoot())
						.getChildCount() - 1 }, new Object[] { node });
	}
	/**
	 * Handle a property change.
	 * @param evt the even to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("player".equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Player) {
			setRoot(new PlayerNode((Player) evt.getNewValue(), model));
		} else if ("unit".equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Unit) {
			addUnit((Unit) evt.getNewValue());
		} else if ("map".equalsIgnoreCase(evt.getPropertyName())) {
			setRoot(new PlayerNode(model.getMap().getPlayers().getCurrentPlayer(), model));
		}
	}
	/**
	 * @param obj an object
	 * @return the model object it contains if it's a node, otherwise the object itself
	 */
	@Override
	public Object getModelObject(final Object obj) {
		return obj instanceof DefaultMutableTreeNode ? ((DefaultMutableTreeNode) obj).getUserObject() : obj;
	}
	/**
	 * Add a member to a unit.
	 * @param unit the unit to contain the member
	 * @param member the member to add to it
	 */
	@Override
	public void addUnitMember(final Unit unit, final UnitMember member) {
		final PlayerNode pnode = (PlayerNode) root;
		final IteratorWrapper<UnitNode> units = new IteratorWrapper<>(
				new EnumerationWrapper<UnitNode>(pnode.children()));
		UnitNode unode = null;
		for (final UnitNode item : units) {
			if (item.getUserObject().equals(unit)) {
				unode = item;
				break;
			}
		}
		if (unode == null) {
			return;
		}
		unit.addMember(member);
		final UnitMemberNode newNode = new UnitMemberNode(member);
		unode.add(newNode);
		fireTreeNodesInserted(this, new Object[] { root, unode },
				new int[] { unode
						.getChildCount() - 1 }, new Object[] { newNode });
	}
}
