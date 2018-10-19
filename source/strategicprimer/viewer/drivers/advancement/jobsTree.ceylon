import javax.swing.event {
    TreeModelListener,
    TreeModelEvent,
    TreeSelectionEvent
}
import javax.swing {
    JTree
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    ISkill
}
import lovelace.util.common {
    todo
}

"A tree representing a worker's Jobs and Skills."
todo("Can we split our special code out of this class, perhaps into
      a TreeSelectionModel, so we can make callers use a bog-standard JTree?")
class JobsTree(JobTreeModel jtModel) extends JTree(jtModel)
        satisfies SkillSelectionSource {
    MutableList<SkillSelectionListener> listeners =
            ArrayList<SkillSelectionListener>();

    shared actual void addSkillSelectionListener(SkillSelectionListener listener) =>
            listeners.add(listener);
    shared actual void removeSkillSelectionListener(
        SkillSelectionListener listener) =>
            listeners.remove(listener);

    jtModel.selectionModel = selectionModel;

    rootVisible = false;

    variable Integer i = 0;

    while (i < rowCount) {
        expandRow(i);
    }

    showsRootHandles = true;

    void handleTreeSelectionChange(TreeSelectionEvent event) {
        ISkill? retval;
        if (exists selectionPath = event.newLeadSelectionPath,
            is ISkill component = selectionPath.lastPathComponent) {
            retval = component;
        } else {
            retval = null;
        }
        for (listener in listeners) {
            listener.selectSkill(retval);
        }
    }
    selectionModel.addTreeSelectionListener(handleTreeSelectionChange);

    object treeModelListener satisfies TreeModelListener {
        shared actual void treeStructureChanged(TreeModelEvent event) {
            if (exists treePath = event.treePath,
                    exists parentPath = treePath.parentPath) {
                expandPath(parentPath);
            }
            variable Integer i = 0;
            while (i < rowCount) {
                expandRow(i);
                i++;
            }
        }
        shared actual void treeNodesRemoved(TreeModelEvent event) { }
        shared actual void treeNodesInserted(TreeModelEvent event) {
            expandPath(event.treePath);
            expandPath(event.treePath.parentPath);
        }
        shared actual void treeNodesChanged(TreeModelEvent event) =>
                expandPath(event.treePath.parentPath);
    }
    jtModel.addTreeModelListener(treeModelListener);
}
