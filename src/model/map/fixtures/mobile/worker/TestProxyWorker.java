package model.map.fixtures.mobile.worker;

import java.io.IOException;
import java.util.Formatter;
import junit.framework.AssertionFailedError;
import model.map.IFixture;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.ProxyUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import org.junit.Test;
import util.NullStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * A class to test that the proxy classes work as expected.
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
@SuppressWarnings("static-method")
public final class TestProxyWorker {
	/**
	 * Assert that a worker contains a Job and that this Job is not empty.
	 *
	 * @param worker  the worker to test
	 * @param jobName the name of the Job.
	 */
	private static void assertWorkerHasJob(final IWorker worker, final String jobName) {
		if (worker.getJob(jobName) != null) {
			return;
		}
		final StringBuilder builder = new StringBuilder(512);
		try (final Formatter format = new Formatter(builder)) {
			format.format("Worker should contain job %n, but contained the following:%n",
					jobName);
			for (final IJob job : worker) {
				format.format("%s", job.getName());
				if (job.isEmpty()) {
					format.format(" (empty)");
				}
				if (job instanceof ProxyJob) {
					format.format(" (proxy)");
				}
				format.format("%n");
			}
		}
		throw new AssertionFailedError(builder.toString());
	}

	/**
	 * Test that the simple case, of a proxy for multiple workers, works properly.
	 */
	@Test
	public void testProxyWorker() {
		final IWorker firstWorker = new Worker("one", "human", 1,
													  new Job("jobOne", 1,
																	 new Skill("skillOne",
																					  0,
																					  5),
																	 new Skill("skillTwo",
																					  2,
																					  6)));
		final IWorker secondWorker = new Worker("two", "elf", 2,
													   new Job("jobTwo", 1,
																	  new Skill("skillThree",
																						   1,
																						   19),
																	  new Skill("skillFour",
																					   0,
																					   99)));
		final IWorker thirdWorker = new Worker("three", "dwarf", 5);
		final Iterable<IJob> proxy =
				new ProxyWorker(firstWorker, secondWorker, thirdWorker);
		for (final IJob job : proxy) {
			for (final ISkill skill : job) {
				skill.addHours(10, 100);
			}
		}
		final IWorker oneCopy = new Worker("one", "human", 1,
												  new Job("jobOne", 1,
																 new Skill("skillOne", 0,
																				  15),
																 new Skill("skillTwo", 2,
																				  16)),
												  new Job("jobTwo", 0,
																 new Skill
																		 ("skillThree", 0,
																				 10),
																 new Skill
																		 ("skillFour", 0,
																				  10)));
		assertThat("First worker should have appropriate experience", firstWorker,
				equalTo(oneCopy));
		final IWorker twoCopy = new Worker("two", "elf", 2,
												  new Job("jobOne", 0,
																 new Skill("skillOne", 0,
																				  10),
																 new Skill("skillTwo", 0,
																				  10)),
												  new Job("jobTwo", 1,
																 new Skill
																		 ("skillThree", 1,
																				 29),
																 new Skill
																		 ("skillFour", 1,
																				  0)));
		assertThat("Second worker should have appropriate experience", secondWorker,
				equalTo(twoCopy));
		final IWorker threeCopy = new Worker("three", "dwarf", 5,
													new Job("jobOne", 0,
																   new Skill
																		   ("skillOne", 0,
																				   10),
																   new Skill
																		   ("skillTwo", 0,
																				   10)),
													new Job("jobTwo", 0,
																   new Skill
																		   ("skillThree",
																					0,
																					10),
																   new Skill("skillFour",
																					0,
																					10)));
		assertThat("Initially-empty worker should have appropriate experience",
				thirdWorker, equalTo(threeCopy));
	}

