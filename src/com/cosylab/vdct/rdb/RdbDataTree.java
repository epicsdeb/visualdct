package com.cosylab.vdct.rdb;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


// Tree Display for IOCs and Epics Groups
public class RdbDataTree extends JTree implements TreeSelectionListener {
    
	public static final boolean debug=false;
    private Vector listeners;
    
    public RdbDataTree() {
        listeners = new Vector();
        
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener(this);
        setPreferredSize(new Dimension(500, 500));
    }

    public void addListener(RdbDataTreeListener l) {
    	listeners.add(l);
    }

    public void removeListener(RdbDataTreeListener l) {
    	listeners.remove(l);
    }

    // interface TreeSelectionListener
    public void valueChanged(TreeSelectionEvent event) {
    	
    	// Idea: Path = {root, ioc, group, version}
        TreePath path = event.getNewLeadSelectionPath();
        if (path == null) { 
            return;
        }    
        int pathLen = path.getPathCount();
        if (pathLen < 2) { 
        	// only root selected
            return;
        }

        String iocId = path.getPathComponent(1).toString();
        
        if (debug) {
            System.out.println("Selected IOC  : " + iocId);
        }
        
        if (pathLen == 2) {
        	// only ioc selected
            for (int i = 0; i < listeners.size(); i++) {
                ((RdbDataTreeListener)listeners.get(i)).iocSelected(iocId);
            }
            return;
        }
        
        String groupId = path.getPathComponent(2).toString();
        if (debug) {
            System.out.println("Selected Group: " + groupId);
        }

        if (pathLen == 3) {
            for (int i = 0; i < listeners.size(); i++) {
                ((RdbDataTreeListener)listeners.get(i)).groupSelected(iocId, groupId);
            }
            return;
        }
        
        String version = path.getPathComponent(3).toString();
        if (debug) {
            System.out.println("Selected version: " + version);
        }

        if (pathLen == 4) {
            for (int i = 0; i < listeners.size(); i++) {
                ((RdbDataTreeListener)listeners.get(i)).versionSelected(iocId, groupId, version);
            }
        }
    }
};

