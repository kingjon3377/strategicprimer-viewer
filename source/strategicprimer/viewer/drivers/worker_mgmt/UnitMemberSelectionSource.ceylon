"An interface for objects that notify others of the user's selection of a unit member."
shared interface UnitMemberSelectionSource {
	"Add a listener."
	shared formal void addUnitMemberListener(UnitMemberListener listener);
	"Remove a listener."
	shared formal void removeUnitMemberListener(UnitMemberListener listener);
}