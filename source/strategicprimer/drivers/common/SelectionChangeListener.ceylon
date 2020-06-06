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
    """The "interaction point" changed. (That is, the user right-clicked on something.) Because
       this the current value of the "interaction point" is not supposed to be cached, and is
       supposed to basically expire as soon as the action is completed, this may or may not
       be called when the point is set to null, and callers must get the value from where
       it is stored themselves."""
    shared formal void interactionPointChanged();
}
