"An interface for things that can fire notifications of the current player changing."
shared interface PlayerChangeSource {
	"Notify the given listener of future changes to which player is current."
	shared formal void addPlayerChangeListener(PlayerChangeListener listener);
	"Stop notifying the given listener of changes to which player is current."
	shared formal void removePlayerChangeListener(PlayerChangeListener listener);
}