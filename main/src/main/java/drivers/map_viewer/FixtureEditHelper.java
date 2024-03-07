package drivers.map_viewer;

import common.map.HasName;
import legacy.map.HasOwner;
import org.jetbrains.annotations.Nullable;

import legacy.map.fixtures.UnitMember;

import legacy.map.fixtures.mobile.IUnit;

import legacy.map.Player;
import legacy.map.HasKind;

import worker.common.IFixtureEditHelper;

import drivers.common.IFixtureEditingModel;

public class FixtureEditHelper implements IFixtureEditHelper {
	public FixtureEditHelper(final IFixtureEditingModel model) {
		this.model = model;
	}

	private final IFixtureEditingModel model;

	/**
	 * Move a member between units.
	 */
	@Override
	public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		model.moveMember(member, old, newOwner);
	}

	/**
	 * Add a new unit, and also handle adding it to the map (via the driver model).
	 */
	@Override
	public void addUnit(final IUnit unit) {
		model.addUnit(unit);
	}

	/**
	 * (Try to) remove a unit (from the map, via the driver model).
	 */
	@Override
	public void removeUnit(final IUnit unit) {
		model.removeUnit(unit);
	}

	/**
	 * Add a new member to a unit.
	 */
	@Override
	public void addUnitMember(final IUnit unit, final UnitMember member) {
		model.addUnitMember(unit, member);
	}

	/**
	 * Change something's name.
	 */
	public void renameItem(final HasName item, final String newName) {
		model.renameItem(item, newName);
	}

	/**
	 * Change something's kind; in the worker mgmt GUI, if it's a unit,
	 * this means moving it in the tree, since units' kinds are currently
	 * their parent nodes.
	 */
	@Override
	public void changeKind(final HasKind item, final String newKind) {
		model.changeKind(item, newKind);
	}

	/**
	 * Dismiss a unit member from a unit and from the player's service.
	 */
	@Override
	public void dismissUnitMember(final UnitMember member) {
		model.dismissUnitMember(member);
	}

	/**
	 * Add a unit member to the unit that contains the given member. If the
	 * base is not in the tree, the model is likely to simply ignore the
	 * call, but the behavior is undefined.
	 */
	@Override
	public void addSibling(final UnitMember base, final UnitMember sibling) {
		model.addSibling(base, sibling);
	}

	/**
	 * Change the owner of the given item.
	 */
	@Override
	public void changeOwner(final HasOwner item, final Player newOwner) {
		model.changeOwner(item, newOwner);
	}

	/**
	 * Sort the contents of the given unit.
	 */
	@Override
	public void sortMembers(final IUnit fixture) {
		model.sortFixtureContents(fixture);
	}

	@Override
	public void mapChanged() {
	} // TODO: Do we need to implement this?

	@Override
	public void mapMetadataChanged() {
	}

	/**
	 * Add a new unit.
	 */
	@Override
	public void addNewUnit(final IUnit unit) {
		addUnit(unit);
	}

	@Override
	public void playerChanged(final @Nullable Player previousCurrent, final Player newCurrent) {
	} // TODO: Do we need to implement this?
}
