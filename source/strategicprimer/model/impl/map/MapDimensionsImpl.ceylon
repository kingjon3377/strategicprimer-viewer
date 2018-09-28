import strategicprimer.model.impl.map {
    MapDimensions
}
"An encapsulation of a map's dimensions (and its map version as well)."
shared class MapDimensionsImpl(rows, columns, version) satisfies MapDimensions {
    "The map version."
    shared actual Integer version;
    "The number of rows in the map."
    shared actual Integer rows;
    "The number of columns in the map."
    shared actual Integer columns;
    shared actual Boolean equals(Object obj) {
        if (is MapDimensions obj) {
            return obj.rows == rows && obj.columns == columns && obj.version == version;
        } else {
            return false;
        }
    }
    shared actual Integer hash = rows + columns.leftLogicalShift(2);
    shared actual String string =>
            "Map dimensions: ``rows`` rows x ``columns`` columns; map version ``
                version``";
}