	/**
	 * Test that the next simplest case, of a proxy for the workers in a unit, works
	 * properly.
	 *
	 * @throws IOException never; required to be declared by our use of isSubset().
	 */
	@Test
	public void testProxyUnit() throws IOException {
		final Worker firstWorker = new Worker("one", "human", 1,
													 new Job("jobOne", 1,
																	new Skill("skillOne",
																					 0,
																					 5),
																	new Skill("skillTwo",
																					 2,
																					 6)));
		final Worker secondWorker = new Worker("two", "elf", 2,
													  new Job("jobTwo", 1,
																	 new Skill("skillThree",
																					  1,
																					  19),
																	 new Skill("skillFour",
																					  0,
																					  99)));
		final IWorker oneCopy = firstWorker.copy(false);
		final IWorker twoCopy = secondWorker.copy(false);
		final IWorker oneOrig = firstWorker.copy(false);
		final IWorker twoOrig = secondWorker.copy(false);
		final Player player = new Player(3, "");
		final IUnit unitOne = new Unit(player, "unitKInd", "unitName", 4);
		final IUnit unitTwo = unitOne.copy(false);
		unitOne.addMember(firstWorker);
		unitOne.addMember(secondWorker);
		unitTwo.addMember(oneCopy);
		unitTwo.addMember(twoCopy);
		final ProxyUnit proxy = new ProxyUnit(4);
		proxy.addProxied(unitOne);
		proxy.addProxied(unitTwo);
		for (final UnitMember member : proxy) {
			//noinspection unchecked
			for (final Iterable<ISkill> job : (Iterable<IJob>) member) {
				for (final ISkill skill : job) {
					skill.addHours(10, 100);
				}
			}
		}
		assertThat("Two copies of first worker should be equal", oneCopy,
				equalTo(firstWorker));
		assertThat("Two copies of second worker should be equal", twoCopy,
				equalTo(secondWorker));
		assertThat("First worker should not still be as it was originally", firstWorker,
				not(equalTo(oneOrig)));
		assertThat("But first worker original should be a subset of first worker now",
				Boolean.valueOf(firstWorker.isSubset(oneOrig, NullStream.DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
		assertThat("Two copies of second worker shouldn't still be as it was originally",
				secondWorker, not(equalTo(twoOrig)));
		assertThat("But second worker original should be a subset of second worker now",
				Boolean.valueOf(secondWorker.isSubset(twoOrig, NullStream.DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
	}

	/**
	 * Test that the complex case, of a proxy for the workers in a unit, which is
	 * itself a
	 * proxy for parallel units in multiple maps, works properly.
	 *
	 * @throws IOException never; required to be declared by our use of isSubset().
	 */
	@Test
	public void testProxyUnitProxy() throws IOException {
		final Worker firstWorker = new Worker("one", "human", 1,
													 new Job("jobOne", 1,
																	new Skill("skillOne",
																					 0,
																					 5),
																	new Skill("skillTwo",
																					 2,
																					 6)));
		final Worker secondWorker = new Worker("two", "elf", 2,
													  new Job("jobTwo", 1,
																	 new Skill("skillThree",
																					  1,
																					  19),
																	 new Skill("skillFour",
																					 0,
																					 99)));
		final IWorker oneCopy = firstWorker.copy(false);
		final IWorker twoCopy = secondWorker.copy(false);
		final IWorker oneOrig = firstWorker.copy(false);
		final IWorker twoOrig = secondWorker.copy(false);
		final Player player = new Player(3, "");
		final IUnit unitOne = new Unit(player, "unitKInd", "unitName", 4);
		final IUnit unitTwo = unitOne.copy(false);
		unitOne.addMember(firstWorker);
		unitOne.addMember(secondWorker);
		unitTwo.addMember(oneCopy);
		unitTwo.addMember(twoCopy);
		final ProxyUnit proxy = new ProxyUnit(4);
		proxy.addProxied(unitOne);
		proxy.addProxied(unitTwo);
		final Iterable<IJob> meta = new ProxyWorker(proxy);
		for (final IJob job : meta) {
			for (final ISkill skill : job) {
				skill.addHours(10, 100);
			}
		}
		assertThat("Two copies of first worker should be equal", oneCopy,
				equalTo(firstWorker));
		assertThat("Two copies of second worker should be equal", twoCopy,
				equalTo(secondWorker));
		assertWorkerHasJob(firstWorker, "jobTwo");
		assertWorkerHasJob(secondWorker, "jobOne");
		assertThat("First worker should not still be as it was originally", firstWorker,
				not(equalTo(oneOrig)));
		assertThat("But first worker original should be a subset of first worker now",
				Boolean.valueOf(firstWorker.isSubset(oneOrig, NullStream.DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
		assertThat("Two copies of second worker shouldn't still be as it was originally",
				secondWorker, not(equalTo(twoOrig)));
		assertThat("But second worker original should be a subset of second worker now",
				Boolean.valueOf(secondWorker.isSubset(twoOrig, NullStream.DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
	}

	/**
	 * Test that the copy() method of Worker works properly.
	 */
	@Test
	public void testWorkerCopy() {
		final IFixture worker = new Worker("one", "human", 1,
												  new Job("jobOne", 1,
																 new Skill("skillOne",
																				  0,
																				  5),
																 new Skill("skillTwo",
																				  2,
																				  6)));
		assertThat("Worker copy should still be equal", worker.copy(false),
				equalTo(worker));
	}

	/**
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestProxyWorker";
	}
}
