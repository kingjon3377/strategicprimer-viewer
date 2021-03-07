"A wrapper around [[Object.equals]] that doesn't require its
 callers to check its arguments for nullity first."
shared Boolean anythingEqual(Anything one, Anything two) {
    if (exists one, exists two) {
        return one == two;
    } else {
        return !one exists && !two exists;
    }
}
