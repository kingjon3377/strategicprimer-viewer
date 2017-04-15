"An object to hold numeric constants useful for drawing tiles' contents."
object drawingNumericConstants {
    "The part of a tile's width or height that a river's short dimension should occupy."
    shared Float riverShortDimension = 1.0 / 8.0;
    "Where the short side of a river starts, along the edge of the tile."
    shared Float riverShortStart = 7.0 / 16.0;
    "The part of a tile's width or height its long dimension should occupy."
    shared Float riverLongDimension = 1.0 / 2.0;
    "The coordinates in an 'event' other than [[eventStart]], 0, and 100%."
    shared Float eventOther = 1.0 / 2.0;
    "How far along a tile's dimension a lake should start."
    shared Float lakeStart = 1.0 / 4.0;
    "How big a unit should be. Also its starting position (?)."
    shared Float unitSize = 1.0 / 4.0;
    "How wide and tall a fortress should be."
    shared Float fortSize = 1.0 / 3.0;
    "Where a fortress should start."
    shared Float fortStart = 2.0 / 3.0;
    """Where an "event" should start."""
    shared Float eventStart = 3.0 / 4.0;
}
