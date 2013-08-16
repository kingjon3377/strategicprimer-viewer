package model.workermgmt;

import java.beans.PropertyChangeListener;

import javax.swing.tree.TreeModel;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
/**
 * An interface to add the moveMember method to the TreeModel interface.
 * @author Jonathan Lovelace
 *
 */
public interface IWorkerTreeModel extends TreeModel, PropertyChangeListener {

	/**
	 * Move a member between units.
	 * @param member a unit member
	 * @param old the prior owner
	 * @param newOwner the new owner
	 */
	void moveMember(final UnitMember member, final Unit old, final Unit newOwner);
	/**
	 * Add a new unit. Also handles adding it to the map (via the driver model).
	 * @param unit the unit to add.
	 */
	void addUnit(final Unit unit);
	/**
	 * @param obj an object, possibly a node in the tree
	 * @return the model object it represents, if it is a node, or the object itself otherwise
	 */
	Object getModelObject(final Object obj);
	/**
	 * Add a new member to a unit.
	 * @param unit the unit that should own it
	 * @param member the member to add
	 */
	void addUnitMember(final Unit unit, final UnitMember member);
}
