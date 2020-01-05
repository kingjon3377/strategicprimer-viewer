import lovelace.util.common {
    todo
}

"An exception thrown to signal traversal is impossible."
todo("Ocean isn't impassable to everything, of course.") // FIXME
shared class TraversalImpossibleException()
        extends Exception("Traversal is impossible.") {}
