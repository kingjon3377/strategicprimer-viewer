import strategicprimer.model.map {
    Player
}
"An abandoned, ruined, or burned-out town."
shared class Town("The status of the town" TownStatus townStatus,
        "The size of the town" TownSize size,
        "The DC to discover the town" Integer discoverDC,
        "The name of the town" String townName,
        "The town's ID number" shared actual Integer id,
        "The owner of the town" Player player)
        extends AbstractTown(townStatus, size, townName, player, discoverDC) {
    shared actual String plural = "Towns";
    shared actual String kind => "town";
    shared actual String defaultImage = "town.png";
    shared actual Town copy(Boolean zero) {
        Town retval = Town(super.status, super.townSize, (zero) then 0 else discoverDC,
            townName, id, player);
        retval.image = image;
        if (!zero) {
            retval.population = population;
        }
        return retval;
    }
}
