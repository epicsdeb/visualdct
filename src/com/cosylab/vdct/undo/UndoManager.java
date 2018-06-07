package com.cosylab.vdct.undo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.cosylab.vdct.Console;
import com.cosylab.vdct.Constants;
import com.cosylab.vdct.events.CommandManager;
import com.cosylab.vdct.events.commands.GetDsManager;
import com.cosylab.vdct.events.commands.SetRedoMenuItemState;
import com.cosylab.vdct.events.commands.SetUndoMenuItemState;
import com.cosylab.vdct.graphics.DsEventListener;
import com.cosylab.vdct.graphics.DsManager;

/**
 * Copyright (c) 2002, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution. 
 * Neither the name of the Cosylab, Ltd., Control System Laboratory nor the names
 * of its contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * This type was created in VisualAge.
 */
public class UndoManager implements DsEventListener {

	protected static HashMap instances = new HashMap();
	
	private final int lowerbound = -1;
	
	protected Object dsId = null;
	
	private int pos;
	private int first, last; 
	private int bufferSize;
	private boolean bufferSizeReached = false;
	private int savedOnPos = lowerbound;
	private int actionsAfterSave = 0;
	private ActionObject[] actions;

	private boolean monitor = false;

	private ComposedAction composedAction = null;
	private int macroActionsCalls = 0;
	
