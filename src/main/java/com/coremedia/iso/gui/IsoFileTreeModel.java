/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;

/**
 * Adapter for an <code>IsoFile</code> to act as a <code>TreeModel</code>
 *
 * @see IsoFile
 * @see TreeModel
 */
public class IsoFileTreeModel implements TreeModel {
    private Container file;

    public IsoFileTreeModel(Container file) {
        this.file = file;
    }

    public Object getRoot() {
        return file;
    }

    public int getChildCount(Object parent) {
        if (parent != null) {
            if (parent instanceof Container) {
                Container container = (Container) parent;
                return container.getBoxes() == null ? 0 : container.getBoxes().size();
            }
        }
        return 0;
    }


    public boolean isLeaf(Object node) {
        return !(node instanceof Container);
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof Container) {
            Container container = (Container) parent;
            return container.getBoxes().get(index);

        }
        return null;
    }

    public int getIndexOfChild(Object parent, Object child) {

        if (parent instanceof Container) {
            Container container = (Container) parent;
            List<Box> boxes = container.getBoxes();
            for (int i = 0; i < boxes.size(); i++) {
                if (boxes.get(i).equals(child)) {
                    return i;
                }
            }
        }

        return 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException();
    }


}
