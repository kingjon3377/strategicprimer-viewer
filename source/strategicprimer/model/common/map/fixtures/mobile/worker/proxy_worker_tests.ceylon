import ceylon.test {
    fail,
    assertEquals,
    test,
    assertNotEquals,
    assertTrue,
    assertFalse
}

import strategicprimer.model.common.map {
    IFixture,
    Player,
    PlayerImpl
}
import strategicprimer.model.common.map.fixtures.mobile {
    IMutableUnit,
    Unit,
    IMutableWorker,
    IWorker,
    Worker,
    ProxyUnit
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    ProxyJob,
    Job,
    Skill,
    ISkill,
    ProxyWorker
}

import lovelace.util.common {
    as
}

"Tests that the proxy classes work as expected."
object proxyWorkerTests {
    "Assert that a worker contains a Job and that this Job is not empty."
    void assertWorkerHasJob(IWorker worker, String jobName) {
        if (!worker.getJob(jobName).emptyJob) {
            return;
        }
        StringBuilder message = StringBuilder();
        message.append(
            "Worker should contain job ``jobName``, but contained the following:");
        message.appendNewline();
        for (job in worker) {
            message.append(job.name);
            if (job.emptyJob) {
                message.append(" (empty)");
            }
            if (job is ProxyJob) {
                message.append(" (proxy)");
            }
            message.appendNewline();
        }
        fail(message.string);
    }

    "Test that adding experience to a proxy for multiple workers, not in a
     unit, works properly."
    test
    shared void testProxyWorker() {
        IMutableWorker firstWorker = Worker("one", "human", 1, Job("jobOne", 1,
            Skill("skillOne", 0, 5), Skill("skillTwo", 2, 6)));
        IMutableWorker secondWorker = Worker("two", "elf", 2, Job("jobTwo", 1,
            Skill("skillThree", 1, 19), Skill("skillFour", 0, 99)));
        IMutableWorker thirdWorker = Worker("three", "dwarf", 5);
        IMutableWorker proxy = ProxyWorker.fromWorkers(firstWorker, secondWorker, thirdWorker);
        for (job in proxy) {
            for (skill in job.narrow<IMutableSkill>()) {
                skill.addHours(10, 100);
            }
        }
        IWorker oneCopy = Worker("one", "human", 1,
            Job("jobOne", 1, Skill("skillOne", 0, 15),
                Skill("skillTwo", 2, 16)),
            Job("jobTwo", 0, Skill("skillThree", 0, 10),
                Skill("skillFour", 0, 10)));
        assertEquals(firstWorker, oneCopy,
            "First worker should have appropriate experience");
        IWorker twoCopy = Worker("two", "elf", 2,
            Job("jobOne", 0, Skill("skillOne", 0, 10),
                Skill("skillTwo", 0, 10)),
            Job("jobTwo", 1, Skill("skillThree", 1, 29),
                Skill("skillFour", 1, 0)));
        assertEquals(secondWorker, twoCopy,
            "Second worker should have appropriate experience");
        IWorker threeCopy = Worker("three", "dwarf", 5,
            Job("jobOne", 0, Skill("skillOne", 0, 10),
                Skill("skillTwo", 0, 10)),
            Job("jobTwo", 0, Skill("skillThree", 0, 10),
                Skill("skillFour", 0, 10)));
        assertEquals(thirdWorker, threeCopy,
            "Initially-empty worker should have appropriate experience");
    }

