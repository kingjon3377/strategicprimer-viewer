"A supertype for both [[TileFixture]] and any
 [[strategicprimer.model.map.fixtures::UnitMember]]s (etc.) that shouldn't be
 [[TileFixture]]s, so we don't have to special-case them for things like searching."
shared interface IFixture {
    "The fixture's ID number. For most fixtures this should be unique in the map."
    shared formal Integer id;
    "Whether the fixture is equal to another if we ignore its ID (and DC for events)."
    shared formal Boolean equalsIgnoringID(IFixture fixture);
    """Clone the fixture, optionally "sanitizing" it in a way that should not break subset
       checking."""
    shared formal IFixture copy(
            """Whether to "zero out" (omit) sensitive information in the copy."""
            Boolean zero);
}
