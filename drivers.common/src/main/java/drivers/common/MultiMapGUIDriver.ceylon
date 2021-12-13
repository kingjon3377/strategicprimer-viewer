"""An interface for graphical apps that (can) operate on both a "main" and at
   least one "subordinate" map."""
shared interface MultiMapGUIDriver satisfies GUIDriver {
    shared formal actual IMultiMapModel model;
}
