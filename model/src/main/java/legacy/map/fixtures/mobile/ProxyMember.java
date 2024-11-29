package legacy.map.fixtures.mobile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import legacy.map.IFixture;
import legacy.map.fixtures.UnitMember;

import java.util.Objects;
import java.util.function.Consumer;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A proxy for non-worker unit members.
 *
 * @deprecated We're trying to get rid of the notion of 'proxies' in favor of
 * driver model methods.
 */
@Deprecated
final class ProxyMember implements UnitMemberProxy<UnitMember> {
	private final List<UnitMember> proxiedMembers = new ArrayList<>();

	private ProxyMember() {
	}

	public ProxyMember(final UnitMember member) {
		proxiedMembers.add(member);
	}

	@Override
	public void addProxied(final UnitMember item) {
		proxiedMembers.add(item);
	}

	@Override
	public @NotNull ProxyMember copy(final CopyBehavior zero) {
		final ProxyMember retval = new ProxyMember();
		for (final UnitMember member : proxiedMembers) {
			retval.addProxied(member.copy(zero));
		}
		return retval;
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		LovelaceLogger.warning("ProxyMember.equalsIgnoringID() called");
		if (fixture instanceof final ProxyMember pm) {
			return new HashSet<>(pm.proxiedMembers).equals(new HashSet<>(proxiedMembers));
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(final IFixture fixture, final Consumer<String> report) {
		report.accept("isSubset called on ProxyMember");
		return false;
	}

	@Override
	public Collection<UnitMember> getProxied() {
		return new ArrayList<>(proxiedMembers);
	}

	@Override
	public boolean isParallel() {
		return true;
	}

	@Override
	public String toString() {
		if (proxiedMembers.isEmpty()) {
			return "a proxy for no unit members";
		}
		final String retval = getConsensus(UnitMember::toString);
		return Objects.requireNonNullElse(retval, "a proxy for a variety of unit members");
	}

	/**
	 * Returns the ID number shared by all the proxied members, or -1 if
	 * either there are no proxied members or some have a different ID.
	 */
	@Override
	public int getId() {
		final Integer retval = getConsensus(UnitMember::getId);
		return Objects.requireNonNullElse(retval, -1);
	}

	@Override
	public String getPlural() {
		final String retval = getConsensus(UnitMember::getPlural);
		return Objects.requireNonNullElse(retval, "Various Unit Members");
	}
}
