package model.listeners;

import java.util.EventListener;
import model.map.Player;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for things that want to be called when the current player changes.
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
@FunctionalInterface
public interface PlayerChangeListener extends EventListener {
	/**
	 * Called when the current player changes.
	 *
	 * @param old       the previous current player
	 * @param newPlayer the new current player
	 */
	void playerChanged(@Nullable Player old, Player newPlayer);
}
