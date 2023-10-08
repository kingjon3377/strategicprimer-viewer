package query;

import common.map.fixtures.Quantity;

/**
 * Models of (game statistics for) herding small animals that don't produce any
 * resources on a daily basis.
 */
public enum SmallAnimalModel implements HerdModel {
    Rabbits(10, "Rabbits", 4);

    private static final Quantity QTY = new Quantity(0, "");

    /**
     * These animals don't actually produce any resources.
     */
    @Override
    public Quantity getProductionPerHead() {
        return QTY;
    }

    /**
     * Since these animals don't produce any resources, the coefficient is said to be zero.
     */
    @Override
    public double getPoundsCoefficient() {
        return 0.0;
    }

    /**
     * How much time, in minutes, herders must spend if there are this many animals.
     */
    private final int dailyTimePerHead;

    /**
     * How much time, in minutes, herders must spend if there are this many animals.
     */
    @Override
    public int getDailyTimePerHead() {
        return dailyTimePerHead;
    }

    /**
     * A description of the model to show the user.
     */
    private final String name;

    /**
     * A description of the model to show the user.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * How many turns, at most, should elapse between "extra chores" days.
     */
    private final int extraChoresInterval;

    /**
     * How many turns, at most, should elapse between "extra chores" days.
     */
    public int getExtraChoresInterval() {
        return extraChoresInterval;
    }

    /**
     * How much time, in minutes, must be spent per head on "extra chores" days.
     */
    public static int getExtraTimePerHead() {
        return 30;
    }

    /**
     * How much time, in minutes, must be spent on the entire herd or flock
     * each turn, regardless of its size, in addition to each herder's time
     * with individual animals.
     */
    public static int getDailyTimeFloor() {
        return 20;
    }

    SmallAnimalModel(final int timePerHead, final String nomen, final int extraInterval) {
        name = nomen;
        dailyTimePerHead = timePerHead;
        extraChoresInterval = extraInterval;
    }

    @Override
    public int dailyTime(final int heads) {
        return heads * dailyTimePerHead + getDailyTimeFloor();
    }
}
