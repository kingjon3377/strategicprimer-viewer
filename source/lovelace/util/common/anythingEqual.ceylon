shared Boolean anythingEqual(Anything one, Anything two) {
    if (exists one) {
        if (exists two) {
            return one == two;
        } else {
            return false;
        }
    } else {
        return !two exists;
    }
}