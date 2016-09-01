package model.listeners;

import model.map.fixtures.mobile.worker.ISkill;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for objects that want to know when the user selects a Skill from a list or
 * tree.
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
public interface SkillSelectionListener {
	/**
	 * @param selectedSkill the newly selected Skill. May be null if no selection.
	 */
	void selectSkill(@Nullable ISkill selectedSkill);
}
