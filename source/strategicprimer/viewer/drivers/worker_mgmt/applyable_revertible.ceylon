import lovelace.util.common {
    todo
}
"An interface to help simplify form management"
todo("Do we really need this in Ceylon?")
interface Applyable {
    """Method to call when an "Apply" button is pressed."""
    shared formal void apply();
}
"An interface to, together with [[Applyable]], simplify form management."
interface Revertible {
    """Method to call when a "Revert" button is pressed."""
    shared formal void revert();
}
