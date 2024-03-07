package common.map.fixtures.mobile.worker;

import java.util.function.IntSupplier;
import java.util.stream.IntStream;

/**
 * A class representing a worker's core statistical attributes.
 */
public final class WorkerStats {
	/**
	 * The basis of stats: every two points more than this is worth +1, and
	 * every two points less is worth -1.
	 */
	private static final int statBasis = 10;

	/**
	 * The modifier for (effect of) the given stat value: (stat - 10) / 2,
	 * always rounding down.
	 */
	public static int getModifier(final int stat) {
		return stat / 2 - statBasis / 2;
	}

	/**
	 * The modifier string for a stat with the given value.
	 */
	public static String getModifierString(final int stat) {
		final int modifier = getModifier(stat);
		return (modifier >= 0) ? "+" + modifier : Integer.toString(modifier);
	}

	/**
	 * The worker's health.
	 */
	private final int hitPoints;

	/**
	 * The worker's health.
	 */
	public int getHitPoints() {
		return hitPoints;
	}

	/**
	 * The worker's max health.
	 */
	private final int maxHitPoints;

	/**
	 * The worker's max health.
	 */
	public int getMaxHitPoints() {
		return maxHitPoints;
	}

	/**
	 * The worker's strength.
	 */
	private final int strength;

	/**
	 * The worker's strength.
	 */
	public int getStrength() {
		return strength;
	}

	/**
	 * The worker's dexterity.
	 */
	private final int dexterity;

	/**
	 * The worker's dexterity.
	 */
	public int getDexterity() {
		return dexterity;
	}

	/**
	 * The worker's constitution.
	 */
	private final int constitution;

	/**
	 * The worker's constitution.
	 */
	public int getConstitution() {
		return constitution;
	}

	/**
	 * The worker's intelligence.
	 */
	private final int intelligence;

	/**
	 * The worker's intelligence.
	 */
	public int getIntelligence() {
		return intelligence;
	}

	/**
	 * The worker's wisdom.
	 */
	private final int wisdom;

	/**
	 * The worker's wisdom.
	 */
	public int getWisdom() {
		return wisdom;
	}

	/**
	 * The worker's charisma.
	 */
	private final int charisma;

	/**
	 * The worker's charisma.
	 */
	public int getCharisma() {
		return charisma;
	}

	/**
	 * Main constructor, taking all the stats.
	 */
	public WorkerStats(final int hp, final int maxHP, final int str, final int dex, final int con,
					   final int intel, final int wis, final int cha) {
		hitPoints = hp;
		maxHitPoints = maxHP;
		strength = str;
		dexterity = dex;
		constitution = con;
		intelligence = intel;
		wisdom = wis;
		charisma = cha;
	}

	/**
	 * Constructor for making a stat-adjustments version, so omitting HP.
	 */
	public static WorkerStats factory(final int str, final int dex, final int con,
									  final int intel, final int wis, final int cha) {
		return new WorkerStats(0, 0, str, dex, con, intel, wis, cha);
	}

	/**
	 * Takes an existing set of stats and a set of adjustments to produce
	 * an adjusted set.
	 *
	 * @param hp         The number to use for {@link #hitPoints} and {@link #maxHitPoints}
	 * @param base       A set of base stats to use for the other stats.
	 * @param adjustment A set of adjustments to add to those stats.
	 */
	public static WorkerStats adjusted(final int hp, final WorkerStats base, final WorkerStats adjustment) {
		return new WorkerStats(hp, hp, base.strength + adjustment.strength,
				base.dexterity + adjustment.dexterity,
				base.constitution + adjustment.constitution,
				base.intelligence + adjustment.intelligence,
				base.wisdom + adjustment.wisdom,
				base.charisma + adjustment.charisma);
	}

	/**
	 * Given an RNG, produce a random set of stats, with HP set to 0.
	 */
	public static WorkerStats random(final IntSupplier rng) {
		return factory(rng.getAsInt(), rng.getAsInt(), rng.getAsInt(), rng.getAsInt(),
				rng.getAsInt(), rng.getAsInt());
	}

	/**
	 * Clone the object.
	 */
	public WorkerStats copy() {
		return new WorkerStats(hitPoints, maxHitPoints, strength, dexterity,
				constitution, intelligence, wisdom, charisma);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final WorkerStats that) {
			return hitPoints == that.hitPoints &&
					maxHitPoints == that.maxHitPoints &&
					strength == that.strength &&
					dexterity == that.dexterity &&
					constitution == that.constitution &&
					intelligence == that.intelligence &&
					wisdom == that.wisdom &&
					charisma == that.charisma;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return hitPoints + (maxHitPoints << 3) +
				(strength << 6) + (dexterity << 9) +
				(constitution << 12) + (intelligence << 15) +
				(wisdom << 18) + (charisma << 21);
	}

	@SuppressWarnings("HardcodedFileSeparator")
	@Override
	public String toString() {
		return String.format("HP: %d / %d%nStr: %d%nDex: %d%nCon: %d%nInt: %d%nWis: %d%nCha: %d%n",
				hitPoints, maxHitPoints, strength, dexterity, constitution, intelligence,
				wisdom, charisma);
	}

	public String getPrintable() {
		return String.format(
				"Str: %+d, Dex %+d, Con %+d, Int %+d, Wis %+d, Cha %+d",
				getModifier(strength), getModifier(dexterity),
				getModifier(constitution), getModifier(intelligence),
				getModifier(wisdom), getModifier(charisma));
	}

	public int[] array() {
		return IntStream.of(strength, dexterity, constitution, intelligence, wisdom,
				charisma).toArray();
	}
}
