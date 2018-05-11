import javax.swing.event {
    TreeModelListener,
    TreeModelEvent
}
import javax.swing {
    JTree
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.model.map.fixtures.mobile.worker {
    ISkill
}
"A tree representing a worker's Jobs and Skills."
class JobsTree(JobTreeModel jtModel) extends JTree(jtModel) satisfies SkillSelectionSource {
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
	selectionModel.addTreeSelectionListener((event) { // TODO: If this becomes a class again, make a class method and use silentListener().
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
	});
	object treeModelListener satisfies TreeModelListener {
		shared actual void treeStructureChanged(TreeModelEvent event) {
			expandPath(event.treePath.parentPath);
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
