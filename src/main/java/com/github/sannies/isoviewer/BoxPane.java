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
import com.googlecode.mp4parser.util.Path;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;

/**
 *
 */
public class BoxPane extends TitledPane {
    static Properties names = new Properties();
    private static final Collection<String> skipList = Arrays.asList(
            "class",
            "boxes",
            "deadBytes",
            "type",
            "header",
            "isoFile",
            "parent",
            "content",
            "fileChannel"
    );


    static {
        try {
            names.load(BoxPane.class.getResourceAsStream("/names.properties"));
        } catch (IOException e) {
            // i dont care
            throw new RuntimeException(e);
        }
    }


    Box box;

    public BoxPane(final Box box) {
        this.box = box;
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(box.getClass());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        TableView<PropertyDescriptor> tableView = new TableView<PropertyDescriptor>();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String name = propertyDescriptor.getName();
            if (!skipList.contains(name) &&
                    propertyDescriptor.getReadMethod() != null &&
                    !Box.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                tableView.getItems().addAll(propertyDescriptor);
            }
        }
        TableColumn<PropertyDescriptor, String> propertyTableColumn = new TableColumn<PropertyDescriptor, String>("Property");
        TableColumn<PropertyDescriptor, Node> valueTableColumn = new TableColumn<PropertyDescriptor, Node>("Value");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        propertyTableColumn.setPrefWidth(10);
        propertyTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PropertyDescriptor, String>, ObservableValue<String>>() {

            public ObservableValue<String> call(final TableColumn.CellDataFeatures<PropertyDescriptor, String> propertyDescriptorStringCellDataFeatures) {
                return new StringBinding() {
                    @Override
                    protected String computeValue() {
                        return propertyDescriptorStringCellDataFeatures.getValue().getName();
                    }
                };
            }
        });
        valueTableColumn.setPrefWidth(20);
        valueTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PropertyDescriptor, Node>, ObservableValue<Node>>() {

            public ObservableValue<Node> call(final TableColumn.CellDataFeatures<PropertyDescriptor, Node> propertyDescriptorStringCellDataFeatures) {
                return new ObjectBinding<Node>() {
                    @Override
                    protected Node computeValue() {
                        try {
                            Object o = propertyDescriptorStringCellDataFeatures.getValue().getReadMethod().invoke(box);
                            if (o instanceof List) {
                                int listSize = ((List) o).size();

                                if (listSize < 5) {
                                    TextField t = new TextField(o.toString());
                                    t.setEditable(false);
                                    return t;
                                } else {
                                    ListView<Object> lv = new ListView<Object>(new ObservableListWrapper<Object>((List<Object>) o));
                                    lv.setMinHeight(60);
                                    int size = listSize > 0 ? ((List) o).get(0).toString().split("\r\n|\r|\n").length : 1;


                                    lv.setPrefHeight(((List) o).size() * 15 * size + 20);
                                    lv.setMaxHeight(200);

                                    TitledPane tp = new TitledPane("List contents (" + ((List) o).size() + ")", lv);
                                    tp.setExpanded(false);
                                    return tp;

                                }


                            } else if (o != null && o.getClass().isArray()) {
                                int length = Array.getLength(o);

                                if (length < 5) {
                                    String v = "[";
                                    for (int i = 0; i < length; i++) {
                                        v += Array.get(o, i);
                                        v += ", ";
                                    }
                                    if (length > 2) {
                                        v = v.substring(0, v.length() - 2);
                                    }
                                    v += "]";
                                    TextField t = new TextField(v);
                                    t.setEditable(false);
                                    return t;
                                } else {
                                    List<Object> values = new ArrayList<Object>();
                                    for (int i = 0; i < length; i++) {
                                        Object value = Array.get(o, i);
                                        if (value instanceof Box) {
                                            values.add(Path.createPath((Box) value));
                                        } else {
                                            values.add(value);
                                        }
                                    }

                                    ListView<Object> lv = new ListView<Object>(new ObservableListWrapper<Object>(values));
                                    int size = values.size() > 0 ? values.get(0).toString().split("\r\n|\r|\n").length : 1;
                                    lv.setMinHeight(20);
                                    lv.setPrefHeight(length * 15 * size + 20);
                                    lv.setMaxHeight(200);
                                    TitledPane tp = new TitledPane("Array contents (" + values.size() + ")", lv);
                                    if (length > 3) {
                                        tp.setExpanded(false);
                                    }
                                    return tp;

                                }


                            } else {
                                if ("flags".equals(propertyDescriptorStringCellDataFeatures.getValue().getName())) {
                                    int v = o == null?0:((Integer) o);
                                    String s = "";
                                    for (int i = 0; i < 24; i++) {
                                        if ((v & (1<<i)) > 0) {
                                            s += "0x" + Integer.toHexString(v & (1<<i)) + ", ";
                                        }
                                    }

                                    TextField t = new TextField(s);
                                    t.setEditable(false);
                                    return t;
                                } else {
                                    TextField t = new TextField(o != null ? o.toString() : "null");
                                    t.setEditable(false);
                                    return t;
                                }
                            }
                        } catch (IllegalAccessException e) {
                            return new Text(e.getLocalizedMessage());
                        } catch (InvocationTargetException e) {
                            return new Text(e.getLocalizedMessage());
                        }
                    }
                };
            }
        });

        tableView.getColumns().addAll(propertyTableColumn, valueTableColumn);
        setContent(tableView);
        setCollapsible(false);
        if (box instanceof IsoFile) {
            setText("ISO File");
        } else {
            setText(names.getProperty(box.getType(), "Unknown Box"));
        }
    }
}
