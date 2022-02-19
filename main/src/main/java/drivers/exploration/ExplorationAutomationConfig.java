package drivers.exploration;

import common.map.IMapNG;
import common.map.Player;
import common.map.Point;
import common.map.TileFixture;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Immortal;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.Village;
import drivers.common.cli.ICLIHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

/* package */ class ExplorationAutomationConfig {
	public ExplorationAutomationConfig(final Player player) {
		this.player = player;
		conditions = Collections.unmodifiableList(
			Arrays.asList(new Condition<>("at others' fortresses",
							fixture -> "a fortress belonging to " + fixture.getOwner(), IFortress.class),
					new Condition<>("at active towns",
							fixture -> String.format("a %s active %s", fixture.getTownSize(), fixture.getKind()),
							AbstractTown.class, t -> TownStatus.Active == t.getStatus()),
					new Condition<>("at inactive towns",
							fixture -> String.format("a %s %s %s", fixture.getTownSize(),
									fixture.getStatus(), fixture.getKind()),
							AbstractTown.class, t -> TownStatus.Active != t.getStatus()),
					new Condition<>("at independent villages", "an independent village",
							Village.class, v -> v.getOwner().isIndependent()),
					new Condition<>("at other players' villages", "another player's village",
							Village.class, v -> !v.getOwner().equals(this.player), v -> !v.getOwner().isIndependent()),
				// TODO: For "your villages" (and perhaps other towns), include name in stop message
					new Condition<>("at villages sworn to you", "one of your villages",
							Village.class, v -> v.getOwner().equals(this.player)),
					new Condition<>("on meeting other players' units",
							unit -> "a unit belonging to " + unit.getOwner(), IUnit.class,
							u -> !u.getOwner().equals(this.player), u -> !u.getOwner().isIndependent()),
					new Condition<>("on meeting independent units", "an independent unit", IUnit.class,
							// TODO: Provide helper default methods isIndependent() and sameOwner() in HasOwner?
							u -> u.getOwner().isIndependent()),
					new Condition<>("on meeting an immortal", i -> "a(n) " + i.getShortDescription(),
							Immortal.class)));
	}

	private final Player player;

	public Player getPlayer() {
		return player;
	}

	private static class Condition<Type extends TileFixture> {
		/**
		 * @param configExplanation A description to use in the question asking whether to stop for this condition.
		 * @param stopExplanation A description to use when stopping because of this condition.
		 * @param conditions Returns true when a tile fixture matches all of these conditions.
		 */
		@SafeVarargs
		public Condition(final String configExplanation, final String stopExplanation, final Class<Type> cls,
		                 final Predicate<Type>... conditions) {
			this.configExplanation = configExplanation;
			this.stopExplanation = ignored -> stopExplanation;
			this.conditions = Collections.unmodifiableList(Arrays.asList(conditions));
			this.cls = cls;
		}

		/**
		 * @param configExplanation A description to use in the question asking whether to stop for this condition.
		 * @param stopExplanation A factory for a description to use when stopping because of this condition.
		 * @param conditions Returns true when a tile fixture matches all of these conditions.
		 */
		@SafeVarargs
		public Condition(final String configExplanation, final Function<Type, String> stopExplanation,
		                 final Class<Type> cls, final Predicate<Type>... conditions) {
			this.configExplanation = configExplanation;
			this.stopExplanation = stopExplanation;
			this.conditions = Collections.unmodifiableList(Arrays.asList(conditions));
			this.cls = cls;
		}

		private final Class<Type> cls;

		/**
		 * A description to use in the question asking whether to stop for this condition.
		 */
		private final String configExplanation;

		/**
		 * A description to use in the question asking whether to stop for this condition.
		 */
		public String getConfigExplanation() {
			return configExplanation;
		}

		/**
		 * A factory for a description to use when stopping because of this condition.
		 */
		private final Function<Type, String> stopExplanation;

		/**
		 * A factory for a description to use when stopping because of this condition.
		 */
		public Function<Type, String> getStopExplanation() {
			return stopExplanation;
		}

		/**
		 * Returns true when a tile fixture matches all of these conditions.
		 */
		private final List<Predicate<Type>> conditions;

		private boolean allConditions(final TileFixture fixture) {
			if (cls.isInstance(fixture)) {
				for (final Predicate<Type> condition : conditions) {
					if (!condition.test((Type) fixture)) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Returns true when the given tile matches this condition.
		 */
		public boolean matches(final IMapNG map, final Point point) {
			return map.getFixtures(point).stream().anyMatch(this::allConditions);
		}
	}

	private final List<Condition<? extends TileFixture>> conditions;

	@Nullable
	private List<Condition<? extends TileFixture>> enabledConditions = null;

	public boolean stopAtPoint(final ICLIHelper cli, final IMapNG map, final Point point) {
		final List<Condition<? extends TileFixture>> localEnabledConditions;
		if (enabledConditions == null) {
			final List<Condition<? extends TileFixture>> temp = new ArrayList<>();
			for (final Condition<? extends TileFixture> condition : conditions) {
				final Boolean resp = cli.inputBooleanInSeries("Stop for instructions " +
					condition.getConfigExplanation() + "?");
				if (resp == null) {
					// EOF, so we want to abort the caller's loop
					return true;
				} else if (resp) {
					temp.add(condition);
				}
			}
			localEnabledConditions = Collections.unmodifiableList(temp);
			enabledConditions = localEnabledConditions;
		} else {
			localEnabledConditions = enabledConditions;
		}
		final Condition<? extends TileFixture> matchingCondition = localEnabledConditions.stream()
			.filter(c -> c.matches(map, point)).findFirst().orElse(null);
		if (matchingCondition == null) {
			return false;
		} else {
			cli.println(String.format("There is %s here, so the explorer stops.",
					matchingCondition.getStopExplanation()));
			return true;
		}
	}
}
