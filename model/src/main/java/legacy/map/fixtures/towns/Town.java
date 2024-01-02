package legacy.map.fixtures.towns;

import legacy.map.Player;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.TownStatus;

/**
 * An abandoned, ruined, or burned-out (or active) town.
 */
public class Town extends AbstractTown {
    /**
     * @param townStatus The status of the town
     * @param size The size of the town
     * @param discoverDC The DC to discover the town
     * @param townName The name of the town
     * @param id The town's ID number
     * @param player The owner of the town
     */
    public Town(final TownStatus townStatus, final TownSize size, final int discoverDC,
                final String townName, final int id, final Player player) {
        super(townStatus, size, townName, player, discoverDC);
        this.id = id;
    }

    /**
     * The town's ID number
     */
    private final int id;

    /**
     * The town's ID number
     */
    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getPlural() {
        return "Towns";
    }

    @Override
    public String getKind() {
        return "town";
    }

    @Override
    public String getDefaultImage() {
        return "town.png";
    }

    @Override
    public Town copy(final CopyBehavior zero) {
        final Town retval = new Town(getStatus(), getTownSize(),
                (zero == CopyBehavior.ZERO) ? 0 : getDC(), getName(), id, owner());
        retval.setImage(getImage());
        if (zero == CopyBehavior.KEEP) {
            retval.setPopulation(getPopulation());
        }
        return retval;
    }
}