    "Test that the next-simplest case, of a proxy for the workers in a unit, works
     properly."
    test
    shared void testProxyUnit() {
        Worker firstWorker = Worker("one", "human", 1,
            Job("jobOne", 1,Skill("skillOne",0,5),
                Skill("skillTwo",2,6)));
        Worker secondWorker = Worker("two", "elf", 2,
            Job("jobTwo", 1,Skill("skillThree",1,19),
                Skill("skillFour",0,99)));
        IWorker oneCopy = firstWorker.copy(false);
        IWorker twoCopy = secondWorker.copy(false);
        IWorker oneOrig = firstWorker.copy(false);
        IWorker twoOrig = secondWorker.copy(false);
        Player player = PlayerImpl(3, "");
        IMutableUnit unitOne = Unit(player, "unitKInd", "unitName", 4);
        IMutableUnit unitTwo = unitOne.copy(false);
        unitOne.addMember(firstWorker);
        unitOne.addMember(secondWorker);
        unitTwo.addMember(oneCopy);
        unitTwo.addMember(twoCopy);
        ProxyUnit proxy = ProxyUnit.fromParallelMaps(4);
        proxy.addProxied(unitOne);
        proxy.addProxied(unitTwo);
        for (member in proxy) {
            assert (is IWorker member);
            //noinspection unchecked
            for (job in member) {
                for (skill in job.narrow<IMutableSkill>()) {
                    skill.addHours(10, 100);
                }
            }
        }
        assertEquals(oneCopy, firstWorker, "Two copies of first worker should be equal");
        assertEquals(twoCopy, secondWorker,
            "Two copies of second worker should be equal");
        assertNotEquals(firstWorker, oneOrig,
            "First worker should not still be as it was originally");
        assertTrue(firstWorker.isSubset(oneOrig, noop),
            "But first worker original should be a subset of first worker now");
        assertNotEquals(secondWorker, twoOrig,
            "Two copies of second worker shouldn't still be as it was originally");
        assertTrue(secondWorker.isSubset(twoOrig, noop),
            "But second worker original should be a subset of second worker now");
    }

    "Test that the complex case, of a proxy for the workers in a unit, which is itself a
     proxy for parallel units in multiple maps, works properly."
    test
    shared void testProxyUnitProxy() {
        Worker firstWorker = Worker("one", "human", 1,
            Job("jobOne", 1,Skill("skillOne",0,5),
                Skill("skillTwo",2,6)));
        Worker secondWorker = Worker("two", "elf", 2,
            Job("jobTwo", 1,Skill("skillThree",1,19),
                Skill("skillFour",0,99)));
        IWorker oneCopy = firstWorker.copy(false);
        IWorker twoCopy = secondWorker.copy(false);
        IWorker oneOrig = firstWorker.copy(false);
        IWorker twoOrig = secondWorker.copy(false);
        Player player = PlayerImpl(3, "");
        IMutableUnit unitOne = Unit(player, "unitKInd", "unitName", 4);
        IMutableUnit unitTwo = unitOne.copy(false);
        unitOne.addMember(firstWorker);
        unitOne.addMember(secondWorker);
        unitTwo.addMember(oneCopy);
        unitTwo.addMember(twoCopy);
        ProxyUnit proxy = ProxyUnit.fromParallelMaps(4);
        proxy.addProxied(unitOne);
        proxy.addProxied(unitTwo);
        ProxyWorker meta = ProxyWorker.fromUnit(proxy);
        for (job in meta) {
            for (skill in job.narrow<IMutableSkill>()) {
                skill.addHours(10, 100);
            }
        }
        assertEquals(oneCopy, firstWorker,
            "Two copies of first worker should be equal");
        assertEquals(twoCopy, secondWorker,
            "Two copies of second worker should be equal");
        assertWorkerHasJob(firstWorker, "jobTwo");
        assertWorkerHasJob(secondWorker, "jobOne");
        assertNotEquals(firstWorker, oneOrig,
            "First worker should not still be as it was originally");
        assertTrue(firstWorker.isSubset(oneOrig, noop),
            "But first worker original should be a subset of first worker now");
        assertNotEquals(secondWorker, twoOrig,
            "Two copies of second worker shouldn't still be as it was originally");
        assertTrue(secondWorker.isSubset(twoOrig, noop),
            "But second worker original should be a subset of second worker now");
    }

