package model.workermgmt;

import javax.swing.tree.TreeModel;
import model.listeners.MapChangeListener;
import model.listeners.NewUnitListener;
import model.listeners.PlayerChangeListener;
import model.map.HasKind;
import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;

/**
 * An interface to add the moveMember method to the TreeModel interface.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IWorkerTreeModel extends TreeModel, NewUnitListener,
		                                          PlayerChangeListener,
		                                          MapChangeListener {

	/**
	 * Move a member between units.
	 *
	 * @param member   a unit member
	 * @param old      the prior owner
	 * @param newOwner the new owner
	 */
	void moveMember(UnitMember member, IUnit old, IUnit newOwner);

	/**
	 * Add a new unit. Also handles adding it to the map (via the driver model).
	 *
	 * @param unit the unit to add.
	 */
	void addUnit(IUnit unit);

	/**
	 * @param obj an object, possibly a node in the tree
	 * @return the model object it represents, if it is a node, or the object itself
	 * otherwise
	 */
	Object getModelObject(Object obj);

	/**
	 * Add a new member to a unit.
	 *
	 * @param unit   the unit that should own it
	 * @param member the member to add
	 */
	void addUnitMember(IUnit unit, UnitMember member);

	/**
	 * Rename a worker or unit.
	 *
	 * @param item the item that has changed
	 */
	void renameItem(HasName item);

	/**
	 * Change a unit's (or other member's) kind. If a unit, this means it has moved in
	 * the
	 * tree.
	 *
	 * @param item the item that has changed
	 */
	void moveItem(HasKind item);

	/**
	 * Dismiss a member from a unit and the player's service.
	 *
	 * @param member the member to dismiss.
	 */
	void dismissUnitMember(UnitMember member);

	/**
	 * @return Unit members that have been dismissed during this session.
	 */
	Iterable<UnitMember> dismissed();
}
