package strategicprimer.viewer.drivers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;

/**
 * A class to make a mock object. Once SimpleUnit is ported to Ceylon, and takes
 * HasOwner instead of IUnit for movers, this can go away.
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
public class UnitProxyMaker {
	/**
	 * Make a proxy (mock object) that only responds to getOwner(), stream(), and equals().
	 * @param player the Player to return as the owner
	 * @return the mock object
	 */
	public static IUnit makeProxyFor(final Player player) {
		final InvocationHandler lambda = (proxy, method, args) -> {
			switch (method.getName()) {
			case "getOwner":
				return player;
			case "equals":
				//noinspection ObjectEquality
				return proxy == args[0];
			case "stream":
				return Stream.empty();
			default:
				throw new IllegalStateException(String.format(
						"Unsupported method %s called on mock object", method.getName()));
			}
		};
		return (IUnit) Proxy.newProxyInstance(UnitProxyMaker.class.getClassLoader(),
				new Class[]{IUnit.class}, lambda);
	}
}
