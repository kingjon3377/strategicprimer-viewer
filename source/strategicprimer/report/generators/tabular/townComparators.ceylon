import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.test {
    test,
    assertEquals
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    Player,
    PlayerImpl
}
import strategicprimer.model.common.map.fixtures.towns {
    TownStatus,
    TownSize,
    FortressImpl,
    ITownFixture,
    IFortress,
    Village,
    City,
    Town,
    Fortification
}
import ceylon.random {
    randomize
}

"An object to provide [[a total ordering|compareTowns]] for
 [[towns|ITownFixture]]. The methods it uses to derive that ordering are useful
 by themselves as well, and so are also shared."
object townComparators {
    "A comparator for towns, sorting them *only* on the basis of what kind of town they
     are, putting fortresses before cities before towns before fortifications before
     villages."
    shared Comparison compareTownKind(ITownFixture one, ITownFixture two) {
        if (one is IFortress) {
            if (two is IFortress) {
                return equal;
            } else {
                return smaller;
            }
        } else if (two is IFortress) {
            return larger;
        } else if (one is City) {
            if (two is City) {
                return equal;
            } else {
                return smaller;
            }
        } else if (two is City) {
            return larger;
        } else if (one is Town) {
            if (two is Town) {
                return equal;
            } else {
                return smaller;
            }
        } else if (two is Town) {
            return larger;
        } else if (one is Fortification) {
            if (two is Fortification) {
                return equal;
            } else {
                return smaller;
            }
        } else if (two is Fortification) {
            return larger;
        } else {
            assert (one is Village, two is Village);
            return equal;
        }
    }

    "A total ordering for towns."
    shared Comparison compareTowns(ITownFixture one, ITownFixture two) =>
            comparing(byIncreasing(ITownFixture.status),
                byDecreasing(ITownFixture.townSize), compareTownKind,
                byIncreasing(ITownFixture.name))(one, two);

