import ceylon.test {
    assertEquals,
    test,
    assertNotEquals,
    assertTrue
}

import strategicprimer.model.common.map {
    IFixture,
    Player,
    PlayerImpl
}
import strategicprimer.model.common.map.fixtures.mobile {
    IMutableUnit,
    Unit,
    IWorker,
    Worker,
    ProxyUnit
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    Job,
    Skill
}

"Tests that the proxy classes work as expected."
object proxyWorkerTests {
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

    "Test that the copy() method of Worker works properly."
    test
    shared void testWorkerCopy() {
            IFixture worker = Worker("one", "human", 1,
                Job("jobOne", 1, Skill("skillOne", 0, 5),
                    Skill("skillTwo", 2, 6)));
            assertEquals(worker.copy(false), worker, "Worker copy should still be equal");
    }
}
