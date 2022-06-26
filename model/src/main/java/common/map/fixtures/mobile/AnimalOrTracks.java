package common.map.fixtures.mobile;

import common.map.HasKind;
import common.map.IFixture;
import common.map.fixtures.UnitMember;

/**
 * An interface to cover animals and animal tracks.
 *
 * TODO: Why do we need to keep them combined?
 */
public interface AnimalOrTracks extends IFixture, UnitMember, HasKind {}
