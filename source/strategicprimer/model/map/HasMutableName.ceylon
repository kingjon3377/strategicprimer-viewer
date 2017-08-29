"An interface for things that have a name that can change."
shared interface HasMutableName satisfies HasName {
	"The name of whatever this is, now specified to be `variable`."
	shared actual formal variable String name;
}