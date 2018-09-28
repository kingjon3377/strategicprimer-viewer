import strategicprimer.model.common.map {
    Player
}
"An abandoned, ruined, or burned-out city."
shared class City("The status of the city" TownStatus townStatus,
        "The size of the city" TownSize size,
        "The DC to discover the city" Integer discoverDC,
        "The name of the city" String townName,
        "The city's ID number" shared actual Integer id,
        "The owner of the city" Player player)
        extends AbstractTown(townStatus, size, townName, player, discoverDC) {
    shared actual String plural = "Cities";
    shared actual String kind => "city";
    shared actual String defaultImage = "city.png";
    shared actual City copy(Boolean zero) {
        City retval = City(status, townSize, (zero) then 0 else dc, name, id, owner);
        retval.image = image;
        if (!zero) {
            retval.population = population;
        }
        return retval;
    }
}
