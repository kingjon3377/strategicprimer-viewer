package utility;

import common.map.Player;
import common.map.fixtures.UnitMember;
import java.util.ArrayList;
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
	private static final List<String> statLabelArray = Collections.unmodifiableList(Arrays.asList("Str",
		"Dex", "Con", "Int", "Wis", "Cha"));

	private static String jobString(final IJob job) {
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

	public WorkerPrintCLI(final ICLIHelper cli, final IExplorationModel model) {
		this.cli = cli;
		this.model = model;
	}

	private void printWorkers(final IUnit unit) {
		for (final UnitMember member : unit) {
			final IWorker worker;
			if (member instanceof IWorker) {
				worker = (IWorker) member;
			} else {
				continue;
			}
			cli.print("- ", worker.getName());
			if (!"human".equals(worker.getRace())) {
				cli.print(" (", worker.getRace(), ")");
			}

			final List<IJob> jobs = StreamSupport.stream(worker.spliterator(), false)
				.filter(j -> j.getLevel() > 0).collect(Collectors.toList());
			if (!jobs.isEmpty()) {
				cli.print(" (",
					jobs.stream().map(WorkerPrintCLI::jobString)
						.collect(Collectors.joining(", ")), ")");
			}

			final WorkerStats stats = worker.getStats();
			if (stats != null) {
				final List<String> statsArray = IntStream.of(stats.array())
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
		final Player player = cli.chooseFromList((List<? extends Player>) new ArrayList<Player>(model.getPlayerChoices()), "Players in the map:", "No players", "Player owning the unit:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
		if (player != null) {
			final IUnit unit = cli.chooseFromList((List<? extends IUnit>) model.getUnits(player), "Units of that player:", "No units", "Unit to print:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (unit != null) {
				printWorkers(unit);
			}
		}
	}
}
