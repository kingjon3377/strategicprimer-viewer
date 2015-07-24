package model.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.map.fixtures.mobile.ProxyFor;

/**
 * An implementation of ISkill whose operations act on multiple workers at once.
 * @author Jonathan Lovelace
 *
 */
public class ProxySkill implements ISkill, ProxyFor<IJob> {
	/**
	 * The name of the skill.
	 */
	private String name;
	/**
	 * The Jobs we're proxying for.
	 */
	private final List<IJob> proxied = new ArrayList<>();
	/**
	 * @param nomen the name of the skill
	 * @param jobs the Jobs to add skill hours to when asked
	 */
	public ProxySkill(final String nomen, final Job... jobs) {
		name = nomen;
		for (final Job job : jobs) {
			proxied.add(job);
		}
	}
	/**
	 * @return the skills' name
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
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
				if (skill instanceof ProxySkill) {
					continue;
				} else if (skill.getName().equals(name)
						&& skill.getLevel() < level) {
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
				if (skill instanceof ProxySkill) {
					continue;
				} else if (skill.getName().equals(name)
						&& skill.getHours() > hours) {
					hours = skill.getHours();
				}
			}
		}
		return hours;
	}

	/**
	 * @param hrs
	 *            how many hours to add
	 * @param condition
	 *            the seed to randomly generate conditions for the proxied skills
	 */
	@Override
	public void addHours(final int hrs, final int condition) {
		final Random random = new Random(condition);
		for (final IJob job : proxied) {
			boolean touched = false;
			for (final ISkill skill : job) {
				if (skill instanceof ProxySkill) {
					continue;
				} else if (skill != null && skill.getName().equals(name)) {
					skill.addHours(hrs, random.nextInt(100));
					touched = true;
				}
			}
			if (!touched) {
				final Skill skill = new Skill(name, 0, 0);
				skill.addHours(hrs, random.nextInt(100));
				job.addSkill(skill);
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
	 * @param item the job to add to the list
	 */
	@Override
	public void addProxied(final IJob item) {
		proxied.add(item);
	}
	/**
	 * Note that this is the *one* place where ProxySkill should be a ProxyFor<ISkill> rather than ProxyFor<IJob>.
	 * @return the proxied Jobs.
	 */
	@Override
	public Iterable<IJob> getProxied() {
		return proxied;
	}
}
