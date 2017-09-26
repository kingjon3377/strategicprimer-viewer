"An interface for objects that can tell others when they've finished something."
interface CompletionSource {
    "Notify the given listener when we finish something in future."
    shared formal void addCompletionListener(CompletionListener listener);
    "Stop notifying the given listener."
    shared formal void removeCompletionListener(CompletionListener listener);
}