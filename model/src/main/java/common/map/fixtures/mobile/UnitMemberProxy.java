package common.map.fixtures.mobile;

import common.map.fixtures.UnitMember;

interface UnitMemberProxy<T extends UnitMember> extends UnitMember, ProxyFor<T> {
}
