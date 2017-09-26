"An interface for objects that can indicate the selected location and tile changed."
shared interface SelectionChangeSource {
    "Notify the given listener of future selection changes."
    shared formal void addSelectionChangeListener(SelectionChangeListener listener);
    "Stop notifying the given listener of selection changes."
    shared formal void removeSelectionChangeListener(SelectionChangeListener listener);
}