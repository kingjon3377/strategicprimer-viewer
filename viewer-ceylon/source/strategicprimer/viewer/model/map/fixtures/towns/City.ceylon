import model.map {
    Player
}
import model.map.fixtures.towns {
    TownStatus
}
"An abandoned, ruined, or burned-out city."
shared class City("The status of the city" TownStatus townStatus,
        "The size of the city" TownSize townSize,
        "The DC to discover the city" Integer discoverDC,
        "The name of the city" String townName,
        "The city's ID number" shared actual Integer id,
        "The owner of the city" Player player)
        extends AbstractTown(townStatus, townSize, townName, player, discoverDC) {
    shared actual String plural() => "Cities";
    shared actual String kind => "city";
    shared actual String defaultImage = "city.png";
    shared actual City copy(Boolean zero) {
        City retval = City(status, size, (zero) then 0 else dc, name, id, owner);
        retval.setImage(image);
        return retval;
    }
}