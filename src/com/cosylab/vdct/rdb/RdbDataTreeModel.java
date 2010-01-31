/**
 * Copyright (c) 2008, Cosylab, Ltd., Control System Laboratory, www.cosylab.com
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

package com.cosylab.vdct.rdb;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 Implements TreeModel in order to support
 late evaluation:
 "Children" are only fetched when asked for.
 If this had been based on the DefaultTreeModel,
 the whole group database would have to be read
 into the model from the very beginning,
 which could be a huge amount of data.

 Root is just a Vector (of IOCNodes).
 Currently only difference to Vector: toString representation
 Default was toString of all the elements
*/

public class RdbDataTreeModel implements TreeModel {
    
	private Vector tree_model_listeners;
    private RootNode root; // IOCNode Vector
    
    private RdbDataMapper mapper = null;
    
    public static final boolean debug=false;
    
    class RootNode extends Vector
    {
        public String toString()
        {   return "IOCs/Groups"; }
    };
    
    // IOCNode is the one level below root for the IOCs.
    // Children of each IOC are groups
    class IOCNode
    {
        private Vector groups;
        private String ioc_id;
            
        public IOCNode(String id)
        {
            ioc_id = id;
            groups = null;
        }

        private void load() {
            try {
                Iterator iterator = mapper.getRdbDatas(ioc_id).iterator();
                
                groups = new Vector();
                while (iterator.hasNext()) {
                	groups.add(new GroupNode(iterator.next().toString(), ioc_id));
                }
            }
            catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
            }
        }
        
        public String toString() {
        	return ioc_id;
        }
            
        public int getChildCount() {
            if (groups == null)
                load();
            return (groups != null) ? groups.size() : 0;
        }
            
        public Object getChild(int index) {
            if (groups == null)
                load();
            return (groups != null) ? groups.get(index) : null;
        }
            
        public int getIndexOfChild(Object child) {
            if (groups == null)
                load();
            return (groups != null) ? groups.indexOf(child) : 0;
        }
    };

    /*
     * GroupNode is  the one level below IOCNode.
     * Children of each IOC are versions.
     */
    class GroupNode {
        
    	private Vector versions = null;
        private String groupId;
            
        public GroupNode(String id, String iocId) {
            groupId = id;
            try {
                versions = mapper.getVersions(groupId, iocId);
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }

        public String toString() {
        	return groupId;
        }
        public int getChildCount() {
            return (versions != null) ? versions.size() : 0;
        }
        public Object getChild(int index) {
            return (versions != null) ? versions.get(index) : null;
        }
        public int getIndexOfChild(Object child) {
            return (versions != null) ? versions.indexOf(child) : 0;
        }
    };
    
    public RdbDataTreeModel(RdbDataMapper mapper) {
        this.mapper = mapper;
    	
    	tree_model_listeners = new Vector();
        root = new RootNode();
        
        try {
        	// Initialize Model: get all IOCs	
            Iterator iterator = mapper.getIocs().iterator();

            // Create an ioc if none exist. Temporary hack. 
            if (!iterator.hasNext()) {
            	mapper.createAnIoc();
            	iterator = mapper.getIocs().iterator();
            }
            
            while (iterator.hasNext()) {
            	root.add(new IOCNode((String)iterator.next()));
            }
        } catch (SQLException e) {
        	e.printStackTrace();
        }
    }

    public Object getRoot() {
    	return root;
    }
    
    public int getChildCount(Object parent) {
        if (parent == root) {
            return root.size();
        } else if (parent instanceof IOCNode) {
            return ((IOCNode)parent).getChildCount();
        } else if (parent instanceof GroupNode) {
            return ((GroupNode)parent).getChildCount();
        }
        return 0;
    }
    
    public Object getChild(Object parent, int index)
    {
        if (debug) {
            System.out.println ("getChild(" + parent.toString() + ", " + index + ")");
        }
        if (parent == root) {
            return root.get(index);
        } else if (parent instanceof IOCNode) {
            return ((IOCNode)parent).getChild(index);
        } else if (parent instanceof GroupNode) {
            return ((GroupNode)parent).getChild(index);
        }
        return null;
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        if (debug) {
            System.out.println ("getIndexOfChild(" + parent.toString() + ", " + child.toString() + ")");
        }
        if (parent == root) {
            return root.indexOf(child);
        } else if (parent instanceof IOCNode) {
            return ((IOCNode)parent).getIndexOfChild(child);
        } else if (parent instanceof GroupNode) {
            return ((GroupNode)parent).getIndexOfChild(child);
        }
        return 0;
    }
    
    public boolean isLeaf(Object node) {
    	return node != root && !(node instanceof IOCNode) && !(node instanceof GroupNode);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("valueForPathChanged:");
        System.out.println("Path: " + path.toString());
        System.out.println("Value: " + newValue.toString());
    }
    
    public void addTreeModelListener(TreeModelListener l) {
    	tree_model_listeners.add(l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
    	tree_model_listeners.remove(l);
    }
}
