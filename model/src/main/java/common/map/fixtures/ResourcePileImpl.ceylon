import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map.fixtures {
    Quantity
}

"A quantity of some kind of resource."
todo("More members?")
// FIXME: Make an interface, with mutator methods split to a mutator interface
shared class ResourcePileImpl(id, kind, contents, quantity)
        satisfies IMutableResourcePile {
    shared actual String plural = "Resource Piles";

    "The ID # of the resource pile."
    shared actual Integer id;

    "What general kind of thing is in the resource pile."
    shared actual String kind;

    "What specific kind of thing is in the resource pile."
    shared actual variable String contents;

    "How much of that thing is in the pile, including units."
    shared actual variable Quantity quantity;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    variable Integer createdTurn = -1;
    "The turn on which the resource was created."
    shared actual Integer created => createdTurn;

    assign created {
        if (created < 0) {
            createdTurn = -1;
        } else {
            createdTurn = created;
        }
    }

    shared actual String defaultImage = "resource.png";

    "Clone the object."
    shared actual ResourcePileImpl copy(Boolean zero) {
        ResourcePileImpl retval = ResourcePileImpl(id, kind, contents, quantity);
        if (!zero) {
            retval.created = created;
        }
        return retval;
    }
}
