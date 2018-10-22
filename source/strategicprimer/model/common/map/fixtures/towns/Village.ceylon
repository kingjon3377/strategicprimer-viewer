import strategicprimer.model.common.map {
    IFixture,
    HasMutableImage,
    Subsettable,
    Player
}
import lovelace.util.common {
    anythingEqual,
    todo
}

"A village in the map."
todo("We'd like to be able to have
      [[Worker|strategicprimer.model.common.map.fixtures.mobile::Worker]]
      members (directly or in [[CommunityStats]]) to represent villagers that
      players have been informed about by name.")
shared class Village(status, name, id, owner, race)
        satisfies ITownFixture&HasMutableImage&IFixture&Subsettable<IFixture> {
    "The status of the village."
    shared actual TownStatus status;

    "The name of the village."
    shared actual String name;

    "The ID number."
    shared actual Integer id;

    "The player the village has pledged to serve and support, if any."
    shared actual variable Player owner;

    "The dominant race of the village."
    shared variable String race;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    "The default-icon filename."
    shared actual String defaultImage = "village.png";

    "A filename of an image to use as a portrait of the village."
    shared actual variable String portrait = "";

    "The contents of the village."
    shared actual variable CommunityStats? population = null;

    "A short description of the village."
    shared actual String shortDescription {
        StringBuilder builder = StringBuilder();
        if (owner.independent) {
            builder.append("Independent ");
        }
        builder.append("``status.string`` village");
        if (!name.empty) {
            builder.append(" named ``name``");
        }
        if (owner.current) {
            builder.append(", owned by you");
        } else if (!owner.independent) {
            builder.append(", owned by ``owner.name``");
        }
        return builder.string;
    }

    shared actual String string => shortDescription;

    "An object is equal if it is a Village with the same status, ID, name, race, and
     owner."
    shared actual Boolean equals(Object obj) {
        if (is Village obj) { // TODO: combine if statements
            if (status == obj.status && name == obj.name && id == obj.id &&
                    owner == obj.owner && race == obj.race) {
                return anythingEqual(population, obj.population);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    "Use the ID number for hashing."
    shared actual Integer hash => id;

    "If we ignore ID, a fixture is equal iff it is a Village with the same status, owner,
     and race."
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Village fixture) { // TODO: combine if statements
            if (status == fixture.status && name == fixture.name &&
                    owner == fixture.owner) {
                return anythingEqual(population, fixture.population);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    "All villages are small."
    shared actual TownSize townSize => TownSize.small;

    shared actual String plural = "Villages";

    """A village is a "subset" of another if they are identical, or if the only difference
        is that the "subset" is independent and the "superset" owes allegiance to some
        player."""
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (is Village obj) {
            if (id != obj.id) {
                report("IDs differ");
            } else if (status != obj.status) {
                report("In village (ID #``id``):\tVillage status differs");
            } else if (name != obj.name) {
                report("In village (ID #``id``):\tVillage name differs");
            } else if (race != obj.race) {
                report("In village ``name`` (ID #``id``):\tDominant race differs");
            } else if (owner.playerId != obj.owner.playerId, !obj.owner.independent) {
                report("In village ``name`` (ID #``id``):\tOwners differ");
            } else if (exists temp = population) {
                return temp.isSubset(obj.population,
                            (st) => report("In village ``name`` (ID #``id``):\t``st``"));
            } else if (obj.population exists) {
                report("In village ``name`` (ID #``id``):\tHas extra population details");
            } else {
                return true;
            }
            return false;
        } else {
            report("Incompatible type to Village");
            return false;
        }
    }

    shared actual String kind => "village";

    "The required Perception check to find the village."
    shared actual Integer dc {
        if (TownStatus.active == status) {
            if (exists temp = population) {
                if (temp.population < 10) {
                    return 20;
                } else if (temp.population < 15) {
                    return 17;
                } else if (temp.population < 20) {
                    return 15;
                } else if (temp.population < 50) {
                    return 12;
                } else if (temp.population < 100) {
                    return 10;
                } else {
                    return 5;
                }
            } else {
                return 15;
            }
        } else {
            return 30;
        }
    }

    "Clone the object."
    shared actual Village copy(Boolean zero) {
        Village retval = Village(status, name, id, owner, race);
        retval.image = image;
        if (!zero) {
            retval.portrait = portrait;
            retval.population = population;
        }
        return retval;
    }
}
