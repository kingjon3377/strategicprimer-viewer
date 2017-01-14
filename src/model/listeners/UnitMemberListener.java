package model.listeners;

import java.util.EventListener;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for objects that want to know when a new unit member (usually a worker) is
 * selected.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * TODO: combine with similar interfaces?
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
public interface UnitMemberListener extends EventListener {
	/**
	 * Handle a change in the selected member.
	 * @param old      the previous selection
	 * @param selected the new selection. Because sometimes there's no selection, may be
	 *                 null.
	 */
	void memberSelected(@Nullable UnitMember old, @Nullable UnitMember selected);
}
