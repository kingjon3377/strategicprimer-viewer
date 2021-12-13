"An interface for GUIs that present units in a map in a hierarchical interface."
shared interface WorkerGUI satisfies GUIDriver {
    shared actual formal IWorkerModel model;
}