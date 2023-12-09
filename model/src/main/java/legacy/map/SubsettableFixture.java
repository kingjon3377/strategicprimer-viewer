package legacy.map;

import common.map.Subsettable;

/**
 * Marker interface for {@link Subsettable subsettable} {@link IFixture fixtures}.
 */
public interface SubsettableFixture extends IFixture, Subsettable<IFixture> {
}
