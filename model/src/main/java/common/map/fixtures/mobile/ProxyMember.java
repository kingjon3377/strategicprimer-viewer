package common.map.fixtures.mobile;

import java.util.ArrayList;
import java.util.List;

import common.map.IFixture;
import common.map.fixtures.UnitMember;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A proxy for non-worker unit members.
 */
class ProxyMember implements UnitMemberProxy<UnitMember> {
	private static final Logger LOGGER = Logger.getLogger(ProxyMember.class.getName());
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
	public ProxyMember copy(final boolean zero) {
		ProxyMember retval = new ProxyMember();
		for (UnitMember member : proxiedMembers) {
			retval.addProxied(member.copy(zero));
		}
		return retval;
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		LOGGER.warning("ProxyMember.equalsIgnoringID() called");
		if (fixture instanceof ProxyMember) {
			return ((ProxyMember) fixture).proxiedMembers.equals(proxiedMembers); // TODO: Shouldn't depend on order, right?
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
	public Iterable<UnitMember> getProxied() {
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
		String retval = getConsensus(UnitMember::toString);
		if (retval == null) {
			return "a proxy for a variety of unit members";
		} else {
			return retval;
		}
	}

	/**
	 * Returns the ID number shared by all the proxied members, or -1 if
	 * either there are no proxied members or some have a different ID.
	 */
	@Override
	public int getId() {
		Integer retval = getConsensus(UnitMember::getId);
		return (retval == null) ? -1 : retval;
	}

	@Override
	public String getPlural() {
		String retval = getConsensus(UnitMember::getPlural);
		return (retval == null) ? "Various Unit Members" : retval;
	}
}
