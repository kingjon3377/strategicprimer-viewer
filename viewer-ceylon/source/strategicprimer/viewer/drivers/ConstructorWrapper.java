package strategicprimer.viewer.drivers;

import controller.map.misc.CLIHelper;
import model.map.IMutableMapNG;
import model.map.IMutablePlayerCollection;
import model.map.MapDimensions;
import model.map.Player;
import model.map.SPMapNG;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;

/**
 * A wrapper around the constructors of classes thatthe Ceylon compiler in the
 * IntelliJ plugin erroneously says don't have a default constructor.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated this is a hack
 */
@Deprecated
public class ConstructorWrapper {
	/**
	 * Instantiate a unit.
	 * @param owner the unit's owner
	 * @param kind the unit's kind
	 * @param name the unit's name
	 * @param id the unit's ID.
	 */
	public static IUnit unit(final Player owner, final String kind, final String name,
							 final int id) {
		return new Unit(owner, kind, name, id);
	}
	/**
	 * Instantiate a map.
	 * @param dimensions its dimensions
	 * @param players the players in it
	 * @param currentTurn its current turn
	 */
	public static IMutableMapNG map(final MapDimensions dimensions,
									final IMutablePlayerCollection players,
									final int currentTurn) {
		return new SPMapNG(dimensions, players, currentTurn);
	}
	/**
	 * Instantiate a CLIHelper.
	 */
	public static CLIHelper cliHelper() {
		return new CLIHelper();
	}
}
