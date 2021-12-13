import lovelace.util.common {
    PathWrapper
}

"""An interface for utility GUI apps that can respond to an "open" menu item."""
shared interface UtilityGUI satisfies UtilityDriver {
    shared formal void open(PathWrapper path);
}
