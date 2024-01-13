package common.entity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A way to identify entities that should be much more resistant to collisions than a simple increasing integer.
 *
 * @param originWorld The game-world where this entity originated, in case it travels to anotheer world.
 * @param creatingPlayer The ID numbeer of the player whose action caused the entity to be created, or a negative ID
 *                       if it was not created due to player action.
 * @param id A unique identifying number for the entity.
 */
public record ComplexIdentifier(@NotNull String originWorld, int creatingPlayer,
								@NotNull UUID id) implements EntityIdentifier {
	public ComplexIdentifier {
		if (creatingPlayer < -1) {
			throw new IllegalArgumentException("Player ID must be -1 if unknown, or nonnegative");
		}
	}


	@Override
	public @NotNull String getIdentifierString() {
		return String.format("%s (%s-%d)", id, originWorld, creatingPlayer);
	}
}
