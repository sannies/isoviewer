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
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.util.Path;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.io.IOException;
import java.util.*;

public class IsoFileTreeView extends TreeView<Box> {

    public IsoFileTreeView() {
        setCellFactory(new Callback<TreeView<Box>,TreeCell<Box>>(){
            public TreeCell<Box> call(TreeView<Box> p) {
                return new BoxTreeCellImpl();
            }
        });
        setShowRoot(false);
        setEditable(false);
    }

    public void loadIsoFile(final IsoFile isoFile) throws IOException {

        setRoot(new TreeItem<Box>() {
            public boolean isFirstTimeChildren = true;

            @Override
            public boolean isLeaf() {
                return false;
            }

            public ObservableList<TreeItem<Box>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    buildChildren(super.getChildren());
                }
                return super.getChildren();
            }

            private void buildChildren(final ObservableList<TreeItem<Box>> children) {

                    final Iterator<Box> boxes = isoFile.getBoxes().iterator();
                    int i = 100;
                    while (boxes.hasNext() && i-- > 0) {
                        // show 100 now and the rest later
                        children.add(createNode(boxes.next()));
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            while (boxes.hasNext()) {
                                int i = 100;
                                List<TreeItem<Box>> treeItemList = new LinkedList<TreeItem<Box>>();
                                while (boxes.hasNext() && i-- > 0) {
                                    // show 100 now and the rest later
                                    treeItemList.add(createNode(boxes.next()));
                                }
                                final TreeItem<Box> treeItems[] = treeItemList.toArray(new TreeItem[treeItemList.size()]);
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        children.addAll(treeItems);
                                    }
                                });
                            }
                        }
                    }.start();
                }


        });

    }


    public TreeItem<Box> createNode(final Box b) {
        return new TreeItem<Box>(b) {

            private boolean isLeaf;

            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<Box>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    buildChildren(super.getChildren());
                }
                return super.getChildren();
            }


            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;

                    isLeaf = !(getValue() instanceof Container);
                }

                return isLeaf;
            }

            private void buildChildren(final ObservableList<TreeItem<Box>> children) {
                Box b = this.getValue();
                if (b != null && b instanceof Container) {
                    final Iterator<Box> boxes = ((Container) b).getBoxes().iterator();
                    int i = 100;
                    while (boxes.hasNext() && i-- > 0) {
                        // show 100 now and the rest later
                        children.add(createNode(boxes.next()));
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            while (boxes.hasNext()) {
                                int i = 100;
                                List<TreeItem<Box>> treeItemList = new LinkedList<TreeItem<Box>>();
                                while (boxes.hasNext() && i-- > 0) {
                                    // show 100 now and the rest later
                                    treeItemList.add(createNode(boxes.next()));
                                }
                                final TreeItem<Box> treeItems[] = treeItemList.toArray(new TreeItem[treeItemList.size()]);
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        children.addAll(treeItems);
                                    }
                                });
                            }
                        }
                    }.start();

                }
            }
        };
    }

    private final class BoxTreeCellImpl extends TreeCell<Box> {

        public BoxTreeCellImpl() {
        }


        @Override
        public void updateItem(Box item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(getString());
                setGraphic(getTreeItem().getGraphic());
            }
        }

        private String getString() {
            return getItem() == null ? "" : Path.createPath(getItem());
        }
    }
}
