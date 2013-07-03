package model.workermgmt;

import javax.swing.tree.TreeModel;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
/**
 * An interface to add the moveMember method to the TreeModel interface.
 * @author Jonathan Lovelace
 *
 */
public interface IWorkerTreeModel extends TreeModel {

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

}
