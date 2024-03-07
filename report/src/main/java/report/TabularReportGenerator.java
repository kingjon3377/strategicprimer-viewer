package report;

import lovelace.util.LovelaceLogger;
import lovelace.util.ThrowingFunction;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;

import lovelace.util.DelayedRemovalMap;
import lovelace.util.Platform;
import lovelace.util.BorderedPanel;
import lovelace.util.ThrowingConsumer;

import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.TerrainFixture;
import report.generators.tabular.UnitTabularReportGenerator;
import report.generators.tabular.FortressTabularReportGenerator;
import report.generators.tabular.AnimalTabularReportGenerator;
import report.generators.tabular.WorkerTabularReportGenerator;
import report.generators.tabular.VillageTabularReportGenerator;
import report.generators.tabular.TownTabularReportGenerator;
import report.generators.tabular.CropTabularReportGenerator;
import report.generators.tabular.DiggableTabularReportGenerator;
import report.generators.tabular.ResourceTabularReportGenerator;
import report.generators.tabular.ImmortalsTabularReportGenerator;
import report.generators.tabular.ExplorableTabularReportGenerator;
import report.generators.tabular.SkillTabularReportGenerator;
import report.generators.tabular.ITableGenerator;

import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;

import drivers.common.cli.ICLIHelper;

import java.util.function.BiConsumer;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Arrays;

/**
 * A collection of methods to produce tabular reports for players.
 *
 * TODO: Dynamically detect generators using service discovery, instead of enumerating them here.
 */
public final class TabularReportGenerator {
	private TabularReportGenerator() {
	}

	/**
	 * A method to produce tabular reports based on a map for a player.
	 *
	 * Unfortunately, 'source' needs to take ThrowingConsumer because console and file I/O can fail.
	 */
	public static void createTabularReports(
			final ILegacyMap map,
			final ThrowingFunction<String, ThrowingConsumer<String, IOException>, IOException> source,
			final ICLIHelper cli) throws IOException {
		final Player player = map.getCurrentPlayer();
		final @Nullable Point hq = ReportGeneratorHelper.findHQ(map, player);
		createTabularReports(map, source, cli, hq);
	}

