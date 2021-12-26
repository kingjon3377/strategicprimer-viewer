"An interface for objects that handle movement and can tell listeners how much a move
 cost."
shared interface MovementCostSource {
    "Notify the given listener of any future movement costs."
    shared formal void addMovementCostListener(MovementCostListener listener);

    "Stop notifying the given listener of movement costs."
    shared formal void removeMovementCostListener(MovementCostListener listener);
}
