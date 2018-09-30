"An interface for things that want to be called when a new map is loaded."
shared interface MapChangeListener {
    "React to the loading of a new map.

     Since all implementations of this interface went to their reference to a map model
     rather than taking the map from an event even when one was provided, this method
      specifies no parameters."
    shared formal void mapChanged();
}