	/**
	 * A method to produce tabular reports based on a map for a player.
	 *
	 * Unfortunately, 'source' needs to take ThrowingConsumer because console and file I/O can fail.
	 */
	public static void createTabularReports(
			final ILegacyMap map,
			final ThrowingFunction<String, ThrowingConsumer<String, IOException>, IOException> source,
			final ICLIHelper cli, final @Nullable Point hq) throws IOException {
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				ReportGeneratorHelper.getFixtures(map);
		final Map<Integer, Integer> parentMap = ReportGeneratorHelper.getParentMap(map);
		final Player player = map.getCurrentPlayer();
		final MapDimensions dimensions = map.getDimensions();
		final int currentTurn = map.getCurrentTurn();
		final List<ITableGenerator<?>> generators = Arrays.asList(
				new FortressTabularReportGenerator(player, hq, dimensions),
				new UnitTabularReportGenerator(player, hq, dimensions),
				new AnimalTabularReportGenerator(hq, dimensions, currentTurn),
				new SkillTabularReportGenerator(),
				new WorkerTabularReportGenerator(hq, dimensions),
				new VillageTabularReportGenerator(player, hq, dimensions),
				new TownTabularReportGenerator(player, hq, dimensions),
				new CropTabularReportGenerator(hq, dimensions),
				new DiggableTabularReportGenerator(hq, dimensions),
				new ResourceTabularReportGenerator(hq, dimensions),
				new ImmortalsTabularReportGenerator(hq, dimensions),
				new ExplorableTabularReportGenerator(player, hq, dimensions));
		for (final ITableGenerator<?> generator : generators) {
			generator.produceTable(source.apply(generator.getTableName()), fixtures, parentMap);
		}
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			final IFixture fixture = pair.getValue1();
			if (fixture instanceof TerrainFixture) {
				fixtures.remove(fixture.getId());
			} else {
				LovelaceLogger.warning("Unhandled fixture:\t%s", fixture);
				cli.print("Unhandled fixture:\t");
				cli.println(fixture.toString());
			}
		}
	}

	private static final NumberFormat NUM_FORMAT = NumberFormat.getInstance();

	/**
	 * A comparison method for two things that might be {@link String}
	 * representations of numbers, to sort numerically if possible and
	 * otherwise lexicographically.
	 *
	 * TODO: Provide a richer model so this becomes unnecessary
	 */
	private static int sorter(final Object one, final Object two) {
		final String actualOne;
		final String actualTwo;
		if (one instanceof final String s) {
			actualOne = s;
		} else {
			actualOne = one.toString();
		}
		if (two instanceof final String s) {
			actualTwo = s;
		} else {
			actualTwo = two.toString();
		}
		try {
			return Double.compare(NUM_FORMAT.parse(actualOne).doubleValue(),
					NUM_FORMAT.parse(actualTwo).doubleValue());
		} catch (final Exception except) {
			return actualOne.compareTo(actualTwo);
		}
	}

	/**
	 * A method to produce tabular reports and add them to a GUI.
	 *
	 * @param consumer The way to add the tables to the GUI
	 * @param map      The map to base the reports on
	 */
	public static void createGUITabularReports(final BiConsumer<String, Component> consumer, final ILegacyMap map)
			throws IOException {
		final Player player = map.getCurrentPlayer();
		final @Nullable Point hq = ReportGeneratorHelper.findHQ(map, player);
		createGUITabularReports(consumer, map, hq);
	}

	/**
	 * A method to produce tabular reports and add them to a GUI.
	 *
	 * @param consumer The way to add the tables to the GUI
	 * @param map      The map to base the reports on
	 * @param hq       the point to count distances from
	 */
	public static void createGUITabularReports(final BiConsumer<String, Component> consumer,
											   final ILegacyMap map, final @Nullable Point hq) throws IOException {
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
				ReportGeneratorHelper.getFixtures(map);
		final Map<Integer, Integer> parentMap = ReportGeneratorHelper.getParentMap(map);
		final Player player = map.getCurrentPlayer();
		final MapDimensions dimensions = map.getDimensions();
		final int currentTurn = map.getCurrentTurn();
		final List<ITableGenerator<?>> generators = Arrays.asList(
				new FortressTabularReportGenerator(player, hq, dimensions),
				new UnitTabularReportGenerator(player, hq, dimensions),
				new AnimalTabularReportGenerator(hq, dimensions, currentTurn),
				new SkillTabularReportGenerator(),
				new WorkerTabularReportGenerator(hq, dimensions),
				new VillageTabularReportGenerator(player, hq, dimensions),
				new TownTabularReportGenerator(player, hq, dimensions),
				new CropTabularReportGenerator(hq, dimensions),
				new DiggableTabularReportGenerator(hq, dimensions),
				new ResourceTabularReportGenerator(hq, dimensions),
				new ImmortalsTabularReportGenerator(hq, dimensions),
				new ExplorableTabularReportGenerator(player, hq, dimensions));
		for (final ITableGenerator<?> generator : generators) {
			final TableModel tableModel = generator.produceTableModel(fixtures, parentMap);
			final JTable table = new JTable(tableModel);
			final TableRowSorter<TableModel> modelSorter = new TableRowSorter<>(tableModel);
			int i = 0;
			for (final String column : generator.getHeaderRow()) {
				if ("distance".equalsIgnoreCase(column)) {
					modelSorter.setComparator(i, TabularReportGenerator::sorter);
				}
				i++;
			}
			table.setRowSorter(modelSorter);
			final int vertControl;
			final int horizControl;
			if (Platform.SYSTEM_IS_MAC) {
				vertControl = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
				horizControl = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
			} else {
				vertControl = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
				horizControl = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
			}
			consumer.accept(generator.getTableName(), new JScrollPane(table, vertControl,
					horizControl));
		}
		final List<String> unhandled = new ArrayList<>();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			final IFixture fixture = pair.getValue1();
			if (fixture instanceof TerrainFixture) {
				fixtures.remove(fixture.getId());
			} else {
				unhandled.add(fixture.toString());
			}
		}
		if (!unhandled.isEmpty()) {
			consumer.accept("other", BorderedPanel.verticalPanel(
					new JLabel("Fixtures not covered in any of the reports:"),
					new JList<>(unhandled.toArray(String[]::new)), null));
		}
	}
}
