package model.listeners;

import java.util.EventListener;

/**
 * An interface for objects that want to start something when another object has
 * finished whatever it's doing.
 *
 * FIXME: Too many uses of this should be replaced by their own set of
 * listeners; in fact, the 'result' below should be a boolean for "start afresh"
 * vs "start incremental.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 */
public interface CompletionListener extends EventListener {
	/**
	 * Stop waiting for the thing being listened to, because it's finished.
	 *
	 * @param end
	 *            whether a list should be scrolled to the end or (if false)
	 *            reset to the beginning
	 */
	void stopWaitingOn(boolean end);
}
