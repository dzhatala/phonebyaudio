package hatukau.speech;

public class UndoInfo {

	static int NO_UNDO = 0;
	static int UNDO_KEYS = 1; // undoing multiple keys stroked..;
	static int REDO_KEYS = 2; // undoof undo_keys

	int lastUndoType = NO_UNDO;
	int numKeyToUndo;
	int[] keys2Redo = null;
	int[] keys2Undo = null;

	/**
	 * get last number of key stroked so undo is nothing more than to erase ;
	 * 
	 * @return
	 */
	int getNumUndoKeys() {
		return numKeyToUndo;
	}

	void postUndoKeys(int[] keys, int numKeys) {
		lastUndoType = UNDO_KEYS;
		numKeyToUndo = numKeys;
		keys2Undo = new int[numKeys];
		for (int i = 0; i < numKeys; i++) {
			keys2Undo[i] = keys[i];
		}

	}

	public int getLastUndoType() {
		return lastUndoType;
	}

	/**
	 * undo of undo keys
	 */
	void postRedoKeys(final int[] keys, int length) {
		lastUndoType = REDO_KEYS;
		keys2Redo = new int[length];
		for (int i = 0; i < length; i++) {
			keys2Redo[i] = keys[i];
		}
	}

	public int[] getKeys2Redo() {
		return keys2Redo;
	}

	public int[] getKeys2Undo() {
		return keys2Undo;
	}

}
