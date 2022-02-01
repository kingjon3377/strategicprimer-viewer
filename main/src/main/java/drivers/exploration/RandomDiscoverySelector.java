package drivers.exploration;

import drivers.common.SelectionChangeListener;

import common.map.fixtures.mobile.IUnit;

import drivers.map_viewer.FixtureList;
import java.util.Collections;
import java.util.function.Supplier;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.JList;

import common.map.TileFixture;
import common.map.Point;

import exploration.common.SimpleMovementModel;
import exploration.common.IExplorationModel;
import exploration.common.Speed;

import java.util.ArrayList;
import java.util.List;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * A list-data-listener to select a random but suitable set of fixtures to be "discovered" if the tile is explored.
 */
/* package */ class RandomDiscoverySelector implements SelectionChangeListener {
	private boolean outsideCritical = true;
	public RandomDiscoverySelector(IExplorationModel driverModel, FixtureList mainList, Supplier<Speed> speedSource) {
		this.driverModel = driverModel;
		this.mainList = mainList;
		this.speedSource = speedSource;
	}

	private final IExplorationModel driverModel;
	private final FixtureList mainList;
	private final Supplier<Speed> speedSource;

	private void selectedPointChangedImpl() {
		IUnit selectedUnit = driverModel.getSelectedUnit();
		if (outsideCritical && selectedUnit != null) {
			outsideCritical = false;
			mainList.clearSelection();
			List<Pair<Integer, TileFixture>> constants = new ArrayList<>();
			List<Pair<Integer, TileFixture>> possibles = new ArrayList<>();
			ListModel<? extends TileFixture> model = mainList.getModel();
			for (int index = 0; index < model.getSize(); index++) {
				TileFixture fixture = model.getElementAt(index);
				if (SimpleMovementModel.shouldAlwaysNotice(selectedUnit, fixture)) {
					constants.add(Pair.with(index, fixture));
				} else if (SimpleMovementModel.shouldSometimesNotice(selectedUnit,
						speedSource.get(), fixture)) {
					possibles.add(Pair.with(index, fixture));
				}
			}
			Collections.shuffle(possibles);
			SimpleMovementModel.selectNoticed(possibles, Pair::getValue1, selectedUnit,
				speedSource.get()).forEach(constants::add);
			mainList.setSelectedIndices(constants.stream().map(Pair::getValue0)
				.mapToInt(Integer::intValue).toArray());
			outsideCritical = true;
		}
	}

	@Override
	public void selectedPointChanged(@Nullable Point old, Point newPoint) {
		SwingUtilities.invokeLater(this::selectedPointChangedImpl);
	}

	@Override
	public void selectedUnitChanged(@Nullable IUnit old, @Nullable IUnit newSel) {}

	@Override
	public void interactionPointChanged() {}

	@Override
	public void cursorPointChanged(@Nullable Point old, Point newCursor) {}
}
