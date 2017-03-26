import ceylon.test {
    fail,
    assertEquals,
    test,
    assertNotEquals,
    assertTrue
}

import model.map {
    Player,
    PlayerImpl,
    IFixture
}
import model.map.fixtures.mobile {
    IWorker,
    Worker
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    ProxyUnit,
    IUnit,
    Unit
}
import model.map.fixtures.mobile.worker {
    Job,
    Skill
}

import util {
    NullStream
}
// Tests that the proxy classes work as expected.
"Assert that a worker contains a Job and that this Job is not empty."
void assertWorkerHasJob(IWorker worker, String jobName) {
    if (exists job = worker.getJob(jobName), !job.empty) {
        return;
    }
    StringBuilder message = StringBuilder();
    message.append("Worker should contain job ``jobName``, but contained the following:");
    message.appendNewline();
    for (job in worker) {
        message.append(job.name);
        if (job.empty) {
            message.append(" (empty)");
        }
        if (job is ProxyJob) {
            message.append(" (proxy)");
        }
        message.appendNewline();
    }
    fail(message.string);
}

test
void testProxyWorker() {
    IWorker firstWorker = Worker("one", "human", 1, Job("jobOne", 1,
        Skill("skillOne", 0, 5), Skill("skillTwo", 2, 6)));
    IWorker secondWorker = Worker("two", "elf", 2, Job("jobTwo", 1,
        Skill("skillThree", 1, 19), Skill("skillFour", 0, 99)));
    IWorker thirdWorker = Worker("three", "dwarf", 5);
    IWorker proxy = ProxyWorker.fromWorkers(firstWorker, secondWorker, thirdWorker);
    for (job in proxy) {
        for (skill in job) {
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

"Test that the next-simplest case, of a proxy for the workers in a unit, works properly."
test
void testProxyUnit() {
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
    IUnit unitOne = Unit(player, "unitKInd", "unitName", 4);
    IUnit unitTwo = unitOne.copy(false);
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
            for (skill in job) {
                skill.addHours(10, 100);
            }
        }
    }
    assertEquals(oneCopy, firstWorker, "Two copies of first worker should be equal");
    assertEquals(twoCopy, secondWorker, "Two copies of second worker should be equal");
    assertNotEquals(firstWorker, oneOrig,
        "First worker should not still be as it was originally");
    assertTrue(firstWorker.isSubset(oneOrig, NullStream.devNull, ""),
        "But first worker original should be a subset of first worker now");
    assertNotEquals(secondWorker, twoOrig,
        "Two copies of second worker shouldn't still be as it was originally");
    assertTrue(secondWorker.isSubset(twoOrig, NullStream.devNull, ""),
        "But second worker original should be a subset of second worker now");
}

"Test that the complex case, of a proxy for the workers in a unit, which is itself a proxy
 for parallel units in multiple maps, works properly."
test
void testProxyUnitProxy() {
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
    IUnit unitOne = Unit(player, "unitKInd", "unitName", 4);
    IUnit unitTwo = unitOne.copy(false);
    unitOne.addMember(firstWorker);
    unitOne.addMember(secondWorker);
    unitTwo.addMember(oneCopy);
    unitTwo.addMember(twoCopy);
    ProxyUnit proxy = ProxyUnit.fromParallelMaps(4);
    proxy.addProxied(unitOne);
    proxy.addProxied(unitTwo);
    ProxyWorker meta = ProxyWorker.fromUnit(proxy);
    for (job in meta) {
        for (skill in job) {
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
    assertTrue(firstWorker.isSubset(oneOrig, NullStream.devNull, ""),
        "But first worker original should be a subset of first worker now");
    assertNotEquals(secondWorker, twoOrig,
        "Two copies of second worker shouldn't still be as it was originally");
    assertTrue(secondWorker.isSubset(twoOrig, NullStream.devNull, ""),
        "But second worker original should be a subset of second worker now");
}

"Test that the copy() method of Worker works properly."
test
void testWorkerCopy() {
		IFixture worker = Worker("one", "human", 1,
            Job("jobOne", 1, Skill("skillOne", 0, 5),
                Skill("skillTwo", 2, 6)));
		assertEquals(worker.copy(false), worker, "Worker copy should still be equal");
}