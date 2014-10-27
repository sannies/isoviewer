/*
 * Copyright 2014 Sebastian Annies
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

package com.github.sannies.isoviewer;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.github.sannies.isoviewer.hex.JHexEditor;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import com.googlecode.mp4parser.util.Path;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class AddBoxTabListener implements ChangeListener<TreeItem<Box>> {
    private final TabPane tabPane;

    public AddBoxTabListener(TabPane tabPane) {
        this.tabPane = tabPane;
    }


    public void changed(ObservableValue<? extends TreeItem<Box>> observableValue, TreeItem<Box> boxTreeItem, TreeItem<Box> boxTreeItem2) {
        if (boxTreeItem2 == null) {
            return;
        }
        String id = Path.createPath(boxTreeItem2.getValue());
        Tab t = null;
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getId().equals(id)) {
                tabPane.getSelectionModel().select(tab);
                t = tab;
            }
        }
        if (t == null) {
            t = new Tab(id);
            t.setId(id);
            tabPane.getTabs().addAll(t);

            BoxPane bp = new BoxPane(boxTreeItem2.getValue());
            SplitPane sp = new SplitPane();
            sp.setOrientation(Orientation.VERTICAL);
            sp.getItems().add(bp);

            SwingNode hexEditorSwingNode = new SwingNode();
            ByteBuffer displayMe;
            Object object = boxTreeItem2.getValue();
            if (!(object instanceof IsoFile)) {
                long sizeToDisplay = ((Box) object).getSize();
                if (sizeToDisplay > Integer.MAX_VALUE) {
                    displayMe = ByteBuffer.wrap("TOO LARGE TO DISPLAY".getBytes());
                } else {
                    displayMe = ByteBuffer.allocate(l2i(sizeToDisplay));
                    try {
                        ((Box) object).getBox(new ByteBufferByteChannel(displayMe));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            } else  {
                displayMe = ByteBuffer.allocate(0);
            }
            hexEditorSwingNode.setContent(new JHexEditor(displayMe));
            sp.getItems().add(hexEditorSwingNode);

            t.setContent(sp);
            tabPane.getSelectionModel().select(t);
        }


    }

}
