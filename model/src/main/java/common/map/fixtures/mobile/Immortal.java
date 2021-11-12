package common.map.fixtures.mobile;

import common.map.fixtures.UnitMember;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/**
 * A (marker) interface for centaurs, trolls, ogres, fairies, and the like.
 */
public interface Immortal extends MobileFixture, UnitMember {
	/**
	 * Clone the object.
	 */
	@Override
	Immortal copy(boolean zero);

	/**
	 * A list of immortals that used to be represented as {@link Animal
	 * animals} but have transitioned to being individual subclasses of
	 * {@link Immortal}. This was provided so XML-reading code can start to
	 * <em>accept</em> that idiom before the transition, and so not break
	 * when given a map written after the XML-writing code starts to
	 * produce it.
	 *
	 * @deprecated the transition has occurred, so maybe this shouldn't be used anymore?
	 */
	@Deprecated
	public static final List<String> IMMORTAL_ANIMALS =
		Collections.unmodifiableList(Arrays.asList("snowbird",
			"thunderbird", "pegasus", "unicorn", "kraken"));
}
