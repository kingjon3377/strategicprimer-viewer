package legacy.map.fixtures.mobile;

public final class Thunderbird extends ImmortalAnimal {
	@SuppressWarnings("MagicNumber")
	public Thunderbird(final int id) {
		super("thunderbird", "Thunderbirds", 29, id);
	}

	@Override
	public Thunderbird copy(final CopyBehavior zero) {
		return new Thunderbird(getId());
	}
}
