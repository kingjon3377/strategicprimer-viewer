package model.listeners;

import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.mobile.worker.ISkill;

/**
 * An interface for objects that want to know when the user selects a Skill from a
 * list or tree.
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
 *
 */
public interface SkillSelectionListener {
	/**
	 * @param skill the newly selected Skill. May be null if no selection.
	 */
	void selectSkill(@Nullable  ISkill skill);
}