    "Test that the copy() method of Worker works properly."
    test
    shared void testWorkerCopy() {
            IFixture worker = Worker("one", "human", 1,
                Job("jobOne", 1, Skill("skillOne", 0, 5),
                    Skill("skillTwo", 2, 6)));
            assertEquals(worker.copy(false), worker, "Worker copy should still be equal");
    }

    "Test that removing a Skill from workers via a proxy works properly."
    test
    shared void testRemoval() {
        Boolean nonemptySkill(ISkill skill) => !skill.empty;
        ISkill skillOne = Skill("skillOne", 0, 10);
        ISkill skillTwo = Skill("skillOne", 0, 10);
        IMutableJob jobOne = Job("jobOne", 0, skillOne, Skill("skillTwo", 2, 5));
        IMutableJob jobTwo = Job("jobOne", 0, skillTwo, Skill("skillThree", 1, 8),
            Skill("skillFour", 5, 0));
        assertTrue(jobTwo.map(ISkill.name).any("skillFour".equals),
            "Extra skill is present at first");
        jobTwo.removeSkill(Skill("skillFour", 5, 0));
        assertFalse(jobTwo.filter(nonemptySkill).map(ISkill.name).any("skillFour".equals),
            "Extra skill isn't present after being removed");
        IWorker workerOne = Worker("workerName", "workerRace", 1, jobOne);
        IWorker workerTwo = Worker("workerName", "workerRace", 1, jobTwo);
        assertTrue(jobOne.map(ISkill.name).any("skillOne".equals),
            "Common skill is present at first");
        assertTrue(jobTwo.map(ISkill.name).any("skillOne".equals),
            "Common skill is present at first");
        IMutableJob proxyJob = ProxyJob("jobOne", true, workerOne, workerTwo);
        proxyJob.removeSkill(Skill("skillOne", 0, 10));
        assertFalse(jobOne.map(ISkill.name).any("skillOne".equals),
            "Common skill isn't there after being removed");
        assertFalse(jobTwo.map(ISkill.name).any("skillOne".equals),
            "Common skill isn't there after being removed");
    }

    "Test that sorting workers via a proxy works properly."
    test
    shared void testSorting() {
        IWorker workerOne = Worker("f", "human", 1);
        IWorker workerTwo = Worker("f", "human", 1);
        IWorker workerThree = Worker("e", "human", 2);
        IWorker workerFour = Worker("d", "human", 3);
        IWorker workerFive = Worker("c", "human", 4);
        IWorker workerSix = Worker("c", "human", 4);
        IWorker workerSeven = Worker("b", "human", 5);
        IWorker workerEight = Worker("a", "human", 6);
        IWorker workerNine = Worker("a", "human", 6);
        value player = PlayerImpl(1, "player");
        IMutableUnit unitOne = Unit(player, "unit", "unit", 7);
        for (worker in [workerOne, workerThree, workerFour, workerFive, workerSeven, workerEight]) {
            unitOne.addMember(worker);
        }
        IMutableUnit unitTwo = Unit(player, "unit", "unit", 7);
        for (worker in [workerTwo, workerSix, workerNine]) {
            unitTwo.addMember(worker);
        }
        value proxy = ProxyUnit.fromParallelMaps(7);
        proxy.addProxied(unitOne);
        proxy.addProxied(unitTwo);
        assertEquals(as<IWorker>(unitOne.first)?.name, "f", "Unit is not initially sorted");
        assertEquals(as<IWorker>(unitTwo.first)?.name, "f", "Unit is not initially sorted");
        assertEquals(as<IWorker>(proxy.first)?.name, "f", "Proxy unit is not initially sorted");
        proxy.sortMembers();
        assertEquals(as<IWorker>(unitOne.first)?.name, "a", "Sorting proxy sorts the units");
        assertEquals(as<IWorker>(unitTwo.first)?.name, "a", "Sorting proxy sorts the units");
        assertEquals(as<IWorker>(proxy.first)?.name, "a", "Sorting proxy works");
    }
}
