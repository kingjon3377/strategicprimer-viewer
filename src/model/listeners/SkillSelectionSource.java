package model.listeners;

/**
 * An interface for objects that handle the user's selection of Skills.
 *
 * TODO: is there a more generic interface that could be used instead?
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
public interface SkillSelectionSource {
	/**
	 * Notify the given listener of newly selected skills.
	 * @param list a listener to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addSkillSelectionListener(SkillSelectionListener list);

	/**
	 * Stop notifying the given listener.
	 * @param list a listener to remove
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeSkillSelectionListener(SkillSelectionListener list);
}
