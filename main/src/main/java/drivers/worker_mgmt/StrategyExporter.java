package drivers.worker_mgmt;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import common.map.fixtures.towns.IFortress;
import java.io.Writer;
import java.io.BufferedWriter;
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
import common.map.Player;
import common.map.HasName;
import common.map.fixtures.Implement;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.UnitMember;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalPlurals;
import common.map.fixtures.mobile.worker.IJob;

/**
 * A class to write a proto-strategy to file.
 */
/* package */ class StrategyExporter implements PlayerChangeListener {
	public StrategyExporter(final IWorkerModel model, final SPOptions options) {
		this.model = model;
		this.options = options;
		currentPlayer = model.getCurrentPlayer();
	}

	private final IWorkerModel model;
	private final SPOptions options;

	private Player currentPlayer;

	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		currentPlayer = newPlayer;
	}

	private static String jobString(final IJob job) {
		return String.format("%s %d", job.getName(), job.getLevel());
	}

	private void writeMember(final Writer writer, @Nullable final UnitMember member) throws IOException {
		if (member instanceof IWorker) {
			IWorker worker = (IWorker) member;
			writer.write(worker.getName());
			List<IJob> jobs;
			if (options.hasOption("--include-unleveled-jobs")) {
				jobs = StreamSupport.stream(worker.spliterator(), false)
						.filter(j -> !j.isEmpty())
						.collect(Collectors.toList());
			} else {
				jobs = StreamSupport.stream(worker.spliterator(), false)
						.filter(j -> j.getLevel() > 0)
						.collect(Collectors.toList());
			}
			if (!jobs.isEmpty()) {
				writer.write(jobs.stream().map(StrategyExporter::jobString)
					.collect(Collectors.joining(", ", " (", ")")));
			}
		} else if (member instanceof Animal) {
			Animal animal = (Animal) member;
			if (animal.getPopulation() > 1) {
				writer.write(String.format("%d ", animal.getPopulation()));
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
				writer.write(String.format(" (born turn %d)", animal.getBorn()));
			}
		} else if (member != null) {
			writer.write(member.toString());
		}
	}

	private void summarizeUnitMembers(final Writer writer, final IUnit unit) throws IOException {
		List<IWorker> leveledWorkers = new ArrayList<>();
		List<UnitMember> nonWorkers = new ArrayList<>();
		List<IWorker> unleveledWorkers = new ArrayList<>();
		for (UnitMember member : unit) {
			if (member instanceof IWorker) {
				if (StreamSupport.stream(((IWorker) member).spliterator(), false)
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
				writeMember(writer, unleveledWorkers.get(0));
				if (unleveledWorkers.size() > 1) {
					writer.write(String.format(", %d other unleveled workers",
						unleveledWorkers.size() - 1));
				}
				needComma = true;
			}
		} else {
			needComma = true;
			boolean first = true;
			for (IWorker worker : leveledWorkers) {
				if (first) {
					first = false;
				} else {
					writer.write(", ");
				}
				writeMember(writer, worker);
			}
			if (!unleveledWorkers.isEmpty()) {
				writer.write(String.format(", %d unleveled workers",
					unleveledWorkers.size()));
			}
		}
		for (UnitMember member : nonWorkers) {
			if (needComma) {
				writer.write(", ");
			}
			writeMember(writer, member);
			needComma = true;
		}
	}

	private static String workerString(@Nullable final UnitMember member) {
		if (member instanceof HasName) {
			return ((HasName) member).getName();
		} else if (member == null) {
			return "";
		} else {
			return member.toString();
		}
	}

	public void writeStrategy(final Path path, final Iterable<UnitMember> dismissed) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			String playerName = currentPlayer.getName();
			int turn = model.getMap().getCurrentTurn();
			Collection<IUnit> units = model.getUnits(currentPlayer);
			Map<String, List<IUnit>> unitsByKind = new HashMap<>(); // TODO: Use multimap (as we did in Ceylon)
			for (IUnit unit : units) {
				if (unit.iterator().hasNext() ||
						"true".equals(options.getArgument("--print-empty"))) {
					List<IUnit> list;
					if (unitsByKind.containsKey(unit.getKind())) {
						list = unitsByKind.get(unit.getKind());
					} else {
						list = new ArrayList<>();
						unitsByKind.put(unit.getKind(), list);
					}
					list.add(unit);
				}
			}
			Map<IUnit, String> orders = new HashMap<>();
			for (IUnit unit : unitsByKind.entrySet().stream()
					.flatMap(e -> e.getValue().stream())
					.collect(Collectors.toList())) {
				String unitOrders = unit.getLatestOrders(turn);
				int ordersTurn = unit.getOrdersTurn(unitOrders);
				if (unitOrders.equals(unit.getOrders(turn)) || ordersTurn < 0) {
					orders.put(unit, unitOrders);
				} else {
					orders.put(unit, String.format("(From turn #%d) %s", ordersTurn,
						unitOrders));
				}
			}
			writer.write("[Player: ");
			writer.write(playerName);
			writer.newLine();
			if (currentPlayer.getCountry() != null &&
					!currentPlayer.getCountry().trim().isEmpty()) {
				writer.write("Country: ");
				writer.write(currentPlayer.getCountry());
				writer.newLine();
			}
			writer.write(String.format("Turn %d]", turn));
			writer.newLine();
			writer.newLine();
			writer.write("Inventions: TODO: any?");
			writer.newLine();
			writer.newLine();

			if (dismissed.iterator().hasNext()) {
				writer.write("Dismissed workers etc.: ");
				writer.write(StreamSupport.stream(dismissed.spliterator(), false)
					.map(StrategyExporter::workerString)
					.collect(Collectors.joining(", ")));
				writer.newLine();
				writer.newLine();
			}

			writer.write("Workers:");
			writer.newLine();
			writer.newLine();
			for (Map.Entry<String, List<IUnit>> entry : unitsByKind.entrySet()) {
				String kind = entry.getKey();
				List<IUnit> list = entry.getValue();
				if (list.isEmpty()) {
					continue;
				}
				writer.write("* ");
				writer.write(kind);
				writer.write(":");
				writer.newLine();
				for (IUnit unit : list) {
					writer.write("  - ");
					writer.write(unit.getName());
					if (unit.iterator().hasNext()) {
						writer.write(" [");
						if (unit.stream().count() > 4L &&
								"true".equals(options
									.getArgument(
										"--summarize-large-units"))) {
							summarizeUnitMembers(writer, unit);
						} else {
							boolean first = true;
							for (UnitMember member : unit) {
								if (first) {
									first = false;
								} else {
									writer.write(", ");
								}
								writeMember(writer, member);
							}
						}
						writer.write("]");
					}
					if (options.hasOption("--results")) {
						writer.write(": ");
						if (orders.containsKey(unit) &&
								!orders.get(unit).isEmpty()) {
							writer.write(orders.get(unit));
						} else {
							writer.write("TODO");
						}
						writer.newLine();
						writer.newLine();
						String results = unit.getResults(turn);
						if (results.isEmpty()) {
							writer.write("TODO: run");
						} else {
							writer.write(results);
						}
						writer.newLine();
						writer.newLine();
						writer.write("TODO: advancement"); // TODO: Omit this nowadays?
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
				writer.write("Resources:");
				writer.newLine();
				writer.newLine();
				for (IFortress fortress : model.getFortresses(currentPlayer)) {
					writer.write("- In ");
					writer.write(fortress.getName());
					writer.newLine();
					List<Implement> equipment =
						fortress.stream().filter(Implement.class::isInstance)
							.map(Implement.class::cast)
							.collect(Collectors.toList());
					if (!equipment.isEmpty()) {
						writer.write("  - Equipment not in a unit:");
						writer.newLine();
						for (Implement item : equipment) { // TODO: move out of if?
							writer.write("    - ");
							writer.write(item.toString()); // FIXME: This is egregiously verbose ("An implement of kind ...")
							writer.newLine();
						}
					}
					for (Map.Entry<String, List<IResourcePile>> entry :
							fortress.stream().filter(IResourcePile.class::isInstance)
								.map(IResourcePile.class::cast)
								.collect(Collectors.groupingBy(
									IResourcePile::getKind))
								.entrySet()) {
						String kind = entry.getKey();
						List<IResourcePile> resources = entry.getValue();
						writer.write("  - ");
						writer.write(kind);
						writer.write(":");
						writer.newLine();
						for (IResourcePile pile : resources) {
							writer.write("    - ");
							writer.write(pile.toString()); // FIXME: This is egregiously verbose ("A pile of ...")
							writer.newLine();
						}
					}
					if (!fortress.stream().allMatch(f -> f instanceof Implement ||
								f instanceof IResourcePile ||
								f instanceof IUnit)) {
						System.err.printf("Unhandled members in %s%n",
							fortress.getName()); // TODO: Take ICLIHelper to report diagnostics on
					}
				}
				for (IUnit unit : model.getUnits(currentPlayer)) {
					if (unit.stream().allMatch(IWorker.class::isInstance)) {
						continue;
					}
					writer.write("- With unit ");
					writer.write(unit.getName());
					writer.write(" (");
					writer.write(unit.getKind());
					writer.write("):");
					writer.newLine();
					for (UnitMember member : unit) {
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
