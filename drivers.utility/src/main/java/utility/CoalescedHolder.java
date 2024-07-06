package utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import legacy.map.IFixture;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.Iterator;
import java.util.Collections;
import java.util.Arrays;

// FIXME: What interface should this implement? Or should it just be dropped? In Ceylon this
// satisfied NonNullCorrespondence
public class CoalescedHolder<Type extends IFixture, Key> implements Iterable<List<Type>> {
	public interface Combiner<Type> {
		Type combine(Type[] args);
	}

	private final Function<Type, Key> extractor;

	private final Combiner<Type> combiner;

	public Combiner<Type> getCombiner() {
		return combiner;
	}

	private final Class<Type> cls;

	private final IntFunction<Type[]> arrayConstructor;

	public CoalescedHolder(final Class<Type> cls, final IntFunction<Type[]> arrayConstructor,
						   final Function<Type, Key> extractor, final Combiner<Type> combiner) {
		this.cls = cls;
		this.extractor = extractor;
		this.combiner = combiner;
		this.arrayConstructor = arrayConstructor;
	}

	private final Map<Key, List<Type>> map = new HashMap<>();

	public boolean defines(final Type key) {
		return true;
	}

	private String plural = "unknown";

	public String getPlural() {
		return plural;
	}

	public void setPlural(final String plural) {
		this.plural = plural;
	}

	public List<Type> get(final Type item) {
		final Key key = extractor.apply(item);
		plural = item.getPlural();
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			final List<Type> retval = new ArrayList<>();
			map.put(key, retval);
			return retval;
		}
	}

	public Iterator<List<Type>> iterator() {
		return map.values().stream().map(Collections::unmodifiableList).iterator();
	}

	public void addIfType(final Object item) {
		if (cls.isInstance(item)) {
			final Type typed = cls.cast(item);
			get(typed).add(typed);
		}
	}

	public Type combineRaw(final IFixture... list) {
		return combiner.combine(Arrays.stream(list).toArray(arrayConstructor));
	}
}
