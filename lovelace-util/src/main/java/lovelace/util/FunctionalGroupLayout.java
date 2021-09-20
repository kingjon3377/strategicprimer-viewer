package lovelace.util;

import javax.swing.GroupLayout;

import java.awt.Component;
import java.awt.Container;

/**
 * An extension to {@link GroupLayout} to provide additional methods to make
 * initialization less verbose and more functional in style.
 */
public final class FunctionalGroupLayout extends GroupLayout {
	/**
	 * Add components and/or groups to a group. Var-arg arguments ({@link
	 * components}) must be either {@link Component} or {@link Group}
	 * instances, even though this cannot be expressed in the function
	 * signature in Java.
	 */
	private static <SpecificGroup extends Group> SpecificGroup
			initializeGroup(SpecificGroup group,
				Object... components) {
		for (Object component : components) {
			if (component instanceof Component) {
				group.addComponent((Component) component);
			} else if (component instanceof Group) {
				group.addGroup((Group) component);
			} else {
				throw new IllegalArgumentException("Must be a Component or Group");
			}
		}
		return group;
	}

	// Do *not* make these factories static; the methods they call are
	// GroupLayout instance methods, so the code will not compile.
	/**
	 * Factory for a parallel group.  Var-arg arguments ({@link
	 * components}) must be either {@link Component} or {@link Group}
	 * instances, even though this cannot be expressed in the function
	 * signature in Java.
	 */
	public ParallelGroup parallelGroupOf(Object... components) {
		return initializeGroup(createParallelGroup(), components);
	}
	/**
	 * Factory for a sequential group.  Var-arg arguments ({@link
	 * components}) must be either {@link Component} or {@link Group}
	 * instances, even though this cannot be expressed in the function
	 * signature in Java.
	 */
	public SequentialGroup sequentialGroupOf(Object... components) {
		return initializeGroup(createSequentialGroup(), components);
	}

	/**
	 * @param host The container to lay out.
	 * @param autoCreateGaps Whether to automatically create gaps between components.
	 * @param autoCreateContainerGaps Whether to automatically create gaps
	 * between components at an edge of the container and that edge.
	 *
	 * TODO: Create an enum for the configuration, so we don't have to take Boolean constructor parameters.
	 */
	public FunctionalGroupLayout(Container host, boolean autoCreateGaps,
			boolean autoCreateContainerGaps) {
		super(host);
		super.setAutoCreateGaps(autoCreateGaps);
		super.setAutoCreateContainerGaps(autoCreateContainerGaps);
	}

	/**
	 * Constructor variant defaulting to not creating container gaps.
	 * @param host The container to lay out.
	 * @param autoCreateGaps Whether to automatically create gaps between components.
	 */
	public FunctionalGroupLayout(Container host, boolean autoCreateGaps) {
		this(host, autoCreateGaps, false);
	}

	/**
	 * Constructor variant defaulting to not creating gaps or container gaps.
	 * @param host The container to lay out.
	 */
	public FunctionalGroupLayout(Container host) {
		this(host, false);
	}
}
