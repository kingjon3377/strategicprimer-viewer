import java.util {
    EventListener
}

"An interface for objects listening for added or removed items in lists."
shared interface AddRemoveListener satisfies EventListener { // TODO: Drop EventListener dependency
    "Handle something being added."
    shared formal void add(
            "What kind of thing is being added"
            String category,
            "A String description of the thing to be added"
            String addendum);

    "Handle something being removed. Default implementation is a no-op."
    shared default void remove("What kind of thing is being removed" String cetegory) {}
}
