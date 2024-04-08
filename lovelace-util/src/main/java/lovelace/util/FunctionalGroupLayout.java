package lovelace.util;

import javax.swing.GroupLayout;

import java.awt.Component;
import java.awt.Container;

/**
 * An extension to {@link GroupLayout} to provide additional methods to make
 * initialization less verbose and more functional in style.
 */
public final class FunctionalGroupLayout extends GroupLayout {
	public enum ContainerGaps {
		AUTO_CREATE_GAPS,
		FLUSH
	}

	/**
	 * Add components and/or groups to a group. Var-arg arguments must
	 * be either {@link Component} or {@link GroupLayout.Group} instances, even
	 * though this cannot be expressed in the function signature in Java.
	 */
	private static <SpecificGroup extends GroupLayout.Group> SpecificGroup
			initializeGroup(final SpecificGroup group,
							final Object... components) {
		for (final Object component : components) {
			switch (component) {
				case final Component c -> group.addComponent(c);
				case final GroupLayout.Group g -> group.addGroup(g);
				case null, default -> throw new IllegalArgumentException("Must be a Component or Group");
			}
		}
		return group;
	}

	// Do *not* make these factories static; the methods they call are
	// GroupLayout instance methods, so the code will not compile.

	/**
	 * Factory for a parallel group.  Var-arg arguments must be either
	 * {@link Component} or {@link GroupLayout.Group} instances, even though this
	 * cannot be expressed in the function signature in Java.
	 */
	@SuppressWarnings("ReturnOfInnerClass")
	public ParallelGroup parallelGroupOf(final Object... components) {
		return initializeGroup(createParallelGroup(), components);
	}

	/**
	 * Factory for a sequential group.  Var-arg arguments  must be either
	 * {@link Component} or {@link GroupLayout.Group} instances, even though this
	 * cannot be expressed in the function signature in Java.
	 */
	@SuppressWarnings("ReturnOfInnerClass")
	public SequentialGroup sequentialGroupOf(final Object... components) {
		return initializeGroup(createSequentialGroup(), components);
	}

	/**
	 * @param host                    The container to lay out.
	 * @param autoCreateGaps          Whether to automatically create gaps between components.
	 * @param autoCreateContainerGaps Whether to automatically create gaps
	 *                                between components at an edge of the container and that edge.
	 */
	public FunctionalGroupLayout(final Container host, final ContainerGaps autoCreateGaps,
								 final ContainerGaps autoCreateContainerGaps) {
		super(host);
		setAutoCreateGaps(autoCreateGaps == ContainerGaps.AUTO_CREATE_GAPS);
		setAutoCreateContainerGaps(autoCreateContainerGaps == ContainerGaps.AUTO_CREATE_GAPS);
	}

	/**
	 * Constructor variant defaulting to not creating container gaps.
	 *
	 * @param host           The container to lay out.
	 * @param autoCreateGaps Whether to automatically create gaps between components.
	 */
	public FunctionalGroupLayout(final Container host, final ContainerGaps autoCreateGaps) {
		this(host, autoCreateGaps, ContainerGaps.FLUSH);
	}

	/**
	 * Constructor variant defaulting to not creating gaps or container gaps.
	 *
	 * @param host The container to lay out.
	 */
	public FunctionalGroupLayout(final Container host) {
		this(host, ContainerGaps.FLUSH);
	}
}
