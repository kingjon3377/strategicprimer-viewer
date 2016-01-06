package model.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import model.map.fixtures.mobile.ProxyFor;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An implementation of ISkill whose operations act on multiple workers at once.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
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
public final class ProxySkill implements ISkill, ProxyFor<IJob> {
	/**
	 * If false, the worker containing this is representing all the workers in a single
	 * unit; if true, it is representing corresponding workers in corresponding units in
	 * different maps. Thus, if true, we should use the same "random" seed repeatedly in
	 * any given adding-hours operation, and not if false.
	 */
	private final boolean parallel;
	/**
	 * The name of the skill.
	 */
	private String name;
	/**
	 * The Jobs we're proxying for.
	 */
	private final Collection<IJob> proxied = new ArrayList<>();

	/**
	 * @param nomen  the name of the skill
	 * @param parall whether the worker containing this represents corresponding units in
	 *               different maps, rather than workers in a single unit
	 * @param jobs   the Jobs to add skill hours to when asked
	 */
	public ProxySkill(final String nomen, final boolean parall,
	                  final @NonNull IJob @NonNull ... jobs) {
		parallel = parall;
		name = nomen;
		Collections.addAll(proxied, jobs);
	}

	/**
	 * @return a copy of this proxy
	 */
	@Override
	public ISkill copy() {
		final ProxySkill retval = new ProxySkill(name, parallel);
		for (final IJob job : proxied) {
			retval.addProxied(job.copy(false));
		}
		return retval;
	}

	/**
	 * @return the skills' name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param nomen the skills' new name
	 */
	@Override
	public void setName(final String nomen) {
		name = nomen;
	}

	/**
	 * @return the lowest level any of the proxied Jobs has the skill
	 */
	@Override
	public int getLevel() {
		int level = Integer.MAX_VALUE;
		for (final IJob job : proxied) {
			for (final ISkill skill : job) {
				if (skill == this) {
					continue;
				} else if (skill.getName().equals(name)
						           &&
						           (skill.getLevel() <
								            level)) {
					level = skill.getLevel();
				}
			}
		}
		if (level == Integer.MAX_VALUE) {
			return 0;
		} else {
			return level;
		}
	}

	/**
	 * @return the most hours any of the proxied Jobs has for the skill
	 */
	@Override
	public int getHours() {
		int hours = 0;
		for (final IJob job : proxied) {
			for (final ISkill skill : job) {
				if (skill == this) {
					continue;
				} else if (skill.getName().equals(name)
						           &&
						           (skill.getHours() >
								            hours)) {
					hours = skill.getHours();
				}
			}
		}
		return hours;
	}

	/**
	 * @param hrs       how many hours to add
	 * @param condition the seed to randomly generate conditions for the proxied skills
	 */
	@Override
	public void addHours(final int hrs, final int condition) {
		final Random random = new Random(condition);
		if (parallel) {
			for (final IJob job : proxied) {
				boolean unmodified = true;
				for (final ISkill skill : job) {
					if (skill == this) {
						continue;
					} else if (skill.getName().equals(name)) {
						skill.addHours(hrs, condition);
						unmodified = false;
					}
				}
				if (unmodified) {
					final ISkill skill = new Skill(name, 0, 0);
					skill.addHours(hrs, condition);
					job.addSkill(skill);
				}
			}
		} else {
			for (final IJob job : proxied) {
				boolean unmodified = true;
				for (final ISkill skill : job) {
					if (skill == this) {
						continue;
					} else if (skill.getName().equals(name)) {
						skill.addHours(hrs, random.nextInt(100));
						unmodified = false;
					}
				}
				if (unmodified) {
					final ISkill skill = new Skill(name, 0, 0);
					job.addSkill(skill);
					boolean absent = true;
					for (final ISkill temp : job) {
						if (temp.getName().equals(name)) {
							temp.addHours(hrs, random.nextInt(100));
							absent = false;
							break;
						}
					}
					if (absent) {
						skill.addHours(hrs, random.nextInt(100));
					}
				}
			}
		}
	}

	/**
	 * @return a String representation
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Add a job to the list of jobs we're proxying a skill for.
	 *
	 * @param item the job to add to the list
	 */
	@Override
	public void addProxied(final IJob item) {
		proxied.add(item);
	}

	/**
	 * Note that this is the *one* place where ProxySkill should be a ProxyFor <ISkill>
	 * rather than ProxyFor<IJob>.
	 *
	 * @return the proxied Jobs.
	 */
	@Override
	public Iterable<IJob> getProxied() {
		return new ArrayList<>(proxied);
	}

	/**
	 * @return whther any of the proxied Skills is "empty"
	 */
	@Override
	public boolean isEmpty() {
		for (final IJob job : proxied) {
			for (final ISkill skill : job) {
				if (skill == this) {
					continue;
				} else if (skill.getName().equals(name) && !skill.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return Whether this should be considered (if true) a proxy for multiple
	 * representations of the same Job, e.g. in different maps, or (if false) a proxy for
	 * different related Jobss.
	 */
	@Override
	public boolean isParallel() {
		// FIXME: Because this is a ProxyFor<IJob>, it should probably return true
		// always ...
		return parallel;
	}
}
