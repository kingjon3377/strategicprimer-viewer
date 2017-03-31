import model.map {
    Player
}
"A Player object that can be set as current or not."
shared interface MutablePlayer satisfies Player {
	"Whether this is the current player or not, now marked as `variable`."
	shared actual formal variable Boolean current;
}