import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    Player
}

"An abandoned, ruined, or burned-out fortification."
todo("FIXME: We want this to share a tag, and model code, with Fortress. Maybe an active
      Fortification is a Fortress, and a non-active Fortress is a Fortification?")
shared class Fortification("The status of the fortification" TownStatus townStatus,
        "The size of the fortification" TownSize size,
        "The DC to discover the fortification" Integer discoverDC,
        "The name of the fortification" String townName,
        "The fortification's ID number" shared actual Integer id,
        "The owner of the fortification" Player player)
        extends AbstractTown(townStatus, size, townName, player, discoverDC) {
    shared actual String defaultImage = "fortification.png";

    shared actual String plural = "Fortifications";

    shared actual String kind => "fortification";

    shared actual Fortification copy(Boolean zero) {
        Fortification retval = Fortification(status, townSize, (zero) then 0 else dc,
            name, id, owner);
        retval.image = image;
        if (!zero) {
            retval.population = population;
        }
        return retval;
    }
}
