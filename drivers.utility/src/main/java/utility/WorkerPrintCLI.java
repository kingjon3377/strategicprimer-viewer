package utility;

import common.map.Player;
import java.util.stream.StreamSupport;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.List;
import drivers.common.ReadOnlyDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;

import drivers.common.cli.ICLIHelper;

import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.WorkerStats;

import exploration.common.IExplorationModel;

import java.util.Collections;
import java.util.Arrays;

/**
 * A driver to print a mini-report on workers, suitable for inclusion in a player's results.
 */
/* package */ class WorkerPrintCLI implements  ReadOnlyDriver {
	private static List<String> statLabelArray = Collections.unmodifiableList(Arrays.asList("Str",
		"Dex", "Con", "Int", "Wis", "Cha"));

	private static String jobString(IJob job) {
		return String.format("%s %d", job.getName(), job.getLevel());
	}

	private final ICLIHelper cli;
	private final IExplorationModel model;

	@Override
	public IExplorationModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	public WorkerPrintCLI(ICLIHelper cli, IExplorationModel model) {
		this.cli = cli;
		this.model = model;
	}

	private void printWorkers(IUnit unit) {
		for (IWorker worker : StreamSupport.stream(unit.spliterator(), false)
				.filter(IWorker.class::isInstance).map(IWorker.class::cast)
				.collect(Collectors.toList())) { // TODO: avoid collector
			cli.print("- ", worker.getName());
			if (!"human".equals(worker.getRace())) {
				cli.print(" (", worker.getRace(), ")");
			}

			List<IJob> jobs = StreamSupport.stream(worker.spliterator(), false)
				.filter(j -> j.getLevel() > 0).collect(Collectors.toList());
			if (!jobs.isEmpty()) {
				cli.print(" (",
					jobs.stream().map(WorkerPrintCLI::jobString)
						.collect(Collectors.joining(", ")), ")");
			}

			WorkerStats stats = worker.getStats();
			if (stats != null) {
				List<String> statsArray = IntStream.of(stats.array())
					.mapToObj(WorkerStats::getModifierString)
					.collect(Collectors.toList());
				cli.print(" [");
				// TODO: convert to iterator-based loop?
				for (int i = 0; i < statLabelArray.size() && i < statsArray.size(); i++) {
					if (i != 0) {
						cli.print(", ");
					}
					cli.print(statLabelArray.get(i), " ", statsArray.get(i));
				}
				cli.print("]");
			}
			cli.println();
		}
	}

	@Override
	public void startDriver() {
		Player player = cli.chooseFromList(StreamSupport.stream(model.getPlayerChoices()
				.spliterator(), false).collect(Collectors.toList()),
			"Players in the map:", "No players", "Player owning the unit:", false).getValue1();
		if (player != null) {
			IUnit unit = cli.chooseFromList(
				StreamSupport.stream(model.getUnits(player).spliterator(), false)
					.collect(Collectors.toList()),
				"Units of that player:", "No units", "Unit to print:", false).getValue1();
			if (unit != null) {
				printWorkers(unit);
			}
		}
	}
}