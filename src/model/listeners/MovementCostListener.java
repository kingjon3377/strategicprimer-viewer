package model.listeners;

import java.util.EventListener;

/**
 * An interface for objects that want to be notified of when a moving unit incurs movement
 * costs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@FunctionalInterface
public interface MovementCostListener extends EventListener {
	/**
	 * Account for a movement.
	 *
	 * @param cost how much it cost
	 */
	void deduct(int cost);
}
