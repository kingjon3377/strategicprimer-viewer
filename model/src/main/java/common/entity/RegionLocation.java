package common.entity;

/**
 * An entity can be located at an unspecified location in a region.
 */
public record RegionLocation(String world, int region) implements Location {
	public RegionLocation {
		if (region < -1) {
			throw new IllegalArgumentException("Region must be -1 if unspecified, or else non-negative");
		}
	}

	@Override
	public String getDisplayRepresentation() {
		return "In world %s region %d".formatted(world, region);
	}
}
