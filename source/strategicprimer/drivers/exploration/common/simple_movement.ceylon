import ceylon.collection {
    MutableList,
    ArrayList
}

import lovelace.util.common {
    todo,
	matchingValue,
	matchingPredicate,
    singletonRandom
}
import strategicprimer.model.map {
    River,
    TileFixture,
    TileType,
    HasOwner
}
import strategicprimer.model.map.fixtures.mobile.worker {
    WorkerStats,
	ISkill
}

import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    IWorker
}
import strategicprimer.model.map.fixtures.terrain {
    Hill,
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    ITownFixture
}
import strategicprimer.drivers.exploration.common {
    Direction
}
import ceylon.random {
    randomize
}
shared object simpleMovementModel {
	"Whether land movement is possible on the given terrain."
	shared Boolean landMovementPossible(TileType terrain) => TileType.ocean != terrain;
	"Whether rivers in either the source or the destination will speed travel in the given
	 direction." // "X in Y" means "Y.contains(X)"
	shared Boolean riversSpeedTravel(Direction direction,
	        {River*} sourceRivers, {River*} destRivers) {
	    Boolean recurse(Direction partial) =>
	            riversSpeedTravel(partial, sourceRivers, destRivers);
	    switch (direction)
	    case (Direction.north) {
			return River.north in sourceRivers || River.south in destRivers;
	    }
	    case (Direction.northeast) {
	        return recurse(Direction.north) || recurse(Direction.east);
	    }
	    case (Direction.east) {
			return River.east in sourceRivers || River.west in destRivers;
	    }
	    case (Direction.southeast) {
	        return recurse(Direction.south) || recurse(Direction.east);
	    }
	    case (Direction.south) {
			return River.south in sourceRivers || River.north in destRivers;
	    }
	    case (Direction.southwest) {
	        return recurse(Direction.south) || recurse(Direction.west);
	    }
	    case (Direction.west) {
			return River.west in sourceRivers || River.east in destRivers;
	    }
	    case (Direction.northwest) {
	        return recurse(Direction.north) || recurse(Direction.west);
	    }
	    case (Direction.nowhere) { return false; }
	}
	"Get the cost of movement in the given conditions."
	shared Integer movementCost(
	        """The terrain being traversed. Null if "not visible.""""
	        TileType? terrain,
	        "Whether the location is forested"
	        Boolean forest,
	        "Whether the location is mountainous"
	        Boolean mountain,
	        "Whether the location has a river that reduces cost"
	        Boolean river,
	        "The fixtures at the location"
	        {TileFixture*} fixtures) {
	    if (exists terrain) {
	        if (TileType.ocean == terrain) {
	            return runtime.maxArraySize;
	        } else if (forest || mountain || !fixtures.narrow<Forest|Hill>().empty ||
	                TileType.desert == terrain) {
	            return (river) then 2 else 3;
	        } else if (TileType.jungle == terrain || TileType.swamp == terrain) {
	            return (river) then 4 else 6;
	        } else {
	            assert (TileType.steppe == terrain || TileType.plains == terrain ||
	                TileType.tundra == terrain);
	            return (river) then 1 else 2;
	        }
	    } else {
	        return runtime.maxArraySize;
	    }
	}

	"Check whether a unit moving at the given relative speed might notice the given fixture.
	 Units do not notice themselves, do not notice themselves, and do not notice null
	 fixtures."
	todo("We now check DCs on Events, but ignore relevant skills other than Perception. And
	      now a lot more things have DCs for which those other skills are relevant.")
	shared Boolean shouldSometimesNotice(
	        "The moving unit"
	        HasOwner unit,
	        "How fast the unit is moving"
	        Speed speed,
	        "The fixture the unit might be noticing"
	        TileFixture? fixture) {
	    if (exists fixture) {
	        if (unit == fixture) {
	            return false;
	        } else {
	            Integer perception;
	            if (is IUnit unit) {
	                perception = highestPerception(unit);
	            } else {
	                perception = 0;
	            }
	            return (perception + speed.perceptionModifier + 15) >= fixture.dc;
	        }
	    } else {
	        return false;
	    }
	}

	"Get the highest Perception score of any member of the unit"
	todo("This does not properly handle the unusual case of a very unobservant unit")
	Integer highestPerception(IUnit unit) =>
	    unit.narrow<IWorker>().map(getPerception).max(increasing) else 0;
	"Get a worker's Perception score."
	Integer getPerception(IWorker worker) {
	    Integer ability;
	    if (exists stats = worker.stats) {
	        ability = WorkerStats.getModifier(stats.wisdom);
	    } else {
	        ability = 0;
	    }
	    Integer ranks = worker.flatMap(identity)
	        .filter(matchingPredicate(matchingValue("perception", String.lowercased),
				ISkill.name))
	        .map(ISkill.level).reduce(plus) else 0;
	    return ability + (ranks * 2);
	}

	"Whether the unit should always notice the given fixture. A null fixture is never
	 noticed."
	todo("""Very-observant units should "always" notice some things that others might
	        "sometimes" notice.""")
	shared Boolean shouldAlwaysNotice(HasOwner unit, TileFixture? fixture) {
        if (is ITownFixture fixture) {
            return fixture.owner == unit.owner;
        } else {
            return fixture is Hill|Forest;
        }
	}

	"Choose what the mover should in fact find from the list of things he or she might find.
	 Since some callers need to have a list of Pairs instead of TileFixtures, we take a
	 function for getting the fixtures out of the list."
	shared {Element*} selectNoticed<Element>({Element*} possibilities,
	        TileFixture(Element) getter, IUnit mover, Speed speed) {
	    {Element*} local = randomize(possibilities);
	    variable Integer perception = highestPerception(mover) + speed.perceptionModifier;
	    MutableList<Element> retval = ArrayList<Element>();
	    for (item in local) {
	        Integer dc = getter(item).dc;
	        if (singletonRandom.nextElement(1..20) + perception >= dc) {
	            retval.add(item);
	            perception -= 5;
	        }
	    }
	    return retval.sequence();
	}
}
"An exception thrown to signal traversal is impossible."
todo("Ocean isn't impassable to everything, of course.") // FIXME
shared class TraversalImpossibleException()
        extends Exception("Traversal is impossible.") {}
