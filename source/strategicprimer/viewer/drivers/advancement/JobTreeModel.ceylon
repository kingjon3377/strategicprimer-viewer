import strategicprimer.model.common.map {
    HasName
}
import lovelace.util.common {
    todo
}
import javax.swing.event {
    TreeModelListener,
    TreeModelEvent
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker
}
import strategicprimer.viewer.drivers.worker_mgmt {
    UnitMemberListener
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import javax.swing.tree {
    TreePath,
    TreeSelectionModel,
    TreeModel
}
import java.lang {
    ArrayIndexOutOfBoundsException,
    IntArray,
    ObjectArray
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob,
    Job,
    Skill,
    ISkill
}

"A model for a tree of a worker's Jobs and Skills."
class JobTreeModel() satisfies TreeModel&UnitMemberListener&AddRemoveListener {
    MutableList<TreeModelListener> listeners = ArrayList<TreeModelListener>();

    "The worker whom the Jobs and Skills describe."
    variable IWorker? localRoot = null;

    shared late TreeSelectionModel selectionModel;

    shared actual IWorker? root => localRoot;

    shared actual HasName getChild(Object parent, Integer index) {
        if (index >= 0, is IWorker|IJob parent,
                exists child = parent.getFromFirst(index)) {
            return child;
        } else {
            throw ArrayIndexOutOfBoundsException("Parent does not have that child");
        }
    }

    shared actual Integer getChildCount(Object parent) {
        if (is IWorker|IJob parent) {
            return parent.size;
        } else {
            assert (is ISkill parent);
            return 0;
        }
    }

    shared actual Boolean isLeaf(Object node) => !node is IWorker|IJob;

    "Handling changed values is not yet implemented."
    todo("Implement if necessary")
    shared actual void valueForPathChanged(TreePath path, Object newValue) =>
            log.error("valueForPathChanged needs to be implemented");

    shared actual Integer getIndexOfChild(Object parent, Object child) {
        if (is IWorker|IJob parent,
                exists index->ignored = parent.locate(child.equals)) {
            return index;
        } else {
            return -1;
        }
    }

    shared actual void addTreeModelListener(TreeModelListener listener) =>
            listeners.add(listener);
    shared actual void removeTreeModelListener(TreeModelListener listener) =>
            listeners.remove(listener);

    void fireTreeNodesInserted(TreeModelEvent event) {
        for (listener in listeners) {
            listener.treeNodesInserted(event);
        }
    }

    void fireTreeStructureChanged(TreeModelEvent event) {
        for (listener in listeners) {
            listener.treeStructureChanged(event);
        }
    }

    "Add a new Job or Skill."
    todo("Show error dialog, or at least visual-beep, instead of just logging warnings?")
    shared actual void add(String category, String addendum) {
        if ("job" == category) {
            if (exists currentRoot = localRoot) {
                IJob job = Job(addendum, 0);
                Integer childCount = getChildCount(currentRoot);
                currentRoot.addJob(job);
                fireTreeNodesInserted(TreeModelEvent(this, TreePath(currentRoot),
                    IntArray.with(Singleton(childCount)), ObjectArray.with(Singleton(job))));
            } else {
                log.warn("Can't add a new Job when no worker selected");
            }
        } else if ("skill" == category) {
            if (exists selectionPath = selectionModel.selectionPath,
                    is IJob job = selectionPath.lastPathComponent) {
                ISkill skill = Skill(addendum, 0, 0);
                Integer childCount = getChildCount(job);
                job.addSkill(skill);
                fireTreeNodesInserted(TreeModelEvent(this,
                    TreePath(ObjectArray<Object>.with([localRoot, job])),
                    IntArray.with(Singleton(childCount)),
                    ObjectArray.with(Singleton(skill))));
            } else {
                log.warn("Can't add a new Skill when no Job selected");
            }
        } else {
            log.warn("Don't know how to add a new '``category``");
        }
    }

    "Change what unit member is currently selected"
    shared actual void memberSelected(UnitMember? old, UnitMember? selected) {
        if (is IWorker selected) {
            localRoot = selected;
            fireTreeStructureChanged(TreeModelEvent(this,
                TreePath(selected)));
        } else {
            localRoot = null;
            fireTreeStructureChanged(TreeModelEvent(this, null of TreePath?));
        }
    }
}
