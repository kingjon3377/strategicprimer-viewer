package report;

import lovelace.util.ThrowingFunction;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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

import common.map.Player;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.IMapNG;
import common.map.fixtures.TerrainFixture;
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
import java.util.logging.Logger;
import java.util.function.Function;
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
	private TabularReportGenerator() {}

	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(TabularReportGenerator.class.getName());

	/**
	 * A method to produce tabular reports based on a map for a player.
	 *
	 * TODO: Does the consumer really need to be ThrowingConsumer here either?
	 */
	public static void createTabularReports(IMapNG map,
	        ThrowingFunction<String, ThrowingConsumer<String, IOException>, IOException> source,
			ICLIHelper cli) throws IOException {
		DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
			ReportGeneratorHelper.getFixtures(map);
		Map<Integer, Integer> parentMap = ReportGeneratorHelper.getParentMap(map);
		Player player = map.getCurrentPlayer();
		MapDimensions dimensions = map.getDimensions();
		@Nullable Point hq = ReportGeneratorHelper.findHQ(map, player);
		int currentTurn = map.getCurrentTurn();
		List<ITableGenerator<?>> generators = Arrays.asList(
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
		for (ITableGenerator<?> generator : generators) {
			generator.produceTable(source.apply(generator.getTableName()), fixtures, parentMap);
		}
		for (Pair<Point, IFixture> pair : fixtures.values()) {
			IFixture fixture = pair.getValue1();
			if (fixture instanceof TerrainFixture) {
				fixtures.remove(fixture.getId());
			} else {
				LOGGER.warning("Unhandled fixture:\t" + fixture);
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
	private static int sorter(Object one, Object two) {
		String actualOne;
		String actualTwo;
		if (one instanceof String) {
			actualOne = (String) one;
		} else {
			actualOne = one.toString();
		}
		if (two instanceof String) {
			actualTwo = (String) two;
		} else {
			actualTwo = two.toString();
		}
		try {
			return Double.compare(NUM_FORMAT.parse(actualOne).doubleValue(),
				NUM_FORMAT.parse(actualTwo).doubleValue());
		} catch (Exception except) {
			return actualOne.compareTo(actualTwo);
		}
	}

	/**
	 * A method to produce tabular reports and add them to a GUI.
	 * @param consumer The way to add the tables to the GUI
	 * @param map The map to base the reports on
	 */
	public static void createGUITabularReports(BiConsumer<String, Component> consumer, IMapNG map)
			throws IOException {
		DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures =
			ReportGeneratorHelper.getFixtures(map);
		Map<Integer, Integer> parentMap = ReportGeneratorHelper.getParentMap(map);
		Player player = map.getCurrentPlayer();
		MapDimensions dimensions = map.getDimensions();
		@Nullable Point hq = ReportGeneratorHelper.findHQ(map, player);
		int currentTurn = map.getCurrentTurn();
		List<ITableGenerator<?>> generators = Arrays.asList(
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
		for (ITableGenerator<?> generator : generators) {
			TableModel tableModel = generator.produceTableModel(fixtures, parentMap);
			JTable table = new JTable(tableModel);
			TableRowSorter<TableModel> modelSorter = new TableRowSorter<TableModel>(tableModel);
			int i = 0;
			for (String column : generator.getHeaderRow()) {
				if ("distance".equalsIgnoreCase(column)) {
					modelSorter.setComparator(i, TabularReportGenerator::sorter);
				}
				i++;
			}
			table.setRowSorter(modelSorter);
			int vertControl;
			int horizControl;
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
		List<String> unhandled = new ArrayList<>();
		for (Pair<Point, IFixture> pair : fixtures.values()) {
			IFixture fixture = pair.getValue1();
			if (fixture instanceof TerrainFixture) {
				fixtures.remove(fixture.getId());
			} else {
				unhandled.add(fixture.toString());
			}
		}
		if (!unhandled.isEmpty()) {
			consumer.accept("other", BorderedPanel.verticalPanel(
				new JLabel("Fixtures not covered in any of the reports:"),
				new JList<String>(unhandled.toArray(new String[0])), null));
		}
	}
}
