"An interface for objects that want to be notified of when a moving unit incurs movement
 costs."
shared interface MovementCostListener {
    "Account for a movement."
    shared formal void deduct(
            "How many movement points the movement or action cost"
            Integer cost);
}
