import javax.swing {
    GroupLayout
}

import java.awt {
    Component,
    Container
}

"An extension to [[GroupLayout]] to provide additional methods to make initialization
 less verbose and more functional in style."
shared class FunctionalGroupLayout extends GroupLayout {
    "Add components and/or groups to a group."
    static SpecificGroup initializeGroup<SpecificGroup>(SpecificGroup group,
            Component|Group* components) given SpecificGroup satisfies Group {
        for (component in components) {
            switch (component)
            case (is Component) { group.addComponent(component); }
            case (is Group) { group.addGroup(component); }
        }
        return group;
    }
    // Do *not* make these factories static; the methods they call are GroupLayout
    // instance methods, so the code will not compile.
    "Factory for a parallel group."
    shared ParallelGroup parallelGroupOf(Component|Group* components) =>
        initializeGroup(createParallelGroup(), *components);
    "Factory for a sequential group."
    shared SequentialGroup sequentialGroupOf(Component|Group* components) =>
        initializeGroup(createSequentialGroup(), *components);
    shared new ("The container to lay out." Container host,
            "Whether to automatically create gaps between components."
            Boolean autoCreateGaps = false,
            "Whether to automatically create gaps between components at an edge of the
             container and that edge."
            Boolean autoCreateContainerGaps = false) extends GroupLayout(host) {
        super.autoCreateGaps = autoCreateGaps;
        super.autoCreateContainerGaps = autoCreateContainerGaps;
    }
}
