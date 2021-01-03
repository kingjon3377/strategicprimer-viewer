import strategicprimer.model.common.map {
    HasMutableOwner,
    HasMutablePortrait
}
shared interface IMutableTownFixture satisfies ITownFixture&HasMutableOwner&HasMutablePortrait {
}
