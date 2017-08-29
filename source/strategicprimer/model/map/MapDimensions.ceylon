"An encapsulation of a map's dimensions (and its map version as well)."
shared interface MapDimensions {
	"The number of rows in the map."
	shared formal Integer rows;
	"The number of columns in the map."
	shared formal Integer columns;
	"The map version."
	shared formal Integer version;
}