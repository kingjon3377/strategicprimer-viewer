package mining;
/**
 * Kinds of mines we know how to create.
 */
/* package */ enum MineKind {
	/**
	 * "Normal," which <em>tries</em> to create randomly-branching "veins".
	 */
	Normal,
	/**
	 * A mine which emphasizes layers, such as a sand mine.
	 */
	Banded
}
