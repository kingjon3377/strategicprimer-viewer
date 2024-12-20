package drivers.worker_mgmt;

import java.util.Collection;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import legacy.map.fixtures.towns.IFortress;

import java.io.Writer;
import java.io.BufferedWriter;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Files;

import drivers.common.SPOptions;
import drivers.common.PlayerChangeListener;
import drivers.common.IWorkerModel;
import legacy.map.Player;
import legacy.map.HasName;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalPlurals;
import legacy.map.fixtures.mobile.worker.IJob;

/**
 * A class to write a proto-strategy to file.
 */
/* package */ final class StrategyExporter implements PlayerChangeListener {
	private static final long LARGE_UNIT_THRESHOLD = 4L;

	public StrategyExporter(final IWorkerModel model, final SPOptions options) {
		this.model = model;
		this.options = options;
		currentPlayer = model.getCurrentPlayer();
	}

	private final IWorkerModel model;
	private final SPOptions options;

	private Player currentPlayer;

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
		currentPlayer = newPlayer;
	}

	private static String jobString(final IJob job) {
		return "%s %d".formatted(job.getName(), job.getLevel());
	}

	private void writeMember(final Writer writer, final @Nullable UnitMember member) throws IOException {
		switch (member) {
			case final IWorker worker -> {
				writer.write(worker.getName());
				final Predicate<IJob> inclusion;
				if (options.hasOption("--include-unleveled-jobs")) {
					inclusion = j -> !j.isEmpty();
				} else {
					inclusion = j -> j.getLevel() > 0;
				}
				final List<IJob> jobs = StreamSupport.stream(worker.spliterator(), false)
						.filter(inclusion).toList();
				boolean needsClosingParen = false;
				if (Objects.nonNull(worker.getMount())) {
					writer.write(" (on ");
					writer.write(worker.getMount().getKind());
					needsClosingParen = true;
				}
				if (!jobs.isEmpty()) {
					if (needsClosingParen) {
						writer.write("; ");
					} else {
						writer.write(" (");
					}
					writer.write(jobs.stream().map(StrategyExporter::jobString)
							.collect(Collectors.joining(", ")));
					needsClosingParen = true;
				}
				if (needsClosingParen) {
					writer.write(")");
				}
			}
			case final Animal animal -> {
				if (animal.getPopulation() > 1) {
					writer.write("%d ".formatted(animal.getPopulation()));
				}
				if (!"domesticated".equals(animal.getStatus())) {
					writer.write(animal.getStatus());
					writer.write(' ');
				}
				if (animal.getPopulation() > 1) {
					writer.write(AnimalPlurals.get(animal.getKind()));
				} else {
					writer.write(animal.getKind());
				}
				if (animal.getBorn() >= 0) {
					writer.write(" (born turn %d)".formatted(animal.getBorn()));
				}
			}
			case null -> {
			}
			default -> writer.write(member.toString());
		}
	}

	private void summarizeUnitMembers(final Writer writer, final Iterable<UnitMember> unit) throws IOException {
		final Collection<IWorker> leveledWorkers = new ArrayList<>();
		final Collection<UnitMember> nonWorkers = new ArrayList<>();
		final List<IWorker> unleveledWorkers = new ArrayList<>();
		for (final UnitMember member : unit) {
			if (member instanceof final IWorker w) {
				if (StreamSupport.stream(w.spliterator(), false)
						.mapToInt(IJob::getLevel).anyMatch(x -> x > 0)) {
					leveledWorkers.add((IWorker) member);
				} else {
					unleveledWorkers.add((IWorker) member);
				}
			} else {
				nonWorkers.add(member);
			}
		}
		boolean needComma;
		if (leveledWorkers.isEmpty()) {
			if (unleveledWorkers.isEmpty()) {
				needComma = false;
			} else {
				writeMember(writer, unleveledWorkers.getFirst());
				if (unleveledWorkers.size() > 1) {
					writer.write(", %d other unleveled workers".formatted(unleveledWorkers.size() - 1));
				}
				needComma = true;
			}
		} else {
			needComma = true;
			boolean first = true;
			for (final IWorker worker : leveledWorkers) {
				if (first) {
					first = false;
				} else {
					writer.write(", ");
				}
				writeMember(writer, worker);
			}
			if (!unleveledWorkers.isEmpty()) {
				writer.write(", %d unleveled workers".formatted(unleveledWorkers.size()));
			}
		}
		for (final UnitMember member : nonWorkers) {
			if (needComma) {
				writer.write(", ");
			}
			writeMember(writer, member);
			needComma = true;
		}
	}

	private static String workerString(final @Nullable UnitMember member) {
		if (member instanceof final HasName hn) {
			return hn.getName();
		} else if (Objects.isNull(member)) {
			return "";
		} else {
			return member.toString();
		}
	}

	public void writeStrategy(final Path path, final Iterable<UnitMember> dismissed) throws IOException {
		try (final BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			final String playerName = currentPlayer.getName();
			final int turn = model.getMap().getCurrentTurn();
			final Collection<IUnit> units = model.getUnits(currentPlayer);
			final Map<String, List<IUnit>> unitsByKind = new HashMap<>(); // TODO: Use multimap (as we did in Ceylon)
			for (final IUnit unit : units) {
				if (unit.iterator().hasNext() ||
						"true".equals(options.getArgument("--print-empty"))) {
					final List<IUnit> list = unitsByKind.computeIfAbsent(unit.getKind(), _ -> new ArrayList<>());
					list.add(unit);
				}
			}
			final Map<IUnit, String> orders = new HashMap<>();
			for (final IUnit unit : unitsByKind.entrySet().stream()
					.flatMap(e -> e.getValue().stream()).toList()) {
				final String unitOrders = unit.getLatestOrders(turn);
				final int ordersTurn = unit.getOrdersTurn(unitOrders);
				if (unitOrders.equals(unit.getOrders(turn)) || ordersTurn < 0) {
					orders.put(unit, unitOrders);
				} else {
					orders.put(unit, "(From turn #%d) %s".formatted(ordersTurn,
							unitOrders));
				}
			}
			writer.write("[Player: ");
			writer.write(playerName);
			writer.newLine();
			if (Objects.nonNull(currentPlayer.getCountry()) &&
					!currentPlayer.getCountry().isBlank()) {
				writer.write("Country: ");
				writer.write(currentPlayer.getCountry());
				writer.newLine();
			}
			writer.write("Turn %d]".formatted(turn));
			writer.newLine();
			writer.newLine();
			writer.write("## Inventions:");
			writer.newLine();
			writer.newLine();
			writer.write("TODO: any?");
			writer.newLine();
			writer.newLine();

			if (dismissed.iterator().hasNext()) {
				writer.write("## Dismissed workers etc.: ");
				writer.newLine();
				writer.newLine();
				writer.write(StreamSupport.stream(dismissed.spliterator(), false)
						.map(StrategyExporter::workerString)
						.collect(Collectors.joining(", ")));
				writer.newLine();
				writer.newLine();
			}

			writer.write("## Workers:");
			writer.newLine();
			writer.newLine();
			final Predicate<Object> isImplement = Implement.class::isInstance;
			final Function<Object, Implement> implementCast = Implement.class::cast;
			for (final Map.Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
				final String kind = entry.getKey();
				final List<IUnit> list = entry.getValue();
				if (list.isEmpty()) {
					continue;
				}
				writer.write("### ");
				writer.write(kind);
				writer.write(":");
				writer.newLine();
				writer.newLine();
				for (final IUnit unit : list) {
					writer.write("#### ");
					writer.write(unit.getName());
					final boolean alreadyWroteMembers;
					// TODO: Support the 'one person plus equipment/animals' case, & maybe the 'leader and helpers' case
					if (unit.stream().count() == 1L) {
						writer.write(" [");
						writeMember(writer, unit.stream().findAny().orElse(null));
						writer.write("]");
						alreadyWroteMembers = true;
					} else {
						writer.write(":");
						alreadyWroteMembers = false;
					}
					writer.newLine();
					writer.newLine();
					if (unit.iterator().hasNext() && !alreadyWroteMembers) {
						writer.write("- Members: ");
						if (unit.stream().count() > LARGE_UNIT_THRESHOLD &&
								"true".equals(options
										.getArgument(
												"--summarize-large-units"))) {
							summarizeUnitMembers(writer, unit);
						} else {
							boolean first = true;
							for (final UnitMember member : unit) {
								if (first) {
									first = false;
								} else {
									writer.write(", ");
								}
								writeMember(writer, member);
							}
						}
						writer.newLine();
						writer.newLine();
					}
					if (options.hasOption("--results")) {
						writer.write("- Orders: ");
						if (orders.containsKey(unit) &&
								!orders.get(unit).isEmpty()) {
							writer.write(orders.get(unit));
						} else {
							writer.write("TODO");
						}
						writer.newLine();
						writer.newLine();
						final String results = unit.getResults(turn);
						if (results.isEmpty()) {
							writer.write("TODO: run");
						} else {
							writer.write(results);
						}
						writer.newLine();
					} else {
						writer.write(":");
						writer.newLine();
						writer.newLine();
						if (orders.containsKey(unit) &&
								!orders.get(unit).isEmpty()) {
							writer.write(orders.get(unit));
						} else {
							writer.write("TODO");
						}
						writer.newLine();
					}
					writer.newLine();
				}
			}
			if (options.hasOption("--results")) {
				writer.write("## Resources:");
				writer.newLine();
				writer.newLine();
				for (final IFortress fortress : model.getFortresses(currentPlayer)) {
					writer.write("### In ");
					writer.write(fortress.getName());
					writer.newLine();
					writer.newLine();
					final List<Implement> equipment =
							fortress.stream().filter(isImplement)
									.map(implementCast).toList();
					if (!equipment.isEmpty()) {
						writer.write("- Equipment not in a unit:");
						writer.newLine();
						for (final Implement item : equipment) { // TODO: move out of if?
							writer.write("  - ");
							writer.write(item.toString()); // FIXME: Egregiously verbose ("An implement of kind ...")
							writer.newLine();
						}
					}
					for (final Map.Entry<String, List<IResourcePile>> entry :
							fortress.stream().filter(IResourcePile.class::isInstance)
									.map(IResourcePile.class::cast)
									.collect(Collectors.groupingBy(
											IResourcePile::getKind))
									.entrySet()) {
						final String kind = entry.getKey();
						final List<IResourcePile> resources = entry.getValue();
						writer.write("- "); // TODO: Markdown header instead?
						writer.write(kind);
						writer.write(":");
						writer.newLine();
						for (final IResourcePile pile : resources) {
							writer.write("  - ");
							writer.write(pile.toString()); // FIXME: This is egregiously verbose ("A pile of ...")
							writer.newLine();
						}
					}
					if (!fortress.stream().allMatch(f -> f instanceof Implement ||
							f instanceof IResourcePile ||
							f instanceof IUnit)) {
						LovelaceLogger.warning("Unhandled members in %s%n", fortress.getName());
					}
				}
				final Predicate<Object> isWorker = IWorker.class::isInstance;
				for (final IUnit unit : model.getUnits(currentPlayer)) {
					if (unit.stream().allMatch(isWorker)) {
						continue;
					}
					writer.write("- With unit ");
					writer.write(unit.getName());
					writer.write(" (");
					writer.write(unit.getKind());
					writer.write("):");
					writer.newLine();
					for (final UnitMember member : unit) {
						if (!(member instanceof IWorker)) {
							writer.write("  - ");
							writer.write(member.toString());
							writer.newLine();
						}
					}
				}
			}
		}
	}
}
