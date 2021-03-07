import strategicprimer.drivers.common {
    SelectionChangeListener
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import java.lang {
    IntArray
}
import ceylon.random {
    randomize
}
import javax.swing {
    SwingUtilities,
    SwingList=JList
}
import strategicprimer.model.common.map {
    TileFixture,
    Point
}
import strategicprimer.drivers.exploration.common {
    simpleMovementModel,
    IExplorationModel,
    Speed
}
import lovelace.util.jvm {
    ListModelWrapper
}
import ceylon.collection {
    ArrayList,
    MutableList
}
"""A list-data-listener to select a random but suitable set of fixtures to
    be "discovered" if the tile is explored."""
class RandomDiscoverySelector(IExplorationModel driverModel,
            SwingList<TileFixture>&SelectionChangeListener mainList, Speed() speedSource)
        satisfies SelectionChangeListener {
    variable Boolean outsideCritical = true;
    void selectedPointChangedImpl() {
        if (outsideCritical, exists selectedUnit = driverModel.selectedUnit) {
            outsideCritical = false;
            mainList.clearSelection();
            MutableList<[Integer, TileFixture]> constants =
                ArrayList<[Integer, TileFixture]>();
            MutableList<[Integer, TileFixture]> possibles =
                ArrayList<[Integer, TileFixture]>();
            for (index->fixture in ListModelWrapper(mainList.model).indexed) {
                if (simpleMovementModel.shouldAlwaysNotice(selectedUnit, fixture)) {
                    constants.add([index, fixture]);
                } else if (simpleMovementModel.shouldSometimesNotice(selectedUnit,
                        speedSource(), fixture)) {
                    possibles.add([index, fixture]);
                }
            }
            constants.addAll(simpleMovementModel.selectNoticed(randomize(possibles),
                compose(Tuple<TileFixture, TileFixture, []>.first,
                    Tuple<Integer|TileFixture, Integer, [TileFixture]>.rest),
                selectedUnit, speedSource()));
            IntArray indices = IntArray.with(constants.map(Tuple.first));
            mainList.selectedIndices = indices;
            outsideCritical = true;
        }
    }
    shared actual void selectedPointChanged(Point? old, Point newPoint) =>
        SwingUtilities.invokeLater(selectedPointChangedImpl);
    shared actual void selectedUnitChanged(IUnit? old, IUnit? newSel) {}
    shared actual void interactionPointChanged() {}
    shared actual void cursorPointChanged(Point? old, Point newCursor) {}
}
