package drivers.resourceadding;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Implement;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Logic extracted from {@link ResourceAddingCLI} that's also useful in the turn-running app.
 */
public class ResourceAddingCLIHelper {
    public ResourceAddingCLIHelper(final ICLIHelper cli, final IDRegistrar idf) {
        this.cli = cli;
        this.idf = idf;
    }

    private final ICLIHelper cli;
    private final IDRegistrar idf;

    private final Set<String> resourceKinds = new HashSet<>();
    // TODO: Use a multimap once we get a handle on a suitable library
    private final Map<String, List<String>> resourceContents = new HashMap<>();
    private final Map<String, String> resourceUnits = new HashMap<>();

    /**
     * Ask the user to choose or enter a resource kind. Returns null on EOF.
     */
    private @Nullable String getResourceKind() {
        final String one = cli.chooseStringFromList(new ArrayList<>(resourceKinds),
                "Possible kinds of resources:", "No resource kinds entered yet",
                "Chosen kind: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
        if (one != null) {
            return one;
        }
        final String two = cli.inputString("Resource kind to use: ");
        if (two == null || two.isEmpty()) {
            return null;
        }
        resourceKinds.add(two);
        return two;
    }

    /**
     * Ask the user to choose or enter a resource-content-type for a given
     * resource kind. Returns null on EOF.
     */
    private @Nullable String getResourceContents(final String kind) {
        final List<String> list = resourceContents.getOrDefault(kind, new ArrayList<>());
        final String one = cli.chooseStringFromList(list,
                String.format("Possible resources in the %s category:", kind),
                "No resources entered yet", "Choose resource: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
        if (one != null) {
            return one;
        }
        final String two = cli.inputString("Resource to use: ");
        if (two == null || two.isEmpty()) {
            return null;
        }
        list.add(two);
        resourceContents.put(kind, list);
        return two;
    }

    /**
     * Ask the user to choose units for a type of resource. Returns null on EOF.
     */
    private @Nullable String getResourceUnits(final String resource) {
        if (resourceUnits.containsKey(resource)) {
            final String unit = resourceUnits.get(resource);
            final Boolean resp = cli.inputBooleanInSeries(
                    String.format("Is %s the correct unit for %s", unit, resource),
                    String.format("correct;%s;%s", unit, resource));
            if (resp == null) {
                return null;
            } else if (resp) {
                return unit;
            }
        }
        // N.B. ICLIHelper trims input before returning
        final String retval = cli.inputString(String.format("Unit to use for %s: ", resource));
        if (retval == null || retval.isEmpty()) {
            return null;
        }
        resourceUnits.put(resource, retval);
        return retval;
    }

    /**
     * Ask the user to enter a resource, which is returned; null is returned on EOF.
     */
    public @Nullable IMutableResourcePile enterResource() {
        final String kind = getResourceKind();
        if (kind == null) {
            return null;
        }
        final String origContents = getResourceContents(kind);
        if (origContents == null) {
            return null;
        }
        final String units = getResourceUnits(origContents);
        if (units == null) {
            return null;
        }
        final Boolean usePrefix = cli.inputBooleanInSeries(
                "Qualify the particular resource with a prefix?", "prefix" + origContents);
        if (usePrefix == null) {
            return null;
        }
        final String contents;
        if (usePrefix) {
            final String prefix = cli.inputString("Prefix to use: ");
            if (prefix == null || prefix.isEmpty()) {
                return null;
            }
            contents = prefix + " " + origContents;
        } else {
            contents = origContents;
        }
        final BigDecimal quantity = cli.inputDecimal(String.format("Quantity in %s?", units));
        if (quantity == null || quantity.signum() < 0) {
            return null;
        }
        final ResourcePileImpl retval = new ResourcePileImpl(idf.createID(), kind, contents,
                new Quantity(quantity, units));
        final Boolean setCreated = cli.inputBooleanInSeries("Set created date?", "created" + origContents);
        if (setCreated == null) {
            return null;
        } else if (setCreated) {
            final Integer created = cli.inputNumber("Turn created?");
            if (created != null) {
                retval.setCreated(created);
            } // IIRC we allow "EOF" here to mitigate mistyped 'y' on previous prompt
        }
        return retval;
    }

    /**
     * Ask the user to enter an Implement (a piece of equipment), which is
     * returned; null is returned on EOF.
     */
    public @Nullable Implement enterImplement() {
        final String kind = cli.inputString("Kind of equipment: ");
        if (kind == null || kind.isEmpty()) {
            return null;
        }
        final Boolean multiple = cli.inputBooleanInSeries("Add more than one? ");
        if (multiple == null) {
            return null;
        }
        final int count;
        if (multiple) {
            final Integer temp = cli.inputNumber("Number to add: ");
            if (temp == null) {
                return null;
            }
            count = temp;
        } else {
            count = 1;
        }
        if (count >= 1) {
            return new Implement(kind, idf.createID(), count);
        } else {
            return null;
        }
    }
}
