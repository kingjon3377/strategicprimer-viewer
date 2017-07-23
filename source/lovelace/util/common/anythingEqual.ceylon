shared Boolean anythingEqual(Anything one, Anything two) {
    if (exists one, exists two) {
        return one == two;
    } else {
        return !one exists && !two exists;
    }
}