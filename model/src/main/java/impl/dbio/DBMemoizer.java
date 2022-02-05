package impl.dbio;

import common.map.IFixture;
import common.map.IMapNG;
import java.util.Map;
import java.util.HashMap;
import common.xmlio.Warning;

import org.javatuples.Pair;
import java.util.stream.Collectors;

final class DBMemoizer {
	private static final Map<Pair<IMapNG, Integer>, IFixture> cache = new HashMap<>();

	public static IFixture findById(final IMapNG map, final int id, final MapContentsReader context, final Warning warner) {
		if (cache.containsKey(Pair.with(map, id))) {
			return cache.get(Pair.with(map, id));
		} else {
			IFixture retval = context.findByIdImpl(map.streamLocations()
					.flatMap(p -> map.getFixtures(p).stream())
						.collect(Collectors.toList()), id);
			if (retval == null) {
				throw new IllegalStateException("Memoizer didn't find value to memoize");
			}
			cache.put(Pair.with(map, id), retval);
			return retval;
		}
	}
}
