import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    IFixture,
    Subsettable,
    Point
}

"A fixture representing a portal to another world."
shared class Portal(destinationWorld, destinationCoordinates, id)
        satisfies ExplorableFixture&IFixture&Subsettable<IFixture> {
    "A string identifying the world the portal connects to."
    todo("Should this be mutable?")
    shared String destinationWorld;

    "The coordinates in that world that the portal connects to. If invalid, the coordinate
     needs to be generated, presumably randomly, before any unit traverses the portal."
    todo("Use Null instead of an invalid Point?",
        "Combine with destinationWorld in a Tuple?")
    shared variable Point destinationCoordinates;

    "A unique ID number."
    shared actual Integer id;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    shared actual Portal copy(Boolean zero) {
        Portal retval = Portal((zero) then "unknown" else destinationWorld,
            (zero) then Point.invalidPoint else destinationCoordinates, id);
        retval.image = image;
        return retval;
    }

    shared actual String shortDescription = "A portal to another world";

    shared actual String string => shortDescription;

    shared actual String defaultImage = "portal.png";

    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Portal fixture) {
            return destinationWorld == fixture.destinationWorld &&
                destinationCoordinates == fixture.destinationCoordinates;
        } else {
            return false;
        }
    }

    shared actual Boolean equals(Object obj) {
        if (is Portal obj) {
            return obj.id == id && equalsIgnoringID(obj);
        } else {
            return false;
        }
    }

    shared actual Integer hash => id;

    shared actual String plural = "Portals";

    todo("Test this")
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is Portal obj) {
                Anything(String) localReport =
                        compose(report, "In portal with ID #``id``: ".plus);
                if (![destinationWorld, "unknown"].contains(obj.destinationWorld)) {
                    localReport("Different destination world");
                    return false;
                } else if (obj.destinationCoordinates.valid,
                        destinationCoordinates != obj.destinationCoordinates) {
                    localReport("Different destination coordinates");
                    return false;
                } else {
                    return true;
                }
            } else {
                report("Different kinds of fixtures for ID #``id``");
                return false;
            }
        } else {
            report("Called with different ID #s");
            return false;
        }
    }

    "The required Perception check result for an explorer to find the portal."
    todo("This should probably be variable, i.e. read from XML")
    shared actual Integer dc => 35;
}
