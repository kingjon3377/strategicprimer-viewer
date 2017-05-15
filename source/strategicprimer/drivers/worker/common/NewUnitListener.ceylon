import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
import java.util {
    EventListener
}
"An interface for things that want to accept a new user-created unit."
shared interface NewUnitListener satisfies EventListener {
    "Add the new unit."
    shared formal void addNewUnit(IUnit unit);
}