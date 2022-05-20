package common.map.fixtures.mobile;

import common.map.fixtures.UnitMember;

/*
 * @deprecated We're trying to get rid of the notion of 'proxies' in favor of
 * driver model methods.
 */
@Deprecated
interface UnitMemberProxy<T extends UnitMember> extends UnitMember, ProxyFor<T> {
}