    "Test that the town-comparison algorithms work as expected."
    test
    shared void testComparison() {
        todo("Build this inline as an immutable object instead of using add()")
        MutableList<ITownFixture> input = ArrayList<ITownFixture>();
        variable Integer id = 0;
        Player owner = PlayerImpl(1, "player");
        for (status in `TownStatus`.caseValues.sort(byIncreasing(TownStatus.ordinal))) {
            for (size in `TownSize`.caseValues.sort(byIncreasing(TownSize.ordinal))) {
                input.add(Town(status, size, -1, "inputTown", id++, owner));
                input.add(City(status, size, -1, "inputCity", id++, owner));
                input.add(Fortification(status, size, -1, "inputFortification", id++, owner));
            }
            input.add(Village(status, "inputVillage", id++, owner, "inputRace"));
        }
        input.add(FortressImpl(owner, "inputFortress", id++, TownSize.small));
        input.add(City(TownStatus.active, TownSize.large, -1, "inputCityTwo", id++, owner));
        input.add(FortressImpl(owner, "inputFortressTwo", id++, TownSize.small));
        input.add(Town(TownStatus.ruined, TownSize.medium, -1, "inputTownTwo", id++, owner));
        input.add(Fortification(TownStatus.burned, TownSize.small, -1,
            "inputFortificationTwo", id++, owner));
        input.add(Village(TownStatus.abandoned, "inputVillageTwo", id++, owner, "inputRace"));
        input.add(FortressImpl(owner, "inputFortressThree", id++, TownSize.medium));
        input.add(FortressImpl(owner, "inputFortressFour", id, TownSize.large));
        {ITownFixture*} shuffled = randomize(input);
        [ITownFixture*] expected = [FortressImpl(owner, "inputFortressFour", 47, TownSize.large),
            City(TownStatus.active, TownSize.large, -1, "inputCity",
                7, owner),
            City(TownStatus.active, TownSize.large, -1, "inputCityTwo", 41, owner),
            Town(TownStatus.active, TownSize.large, -1, "inputTown", 6, owner),
            Fortification(TownStatus.active, TownSize.large, -1, "inputFortification", 8,
                owner),
            FortressImpl(owner, "inputFortressThree", 46, TownSize.medium),
            City(TownStatus.active, TownSize.medium, -1, "inputCity", 4, owner),
            Town(TownStatus.active, TownSize.medium, -1, "inputTown", 3, owner),
            Fortification(TownStatus.active, TownSize.medium, -1, "inputFortification", 5,
                owner),
            FortressImpl(owner, "inputFortress", 40, TownSize.small),
            FortressImpl(owner, "inputFortressTwo", 42, TownSize.small),
            City(TownStatus.active, TownSize.small, -1, "inputCity", 1, owner),
            Town(TownStatus.active, TownSize.small, -1, "inputTown", 0, owner),
            Fortification(TownStatus.active, TownSize.small, -1, "inputFortification", 2,
                owner),
            Village(TownStatus.active, "inputVillage", 9, owner, "inputRace"),
            City(TownStatus.abandoned, TownSize.large, -1, "inputCity", 17, owner),
            Town(TownStatus.abandoned, TownSize.large, -1, "inputTown", 16, owner),
            Fortification(TownStatus.abandoned, TownSize.large, -1, "inputFortification", 18,
                owner),
            City(TownStatus.abandoned, TownSize.medium, -1, "inputCity", 14, owner),
            Town(TownStatus.abandoned, TownSize.medium, -1, "inputTown", 13, owner),
            Fortification(TownStatus.abandoned, TownSize.medium, -1, "inputFortification", 15,
                owner),
            City(TownStatus.abandoned, TownSize.small, -1, "inputCity", 11, owner),
            Town(TownStatus.abandoned, TownSize.small, -1, "inputTown", 10, owner),
            Fortification(TownStatus.abandoned, TownSize.small, -1, "inputFortification", 12,
                owner),
            Village(TownStatus.abandoned, "inputVillage", 19, owner, "inputRace"),
            Village(TownStatus.abandoned, "inputVillageTwo", 45, owner, "inputRace"),
            City(TownStatus.ruined, TownSize.large, -1, "inputCity", 27, owner),
            Town(TownStatus.ruined, TownSize.large, -1, "inputTown", 26, owner),
            Fortification(TownStatus.ruined, TownSize.large, -1, "inputFortification", 28,
                owner),
            City(TownStatus.ruined, TownSize.medium, -1, "inputCity", 24, owner),
            Town(TownStatus.ruined, TownSize.medium, -1, "inputTown", 23, owner),
            Town(TownStatus.ruined, TownSize.medium, -1, "inputTownTwo", 43, owner),
            Fortification(TownStatus.ruined, TownSize.medium, -1, "inputFortification", 25,
                owner),
            City(TownStatus.ruined, TownSize.small, -1, "inputCity", 21, owner),
            Town(TownStatus.ruined, TownSize.small, -1, "inputTown", 20, owner),
            Fortification(TownStatus.ruined, TownSize.small, -1, "inputFortification", 22,
                owner),
            Village(TownStatus.ruined, "inputVillage", 29, owner, "inputRace"),
            City(TownStatus.burned, TownSize.large, -1, "inputCity", 37, owner),
            Town(TownStatus.burned, TownSize.large, -1, "inputTown", 36, owner),
            Fortification(TownStatus.burned, TownSize.large, -1, "inputFortification", 38,
                owner),
            City(TownStatus.burned, TownSize.medium, -1, "inputCity", 34, owner),
            Town(TownStatus.burned, TownSize.medium, -1, "inputTown", 33, owner),
            Fortification(TownStatus.burned, TownSize.medium, -1, "inputFortification", 35,
                owner),
            City(TownStatus.burned, TownSize.small, -1, "inputCity", 31, owner),
            Town(TownStatus.burned, TownSize.small, -1, "inputTown", 30, owner),
            Fortification(TownStatus.burned, TownSize.small, -1, "inputFortification", 32,
                owner),
            Fortification(TownStatus.burned, TownSize.small, -1, "inputFortificationTwo", 44,
                owner),
            Village(TownStatus.burned, "inputVillage", 39, owner, "inputRace")];
        value sorted = shuffled.sort(townComparators.compareTowns);
    //    for (i in 0:shuffled.size) {
    //        assertEquals(sorted[i], expected[i],
    //            "``i``th element in sorted list of towns is as expected");
    //    }
        assertEquals(sorted, expected,
            "Sorted list of towns is in the order we expect");
    }
}
