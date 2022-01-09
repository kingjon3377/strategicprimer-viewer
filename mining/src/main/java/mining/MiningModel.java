package mining;

import org.javatuples.Pair;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Deque;

import java.util.Random;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

/**
 * A class to model the distribution of a mineral to be mined. Note that the
 * constructor can be <em>very</em> computationally expensive!
 *
 * TODO: Try to reduce type-parameter verbosity
 */
/* package */ final class MiningModel {
	/**
	 * The points we have generated so far and the lode-status of those points.
	 */
	private final Map<Pair<Integer, Integer>, LodeStatus> unnormalized = new HashMap<>();

	private final Random rng;

	private void unnormalizedSet(Pair<Integer, Integer> loc, @Nullable LodeStatus status) {
		if (status == null) {
			unnormalized.remove(loc);
		} else {
			unnormalized.put(loc, status);
		}
	}

	Deque<Pair<Integer, Integer>> queue = new LinkedList<>();

	private final Function<LodeStatus, @Nullable LodeStatus> horizontalGenerator;

	private final Function<LodeStatus, @Nullable LodeStatus> verticalGenerator;

	private static int getColumn(Pair<Integer, Integer> pair) {
		return pair.getValue1();
	}

	/**
	 * Generate a value for the given point, and add its neighbors to the queue.
	 */
	private void modelPoint(int row, int column) {
		Pair<Integer, Integer> point = Pair.with(row, column);
		Pair<Integer, Integer> left = Pair.with(row, column - 1);
		Pair<Integer, Integer> down = Pair.with(row + 1, column);
		Pair<Integer, Integer> right = Pair.with(row, column + 1);
		if (unnormalized.containsKey(point)) {
			return;
		}
		LodeStatus current = unnormalized.get(point);
		if (!unnormalized.containsKey(right)) {
			unnormalizedSet(right, horizontalGenerator.apply(current));
			queue.offerLast(right);
		}
		if (!unnormalized.containsKey(down)) {
			unnormalizedSet(down, verticalGenerator.apply(current));
			queue.offerLast(down);
		}
		if (!unnormalized.containsKey(left)) {
			unnormalizedSet(left, horizontalGenerator.apply(current));
			queue.offerLast(left);
		}
	}

	/**
	 * A mapping from positions (normalized so they could be spit out into
	 * a spreadsheet) to {@link LodeStatus}es.
	 */
	private final Map<Pair<Integer, Integer>, LodeStatus> data;

	private final int maximumRow;

	private final int maximumColumn;

	private static <K, V> SortedMap<K, V> treeMap(Map<K, V> map, Comparator<K> comparator) {
		final SortedMap<K, V> retval = new TreeMap<>(comparator);
		retval.putAll(map);
		return retval;
	}

	/**
	 * @param initial The status to give the mine's starting point.
	 * @param seed A number to seed the RNG
	 * @param kind What kind of mine to model
	 */
	public MiningModel(LodeStatus initial, long seed, MineKind kind) {
		unnormalized.put(Pair.with(0, 0), initial);
		queue.offerLast(Pair.with(0, 0));
		rng = new Random(seed);
		verticalGenerator = (current) -> current.adjacent(rng::nextDouble);

		switch (kind) {
		case Normal:
			horizontalGenerator = (current) -> current.adjacent(rng::nextDouble);
			break;
		case Banded:
			horizontalGenerator = (current) -> current.bandedAdjacent(rng);
			break;
		default:
			throw new IllegalStateException("Non-exhaustive switch");
		}

		long counter = 0L;
		int pruneCounter = 0;

		while (!queue.isEmpty()) {
			Pair<Integer, Integer> point = queue.getFirst();
			counter++;
			if (counter % 100000L == 0L) {
				System.out.println(String.format("(%d,%d)", point.getValue0(),
					point.getValue1())); // TODO: Take ICLIHelper instead of using stdout
			} else if (counter % 1000L == 0L) {
				System.out.print(".");
				System.out.flush();
			}
			// Limit the size of the output spreadsheet.
			if (Math.abs(point.getValue0()) > 200 || Math.abs(point.getValue1()) > 100) {
				pruneCounter++;
				continue;
			} else {
				modelPoint(point.getValue0(), point.getValue1());
			}
		}
		System.out.println();
		System.out.printf("Pruned %d branches beyond our boundaries%n", pruneCounter);

		// FIXME: What is this procedure (by-row and by-column)
		// supposed to do? On porting back to Java it looks like it's
		// guaranteed to break on the first iteration in all three loops ...
		SortedMap<Integer, List<Pair<Integer, Integer>>> byRow =
			MiningModel.<Integer, List<Pair<Integer, Integer>>>treeMap(unnormalized.keySet()
					.stream().collect(Collectors.<Pair<Integer, Integer>, Integer>groupingBy(Pair::getValue0)),
				Comparator.<Integer>reverseOrder());
		for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : byRow.entrySet()) {
			int row = entry.getKey();
			List<Pair<Integer, Integer>> points = entry.getValue();
			if (points.stream().anyMatch(unnormalized::containsKey)) {
				points.forEach(unnormalized::remove);
			}
		}

		SortedMap<Integer, List<Pair<Integer, Integer>>> byColumnIncreasing =
			new TreeMap<>(unnormalized.keySet().stream()
				.collect(Collectors.groupingBy(Pair::getValue1)));
		for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : byColumnIncreasing.entrySet()) {
			int column = entry.getKey();
			List<Pair<Integer, Integer>> points = entry.getValue();
			if (points.stream().anyMatch(unnormalized::containsKey)) {
				points.forEach(unnormalized::remove);
			}
		}

		SortedMap<Integer, List<Pair<Integer, Integer>>> byColumnDecreasing =
			treeMap(unnormalized.keySet().stream()
					.collect(Collectors.<Pair<Integer, Integer>, Integer>groupingBy(Pair::getValue1)),
				Comparator.reverseOrder());
		for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : byColumnDecreasing.entrySet()) {
			int column = entry.getKey();
			List<Pair<Integer, Integer>> points = entry.getValue();
			if (points.stream().anyMatch(unnormalized::containsKey)) {
				points.forEach(unnormalized::remove);
			}
		}

		int minimumColumn = unnormalized.keySet().stream().map(Pair::getValue1)
			.mapToInt(Integer::intValue).min().orElse(0);
		data = Collections.unmodifiableMap(unnormalized.entrySet().stream().collect(
			Collectors.toMap(e -> Pair.with(e.getKey().getValue0(),
				e.getKey().getValue1() - minimumColumn), e -> e.getValue())));
		maximumRow = data.keySet().stream().map(Pair::getValue0).mapToInt(Integer::intValue)
			.max().orElse(0);
		maximumColumn = data.keySet().stream().map(Pair::getValue1).mapToInt(Integer::intValue)
			.max().orElse(0);
	}

	/**
	 * The farthest row we reached
	 */
	public int getMaximumRow() {
		return maximumRow;
	}

	/**
	 * The farthest column we reached
	 */
	public int getMaximumColumn() {
		return maximumColumn;
	}

	@Nullable
	public LodeStatus statusAt(int row, int column) {
		return data.get(Pair.with(row, column));
	}
}