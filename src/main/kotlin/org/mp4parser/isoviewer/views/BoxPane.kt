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

package org.mp4parser.isoviewer.views

import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.text.Text
import javafx.util.Callback
import org.mp4parser.Box

import java.beans.BeanInfo
import java.beans.IntrospectionException
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.io.IOException
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 *
 */
class BoxPane(internal var box: Box) : TitledPane() {

    init {
        var beanInfo: BeanInfo? = null
        try {
            beanInfo = Introspector.getBeanInfo(box.javaClass)
        } catch (e: IntrospectionException) {
            throw RuntimeException(e)
        }

        val propertyDescriptors = beanInfo!!.propertyDescriptors
        val tableView = TableView<PropertyDescriptor>()

        for (propertyDescriptor in propertyDescriptors) {
            val name = propertyDescriptor.name
            if (!skipList.contains(name) &&
                    propertyDescriptor.readMethod != null &&
                    !Box::class.java.isAssignableFrom(propertyDescriptor.propertyType)) {
                tableView.items.addAll(propertyDescriptor)
            }
        }
        val propertyTableColumn = TableColumn<PropertyDescriptor, String>("Property")
        val valueTableColumn = TableColumn<PropertyDescriptor, Node>("Value")
        tableView.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        propertyTableColumn.prefWidth = 10.0
        propertyTableColumn.setCellValueFactory { propertyDescriptorStringCellDataFeatures ->
            object : StringBinding() {
                override fun computeValue(): String {
                    return propertyDescriptorStringCellDataFeatures.value.name
                }
            }
        }
        valueTableColumn.prefWidth = 20.0
        valueTableColumn.setCellValueFactory { propertyDescriptorStringCellDataFeatures ->
            object : ObjectBinding<Node>() {
                override fun computeValue(): Node {
                    try {
                        val o = propertyDescriptorStringCellDataFeatures.value.readMethod.invoke(box)
                        if (o is List<*>) {
                            val listSize = o.size

                            if (listSize < 5) {
                                val t = TextField(o.toString())
                                t.isEditable = false
                                return t
                            } else {
                                val lv = ListView(ObservableListWrapper(o as List<Any>))
                                lv.minHeight = 60.0
                                val size = if (listSize > 0) o[0].toString().split("\r\n|\r|\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size else 1


                                lv.prefHeight = ((o as List<*>).size * 15 * size + 20).toDouble()
                                lv.maxHeight = 200.0

                                val tp = TitledPane("List contents (" + (o as List<*>).size + ")", lv)
                                tp.isExpanded = false
                                return tp

                            }


                        } else if (o != null && o.javaClass.isArray) {
                            val length = Array.getLength(o)

                            if (length < 5) {
                                var v = "["
                                for (i in 0..length - 1) {
                                    v += Array.get(o, i)
                                    v += ", "
                                }
                                if (length > 2) {
                                    v = v.substring(0, v.length - 2)
                                }
                                v += "]"
                                val t = TextField(v)
                                t.isEditable = false
                                return t
                            } else {
                                val values = ArrayList<Any>()
                                for (i in 0..length - 1) {
                                    val value = Array.get(o, i)
                                    values.add(value)
                                }

                                val lv = ListView(ObservableListWrapper(values))
                                val size = if (values.size > 0) values[0].toString().split("\r\n|\r|\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size else 1
                                lv.minHeight = 20.0
                                lv.prefHeight = (length * 15 * size + 20).toDouble()
                                lv.maxHeight = 200.0
                                val tp = TitledPane("Array contents (" + values.size + ")", lv)
                                if (length > 3) {
                                    tp.isExpanded = false
                                }
                                return tp

                            }


                        } else {
                            if ("flags" == propertyDescriptorStringCellDataFeatures.value.name) {
                                val v = if (o == null) 0 else o as Int
                                var s = ""
                                for (i in 0..23) {
                                    if (v and (1 shl i) > 0) {
                                        s += "0x" + Integer.toHexString(v and (1 shl i)) + ", "
                                    }
                                }

                                val t = TextField(s)
                                t.isEditable = false
                                return t
                            } else {
                                val t = TextField(o?.toString() ?: "null")
                                t.isEditable = false
                                return t
                            }
                        }
                    } catch (e: IllegalAccessException) {
                        return Text(e.localizedMessage)
                    } catch (e: InvocationTargetException) {
                        return Text(e.localizedMessage)
                    }

                }
            }
        }

        tableView.columns.addAll(propertyTableColumn, valueTableColumn)
        content = tableView
        isCollapsible = false
        text = names.getProperty(box.type, "Unknown Box")

    }

    companion object {
        internal var names = Properties()
        private val skipList = Arrays.asList(
                "class",
                "boxes",
                "deadBytes",
                "type",
                "header",
                "isoFile",
                "parent",
                "content",
                "fileChannel"
        )


        init {
            try {
                names.load(BoxPane::class.java.getResourceAsStream("/names.properties"))
            } catch (e: IOException) {
                // i dont care
                throw RuntimeException(e)
            }

        }
    }
}
