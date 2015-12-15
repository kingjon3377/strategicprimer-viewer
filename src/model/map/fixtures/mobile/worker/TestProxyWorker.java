package model.map.fixtures.mobile.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import model.map.fixtures.mobile.IUnit;
import org.junit.Test;

import junit.framework.AssertionFailedError;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.ProxyUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import util.NullStream;

/**
 * A class to test that the proxy classes work as expected.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
@SuppressWarnings("static-method")
public final class TestProxyWorker {
	/**
	 * Test that the simple case, of a proxy for multiple workers, works properly.
	 */
	@Test
	public void testProxyWorker() {
		final Worker one = new Worker("one", "human", 1, new Job("jobOne", 1,
				new Skill("skillOne", 0, 5), new Skill("skillTwo", 2, 6)));
		final Worker two = new Worker("two", "elf", 2, new Job("jobTwo", 1,
				new Skill("skillThree", 1, 19), new Skill("skillFour", 0, 99)));
		final Worker three = new Worker("three", "dwarf", 5);
		final Iterable<IJob> proxy = new ProxyWorker(one, two, three);
		for (IJob job : proxy) {
			for (ISkill skill : job) {
				skill.addHours(10, 100);
			}
		}
		final Worker oneCopy = new Worker("one", "human", 1,
				new Job("jobOne", 1, new Skill("skillOne", 0, 15),
						new Skill("skillTwo", 2, 16)),
				new Job("jobTwo", 0, new Skill("skillThree", 0, 10),
						new Skill("skillFour", 0, 10)));
		assertEquals("First worker should have appropriate experience", oneCopy, one);
		final Worker twoCopy = new Worker("two", "elf", 2,
				                                 new Job("jobOne", 0, new Skill("skillOne", 0, 10),
						                                        new Skill("skillTwo", 0, 10)),
				                                 new Job("jobTwo", 1, new Skill("skillThree", 1, 29),
						                                        new Skill("skillFour", 1, 0)));
		assertEquals("Second worker should have appropriate experience", twoCopy, two);
		final Worker threeCopy = new Worker("three", "dwarf", 5,
				                                   new Job("jobOne", 0, new Skill("skillOne", 0, 10),
						                                          new Skill("skillTwo", 0, 10)),
				                                   new Job("jobTwo", 0, new Skill("skillThree", 0, 10),
						                                          new Skill("skillFour", 0, 10)));
		assertEquals(
				"Initially-empty worker should have appropriate experience",
				threeCopy, three);
	}

	/**
	 * Test that the next simplest case, of a proxy for the workers in a unit,
	 * works properly.
	 *
	 * @throws IOException
	 *             never; required to be declared by our use of isSubset().
	 */
	@Test
	public void testProxyUnit() throws IOException {
		final Worker one = new Worker("one", "human", 1, new Job("jobOne", 1,
				new Skill("skillOne", 0, 5), new Skill("skillTwo", 2, 6)));
		final Worker two = new Worker("two", "elf", 2, new Job("jobTwo", 1,
				new Skill("skillThree", 1, 19), new Skill("skillFour", 0, 99)));
		final Worker oneCopy = one.copy(false);
		final Worker twoCopy = two.copy(false);
		final Worker oneOrig = one.copy(false);
		final Worker twoOrig = two.copy(false);
		final Player player = new Player(3, "");
		final IUnit unitOne = new Unit(player, "unitKInd", "unitName", 4);
		final IUnit unitTwo = unitOne.copy(false);
		unitOne.addMember(one);
		unitOne.addMember(two);
		unitTwo.addMember(oneCopy);
		unitTwo.addMember(twoCopy);
		final ProxyUnit proxy = new ProxyUnit(4);
		proxy.addProxied(unitOne);
		proxy.addProxied(unitTwo);
		for (UnitMember member : proxy) {
			for (Iterable<ISkill> job : (Iterable<IJob>) member) {
				for (ISkill skill : job) {
					skill.addHours(10, 100);
				}
			}
		}
		assertEquals("Two copies of first worker should be equal", one,
				oneCopy);
		assertEquals("Two copies of second worker should be equal", two,
				twoCopy);
		assertFalse("First worker should not still be as it was originally",
				oneOrig.equals(one));
		assertTrue(
				"But first worker original should be a subset of first worker now",
				one.isSubset(oneOrig, NullStream.DEV_NULL, ""));
		assertFalse(
				"Two copies of second worker shouldn't still be as it was originally",
				twoOrig.equals(two));
		assertTrue(
				"But second worker original should be a subset of second worker now",
				two.isSubset(twoOrig, NullStream.DEV_NULL, ""));
	}

	/**
	 * Test that the complex case, of a proxy for the workers in a unit, which
	 * is itself a proxy for parallel units in multiple maps, works properly.
	 *
	 * @throws IOException never; required to be declared by our use of isSubset().
	 */
	@Test
	public void testProxyUnitProxy() throws IOException {
		final Worker one = new Worker("one", "human", 1, new Job("jobOne", 1,
				new Skill("skillOne", 0, 5), new Skill("skillTwo", 2, 6)));
		final Worker two = new Worker("two", "elf", 2, new Job("jobTwo", 1,
				new Skill("skillThree", 1, 19), new Skill("skillFour", 0, 99)));
		final Worker oneCopy = one.copy(false);
		final Worker twoCopy = two.copy(false);
		final Worker oneOrig = one.copy(false);
		final Worker twoOrig = two.copy(false);
		final Player player = new Player(3, "");
		final IUnit unitOne = new Unit(player, "unitKInd", "unitName", 4);
		final IUnit unitTwo = unitOne.copy(false);
		unitOne.addMember(one);
		unitOne.addMember(two);
		unitTwo.addMember(oneCopy);
		unitTwo.addMember(twoCopy);
		final ProxyUnit proxy = new ProxyUnit(4);
		proxy.addProxied(unitOne);
		proxy.addProxied(unitTwo);
		final Iterable<IJob> meta = new ProxyWorker(proxy);
		for (IJob job : meta) {
			for (ISkill skill : job) {
				skill.addHours(10, 100);
			}
		}
		assertEquals("Two copies of first worker should be equal", one,
				oneCopy);
		assertEquals("Two copies of second worker should be equal", two,
				twoCopy);
		assertWorkerHasJob(one, "jobTwo");
		assertWorkerHasJob(two, "jobOne");
		assertFalse("First worker should not still be as it was originally",
				oneOrig.equals(one));
		assertTrue(
				"But first worker original should be a subset of first worker now",
				one.isSubset(oneOrig, NullStream.DEV_NULL, ""));
		assertFalse(
				"Two copies of second worker shouldn't still be as it was originally",
				twoOrig.equals(two));
		assertTrue(
				"But second worker original should be a subset of second worker now",
				two.isSubset(twoOrig, NullStream.DEV_NULL, ""));
	}
	/**
	 * Assert that a worker contains a Job and that this Job is not empty.
	 * @param worker the worker to test
	 * @param jobName the name of the Job.
	 */
	public void assertWorkerHasJob(final IWorker worker, final String jobName) {
		if (worker.getJob(jobName) != null) {
			return;
		}
		final StringBuilder builder = new StringBuilder("Worker should contain job ");
		builder.append(jobName);
		builder.append(". Worker contained the following: \n");
		for (IJob job : worker) {
			builder.append(job.getName());
			if (job.isEmpty()) {
				builder.append(" (empty)");
			}
			if (job instanceof ProxyJob) {
				builder.append(" (proxy)");
			}
			builder.append('\n');
		}
		throw new AssertionFailedError(builder.toString());
	}
	/**
	 * Test that the copy() method of Worker works properly.
	 */
	@Test
	public void testWorkerCopy() {
		final IWorker one = new Worker("one", "human", 1, new Job("jobOne", 1,
				new Skill("skillOne", 0, 5), new Skill("skillTwo", 2, 6)));
		assertEquals("Worker copy should still be equal", one, one.copy(false));
	}
	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "TestProxyWorker";
	}
}
