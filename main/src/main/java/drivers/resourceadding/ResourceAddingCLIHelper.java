package drivers.resourceadding;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import legacy.map.fixtures.LegacyQuantity;
import org.jspecify.annotations.Nullable;

import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;

import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.ResourcePileImpl;
import legacy.map.fixtures.Implement;

import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Logic extracted from {@link ResourceAddingCLI} that's also useful in the turn-running app.
 */
public final class ResourceAddingCLIHelper {
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
		if (Objects.nonNull(one)) {
			return one;
		}
		final String two = cli.inputString("Resource kind to use: ");
		if (Objects.isNull(two) || two.isBlank()) {
			return null;
		}
		resourceKinds.add(two.trim());
		return two.trim();
	}

	/**
	 * Ask the user to choose or enter a resource-content-type for a given
	 * resource kind. Returns null on EOF.
	 */
	private @Nullable String getResourceContents(final String kind) {
		final List<String> list = resourceContents.getOrDefault(kind, new ArrayList<>());
		final String one = cli.chooseStringFromList(list,
				"Possible resources in the %s category:".formatted(kind),
				"No resources entered yet", "Choose resource: ",
				ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
		if (Objects.nonNull(one)) {
			return one;
		}
		final String two = cli.inputString("Resource to use: ");
		if (Objects.isNull(two) || two.isBlank()) {
			return null;
		}
		list.add(two.trim());
		resourceContents.put(kind, list);
		return two.trim();
	}

	/**
	 * Ask the user to choose units for a type of resource. Returns null on EOF.
	 */
	private @Nullable String getResourceUnits(final String resource) {
		if (resourceUnits.containsKey(resource)) {
			final String unit = resourceUnits.get(resource);
			switch (cli.inputBooleanInSeries(
					"Is %s the correct unit for %s".formatted(unit, resource),
					"correct;%s;%s".formatted(unit, resource))) {
				case YES -> {
					return unit;
				}
				case NO -> { // Do nothing
				}
				case QUIT -> {
					return null;
				}
				case EOF -> { // TODO: Somehow signal EOF to callers
					return null;
				}
			}
		}
		// N.B. ICLIHelper trims input before returning
		final String retval = cli.inputString("Unit to use for %s: ".formatted(resource));
		if (Objects.isNull(retval) || retval.isBlank()) {
			return null;
		}
		resourceUnits.put(resource, retval.trim());
		return retval.trim();
	}

	/**
	 * Ask the user to enter a resource, which is returned; null is returned on EOF.
	 */
	public @Nullable IMutableResourcePile enterResource() {
		final String kind = getResourceKind();
		if (Objects.isNull(kind)) {
			return null;
		}
		final String origContents = getResourceContents(kind);
		if (Objects.isNull(origContents)) {
			return null;
		}
		final String units = getResourceUnits(origContents);
		if (Objects.isNull(units)) {
			return null;
		}
		final String contents;
		switch (cli.inputBooleanInSeries(
				"Qualify the particular resource with a prefix?", "prefix" + origContents)) {
			case EOF -> { // TODO: Somehow signal EOF to callers
				return null;
			}
			case QUIT -> {
				return null;
			}
			case YES -> {
				final String prefix = cli.inputString("Prefix to use: ");
				if (Objects.isNull(prefix) || prefix.isBlank()) {
					return null;
				}
				contents = prefix.trim() + " " + origContents;
			}
			case NO -> contents = origContents;
			default -> throw new IllegalStateException("Exhaustive switch wasn't");
		}
		final BigDecimal quantity = cli.inputDecimal("Quantity in %s?".formatted(units));
		if (Objects.isNull(quantity) || quantity.signum() < 0) {
			return null;
		}
		final IMutableResourcePile retval = new ResourcePileImpl(idf.createID(), kind, contents,
				new LegacyQuantity(quantity, units));
		return switch (cli.inputBooleanInSeries("Set created date?", "created" + origContents)) {
			case YES -> {
				final Integer created = cli.inputNumber("Turn created?");
				// IIRC we allow "EOF" here to mitigate mistyped 'y' on previous prompt
				if (Objects.nonNull(created)) {
					retval.setCreated(created);
				}
				yield retval;
			}
			case NO -> retval;
			case QUIT -> retval; // TODO: Somehow signal to callers
			case EOF -> null; // TODO: Somehow signal EOF to callers
		};
	}

	/**
	 * Ask the user to enter an Implement (a piece of equipment), which is
	 * returned; null is returned on EOF.
	 */
	public @Nullable Implement enterImplement() {
		final String kind = cli.inputString("Kind of equipment: ");
		if (Objects.isNull(kind) || kind.isBlank()) {
			return null;
		}
		final int count;
		switch (cli.inputBooleanInSeries("Add more than one? ")) {
			case YES -> {
				final Integer temp = cli.inputNumber("Number to add: ");
				if (Objects.isNull(temp)) {
					return null;
				}
				count = temp;
			}
			case NO -> count = 1;
			case QUIT -> {
				return null;
			}
			case EOF -> { // TODO: Somehow signal EOF to callers
				return null;
			}
			default -> throw new IllegalStateException("Exhaustive switch wasn't");
		}
		if (count >= 1) {
			return new Implement(kind.trim(), idf.createID(), count);
		} else {
			return null;
		}
	}
}
