import lovelace.util.common {
    todo
}
"An interface for objects that can tell others when they've finished something."
todo("Does this need to be shared?")
shared interface CompletionSource {
	"Notify the given listener when we finish something in future."
	shared formal void addCompletionListener(CompletionListener listener);
	"Stop notifying the given listener."
	shared formal void removeCompletionListener(CompletionListener listener);
}