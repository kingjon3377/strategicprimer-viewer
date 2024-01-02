package utility;

import legacy.map.Player;
import legacy.map.fixtures.UnitMember;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.StreamSupport;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.List;

import drivers.common.ReadOnlyDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;

import drivers.common.cli.ICLIHelper;

import legacy.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.WorkerStats;

import exploration.common.IExplorationModel;

/**
 * A driver to print a mini-report on workers, suitable for inclusion in a player's results.
 */
/* package */ class WorkerPrintCLI implements ReadOnlyDriver {
    private static final List<String> statLabelArray = List.of("Str", "Dex", "Con", "Int", "Wis", "Cha");

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

            if (worker.getMount() != null) {
                cli.print(" (mounted on ", worker.getMount().getKind(), ")");
            }

            final List<IJob> jobs = StreamSupport.stream(worker.spliterator(), false)
                    .filter(j -> j.getLevel() > 0).toList();
            if (!jobs.isEmpty()) {
                cli.print(" (",
                        jobs.stream().map(WorkerPrintCLI::jobString)
                                .collect(Collectors.joining(", ")), ")");
            }

            final WorkerStats stats = worker.getStats();
            if (stats != null) {
                final Iterator<String> statsIterator = IntStream.of(stats.array())
                        .mapToObj(WorkerStats::getModifierString).iterator();
                final Iterator<String> labelIterator = statLabelArray.iterator();
                boolean first = true;
                while (labelIterator.hasNext() && statsIterator.hasNext()) {
                    if (first) {
                        cli.print(" [");
                        first = false;
                    } else {
                        cli.print(", ");
                    }
                    cli.print(labelIterator.next(), " ", statsIterator.next());
                }
                cli.print("]");
            }
            cli.println();
        }
    }

    @Override
    public void startDriver() {
        final Player player = cli.chooseFromList((List<? extends Player>) new ArrayList<>(model.getPlayerChoices()), "Players in the map:", "No players", "Player owning the unit:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
        if (player != null) {
            final IUnit unit = cli.chooseFromList((List<? extends IUnit>) model.getUnits(player), "Units of that player:", "No units", "Unit to print:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
            if (unit != null) {
                printWorkers(unit);
            }
        }
    }
}