	private Vector macroActionListeners = null;

/**
 * UndoManager constructor comment.
 */
protected UndoManager(Object dsId) {
	this.dsId = dsId;
	macroActionListeners = new Vector();
	bufferSize = Constants.UNDO_STEPS_TO_REMEMBER;
	actions = new ActionObject[bufferSize];
	reset();
}
/**
 * This method was created in VisualAge.
 * @return int
 */
private int actions2redo() {
	int redos = 0;
	int np = pos;
	while (np!=last) {
		redos++;
		np = increment(np);
	}
	return redos;
}
/**
 * This method was created in VisualAge.
 * @return int
 */
private int actions2undo() {
	if (pos==lowerbound) return 0;
	
	int undos = 1;
	int np = pos;
	while (np!=first) {
		undos++;
		np = decrement(np);
	}
	return undos;
}
/**
 * This method was created in VisualAge.
 * @param action epics.undo.ActionObject
 */
public void addAction(ActionObject action) {
    
	if (!monitor) return;

	if (composedAction!=null)
	{
		composedAction.addAction(action);
		//System.out.println("Composing: "+action.getDescription());
		return;
	}
	
	if (actionsAfterSave >= 0 && actionsAfterSave <= bufferSize){
	    actionsAfterSave++;
	    if (actionsAfterSave >= bufferSize)
	        bufferSizeReached = true;
	} else {
	    bufferSizeReached = true;
	}

	//System.out.println("New action: "+action.getDescription());
	DsManager.getDrawingSurface(dsId).setModified(true);

	if (pos==lowerbound) pos=last=increment(pos);
	else {
		pos=last=increment(pos);
		if (last==first) first=increment(first);		// lose first (the "oldest" action)
	}
	actions[pos]=action;

	int np = increment(last);							// clear lost actions -> finalization!
	while (np!=first) {
		actions[np]=null;
		np=increment(np);
	}
	
	updateMenuItems();
}
/**
 * This method was created in VisualAge.
 * @return int
 * @param pos int
 */
private int decrement(int pos) {
	if (pos==first) return lowerbound;
	else {
		int np = pos-1;
		if (np<0) np=bufferSize-1;
		return np;
	}
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 20:45:26)
 * @return com.cosylab.vdct.undo.ComposedActionInterface
 */
public ComposedActionInterface getComposedAction() {
	return composedAction;
}

public static UndoManager getInstance(Object dsId) {

	UndoManager undoManager = (UndoManager)instances.get(dsId);
    if (undoManager == null) {
    	//System.err.println("Warning: UndoManager.getInstance: instance with id does not exist, creating new one.");
    	undoManager = new UndoManager(dsId);
    	instances.put(dsId, undoManager);
    }
	return undoManager;
}

/**
 * This method was created in VisualAge.
 * @return int
 * @param pos int
 */
private int increment(int pos) {
    if (pos==lowerbound) return first;
	else return ((pos+1) % bufferSize);
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 15:36:00)
 * @return boolean
 */
public boolean isMonitor() {
	return monitor;
}
/**
 * This method was created in VisualAge.
 */
public void redo() {

	if (composedAction != null) {
		Console.getInstance().println("Warning: UndoManager: redo(): macro action in progress.");
		packComposedAction();
	}
	
	if (pos!=last) {
		boolean m = monitor;
		monitor = false;
		pos=increment(pos);
		
		fireMacroActionStart();
		actions[pos].redo();
		fireMacroActionStop();
		
		//System.out.println("Redo: "+actions[pos].getDescription());
		DsManager.getDrawingSurface(dsId).setModified(true);
		updateMenuItems();
		monitor = m;
		setModification();
		actionsAfterSave++;
	}
}
/**
 * This method was created in VisualAge.
 */
public void reset() {

	if (composedAction != null) {
		Console.getInstance().println("Warning: UndoManager: reset(): macro action in progress.");
		packComposedAction();
	}
	
	first=0;
	pos=last=lowerbound;
	for (int i=0; i < bufferSize; i++)
		actions[i]=null;
	monitor = false;
	bufferSizeReached=false;
	updateMenuItems();
	prepareAfterSaving();
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 15:36:00)
 * @param newMonitor boolean
 */
public void setMonitor(boolean newMonitor) {
	monitor = newMonitor;
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 20:43:57)
 */
public void startMacroAction() {

	macroActionsCalls++;
	if (macroActionsCalls > 1) {
		return;
	}
	
	if (composedAction != null) {
		Console.getInstance().println("Warning: UndoManager: startMacroAction(): macro action not stopped.");
		packComposedAction();
	}
	composedAction = new ComposedAction();
	fireMacroActionStart();
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 20:44:21)
 */
public void stopMacroAction() {

	macroActionsCalls--;
	if (macroActionsCalls > 0) {
		return;
	}
	
	if (composedAction == null) {
		Console.getInstance().println("Warning: UndoManager: stopMacroAction(): macro action not started.");
		return;
	}
	packComposedAction();
	fireMacroActionStop();
	//System.out.println("Stopped composing");
}
/**
 * This method was created in VisualAge.
 */
public void undo() {

	if (composedAction != null) {
		Console.getInstance().println("Warning: UndoManager: undo(): macro action in progress.");
		packComposedAction();
	}

	if (pos!=lowerbound)  {
		boolean m = monitor;
		monitor = false;
		
		fireMacroActionStart();
		actions[pos].undo();
		fireMacroActionStop();
		
		//System.out.println("Undo: "+actions[pos].getDescription());
		pos=decrement(pos);
		DsManager.getDrawingSurface(dsId).setModified(true);
		updateMenuItems();
		monitor = m;
		setModification();
		actionsAfterSave--;
	}
}

/**
 * 
 * Sets the modified tag according to the state of the opened template. If the template has
 * (by undoing) became the same as the one that is saved tag is turned to false or 
 * else it is true.
 *
 */
private void setModification() {
	DsManager.getDrawingSurface(dsId).setModified(pos != savedOnPos || bufferSizeReached);
}

/**
 * 
 * Sets all the counters after a file has been saved.
 *
 */
public void prepareAfterSaving() {
    savedOnPos = pos; 
    bufferSizeReached = false;
    actionsAfterSave = 0;
}

public void addMacroActionEventListener(MacroActionEventListener listener) {
	macroActionListeners.add(listener);
}
public void removeMacroActionEventListener(MacroActionEventListener listener) {
	macroActionListeners.remove(listener);
}

private void fireMacroActionStart() {
	Iterator iterator = macroActionListeners.iterator();
	while (iterator.hasNext()) {
	    ((MacroActionEventListener)iterator.next()).macroActionStarted();
	}
}
private void fireMacroActionStop() {
	Iterator iterator = macroActionListeners.iterator();
	while (iterator.hasNext()) {
	    ((MacroActionEventListener)iterator.next()).macroActionStopped();
	}
}

private void packComposedAction() {
	ComposedAction action = composedAction; 
	composedAction = null;
	if (!action.isEmpty()) {
		addAction(action);
	}
}

public static void registerDsListener() {

    UndoManager manager = new UndoManager(Constants.DEFAULT_NAME);
	instances.put(Constants.DEFAULT_NAME, manager);
	
	GetDsManager command = (GetDsManager)CommandManager.getInstance().getCommand("GetDsManager");
	if (command != null) {
		command.getManager().addDsEventListener(manager);
	}
}

public void onDsAdded(Object id) {
    instances.put(id, new UndoManager(id));
}
public void onDsRemoved(Object id) {
}
public void onDsFocused(Object id) {
	((UndoManager)instances.get(id)).updateMenuItems();
}

private void updateMenuItems() {
	SetRedoMenuItemState cmd = (SetRedoMenuItemState)CommandManager.getInstance().getCommand("SetRedoMenuItemState");
	if (cmd != null) {
		cmd.setState(actions2redo() > 0);
		cmd.execute();
	}
	SetUndoMenuItemState cmd2 = (SetUndoMenuItemState)CommandManager.getInstance().getCommand("SetUndoMenuItemState");
	if (cmd2 != null) {
		cmd2.setState(actions2undo() > 0);
		cmd2.execute();
	}
}

}
