package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.util.Path;
import org.jdesktop.application.session.PropertySupport;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class BoxJTree extends JTree implements PropertySupport {
    public BoxJTree() {
        setRootVisible(false);
        setLargeModel(true);
        setName("boxTree");
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof Box) {
            return ((Box) value).getType();
        } else {
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    public Object getSessionState(Component c) {
        Enumeration<TreePath> treePathEnumeration = this.getExpandedDescendants(new TreePath(this.getModel().getRoot()));
        java.util.List<String> openPath = new LinkedList<String>();
        Path oldMp4Path = null;
        if (treePathEnumeration != null) {


            while (treePathEnumeration.hasMoreElements()) {
                TreePath treePath = treePathEnumeration.nextElement();
                openPath.add(Path.createPath((Box) treePath.getLastPathComponent()));
            }
        }
        return openPath;

    }

    public void setSessionState(Component c, Object state) {
        LinkedList<String> openPath = (LinkedList<String>) state;

        if (!openPath.isEmpty()) {

            for (String s : openPath) {
                Object expanded = Path.getPath((Box) this.getModel().getRoot(), s);
                List path = new LinkedList();
                while (expanded instanceof Box) {
                    path.add(expanded);
                    expanded = ((Box) expanded).getParent();
                }
                if (path.size() > 0) {
                    Collections.reverse(path);
                    TreePath tp = new TreePath(path.toArray());
                    this.expandPath(tp);
                }
            }
        }
    }


}
