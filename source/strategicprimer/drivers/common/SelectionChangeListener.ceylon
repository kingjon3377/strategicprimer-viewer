import strategicprimer.model.common.map {
    Point
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

"An interface for objects that want to know when the selected tile, or the selected unit
 if the app has a notion of a selected unit, changes."
shared interface SelectionChangeListener {
    "The selected tile's location changed."
    shared formal void selectedPointChanged(Point? previousSelection, Point newSelection);
    "The selected unit changed."
    shared formal void selectedUnitChanged(IUnit? previousSelection, IUnit? newSelection);
}
