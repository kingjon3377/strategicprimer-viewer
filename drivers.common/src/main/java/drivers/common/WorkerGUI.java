package drivers.common;

/**
 * An interface for GUIs that present units in a map in a hierarchical interface.
 */
public interface WorkerGUI extends GUIDriver {
	@Override
	IWorkerModel getModel();
}
