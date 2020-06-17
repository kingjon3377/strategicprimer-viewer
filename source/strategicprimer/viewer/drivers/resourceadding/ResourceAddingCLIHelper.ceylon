import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures {
    Quantity,
    ResourcePile,
    Implement
}
import com.vasileff.ceylon.structures {
    MutableMultimap,
    HashMultimap
}
import ceylon.collection {
    MutableSet,
    MutableMap,
    HashSet,
    HashMap
}
"Logic extracted from [[ResourceAddingCLI]] that's also useful in the turn-running app."
shared class ResourceAddingCLIHelper(ICLIHelper cli, IDRegistrar idf) {
    MutableSet<String> resourceKinds = HashSet<String>();
    MutableMultimap<String, String> resourceContents =
        HashMultimap<String, String>();
    MutableMap<String, String> resourceUnits = HashMap<String, String>();

    "Ask the user to choose or enter a resource kind. Returns [[null]] on EOF."
    String? getResourceKind() {
        if (exists retval = cli.chooseStringFromList(resourceKinds.sequence(),
                "Possible kinds of resources:", "No resource kinds entered yet",
                "Chosen kind: ", false).item) {
            return retval;
        } else if (exists retval = cli.inputString("Resource kind to use: ")) {
            if (retval.empty) {
                return null;
            }
            resourceKinds.add(retval);
            return retval;
        } else {
            return null;
        }
    }

    "Ask the user to choose or enter a resource-content-type for a given resource kind.
     Returns [[null]] on EOF."
    String? getResourceContents(String kind) {
        if (exists item = cli.chooseStringFromList(resourceContents.get(kind).sequence(),
                "Possible resources in the ``kind`` category:",
                "No resources entered yet", "Choose resource: ", false).item) {
            return item;
        } else if (exists retval = cli.inputString("Resource to use: ")) {
            if (retval.empty) {
                return null;
            }
            resourceContents.put(kind, retval);
            return retval;
        } else {
            return null;
        }
    }

    "Ask the user to choose units for a type of resource. Returns [[null]] on EOF."
    String? getResourceUnits(String resource) {
        if (exists unit = resourceUnits[resource]) {
            switch (cli.inputBooleanInSeries(
                "Is ``unit`` the correct unit for ``resource``? ",
                "correct;``unit``;``resource``"))
            case (true) { return unit; }
            case (null) { return null; }
            case (false) {}
        }
        if (exists retval = cli.inputString("Unit to use for ``resource``: ")) {
            if (retval.empty) {
                return null;
            }
            resourceUnits[resource] = retval;
            return retval;
        } else {
            return null;
        }
    }

    "Ask the user to enter a resource, which is returned; [[null]] is returned on EOF."
    shared ResourcePile? enterResource() {
        if (exists kind = getResourceKind(),
                exists origContents = getResourceContents(kind),
                exists units = getResourceUnits(origContents),
                exists usePrefix = cli.inputBooleanInSeries(
                    "Qualify the particular resource with a prefix?",
                    "prefix " + origContents)) {
            String contents;
            if (usePrefix) {
                if (exists prefix = cli.inputString("Prefix to use: "), !prefix.empty) {
                    contents = prefix + " " + origContents;
                } else {
                    return null;
                }
            } else {
                contents = origContents;
            }
            if (exists quantity = cli.inputDecimal("Quantity in ``units``?"),
                    quantity.positive) {
                return ResourcePile(idf.createID(), kind, contents, Quantity(quantity,
                    units));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    "Ask the user to enter an Implement (a piece of equipment), which is returned;
     [[null]] is returned on EOF."
    shared Implement? enterImplement() {
        if (exists kind = cli.inputString("Kind of equipment: "), !kind.empty,
                exists multiple = cli.inputBooleanInSeries("Add more than one? ")) {
            Integer count;
            if (multiple) {
                if (exists temp = cli.inputNumber("Number to add: ")) {
                    count = temp;
                } else {
                    return null;
                }
            } else {
                count = 1;
            }
            if (count >= 1) {
                return Implement(kind, idf.createID(), count);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
