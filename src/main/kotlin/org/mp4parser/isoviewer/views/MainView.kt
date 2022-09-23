package org.mp4parser.isoviewer.views

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.mp4parser.Box
import org.mp4parser.Container
import org.mp4parser.isoviewer.BoxParser
import org.mp4parser.isoviewer.Dummy
import org.mp4parser.isoviewer.MyIsoFile
import org.mp4parser.isoviewer.Styles
import org.mp4parser.tools.Hex
import tornadofx.*
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer

class MainView : View("ISO Viewer") {
    var tree: TreeView<Box> by singleAssign()
    var hexLines: ObservableList<ByteArray> = FXCollections.observableArrayList()
    var offsets: MutableMap<Box, Long> = mutableMapOf()
    var raf: RandomAccessFile = RandomAccessFile(File.createTempFile("MainView", ""), "r");
    var details: VBox by singleAssign()

    override val root = borderpane {
        top {
            menubar {
                menu("File") {
                    item("Open", "Shortcut+O").action {
                        val file = chooseFile(filters = arrayOf(
                                FileChooser.ExtensionFilter("MP4 files", "*.mp4", "*.uvu", "*.m4v", "*.m4a", "*.uva", "*.uvv", "*.uvt", "*.mov", "*.m4s", "*.ism?"),
                                FileChooser.ExtensionFilter("All files", "*.*")))
                        if (file.isNotEmpty()) {
                            raf = RandomAccessFile(file[0], "r")
                            runAsync {
                                val isoFile = MyIsoFile(file[0])
                                offsets = mutableMapOf(Pair(isoFile, 0L))
                                hexLines.clear()
                                isoFile.initContainer(FileInputStream(file[0]).getChannel(), file[0].length(), BoxParser(offsets))
                                isoFile
                            } success {
                                tree.root = TreeItem<Box>(it)
                                tree.populate(
                                        childFactory = fun(ti: TreeItem<Box>): List<Box>? {
                                            if (ti.value is Container) {
                                                return (ti.value as Container).boxes
                                            } else {
                                                return null
                                            }
                                        })
                            }
                        }

                    }
                }
            }.isUseSystemMenuBar = System.getProperty("os.name").contains("Mac")

        }
        center {
            splitpane(Orientation.HORIZONTAL) {
                setDividerPositions(0.2)
                tree = treeview {
                    cellFormat { text = it.type }
                    root = TreeItem<Box>(Dummy())
                    onUserSelect { it ->
                        run {
                            hexLines.clear()
                            raf.seek(offsets.getOrDefault(it, 0))
                            var size = it.size
                            ByteBuffer.allocate(16).array()
                            while (size > 0) {
                                val buf = ByteArray(Math.min(16, size.toInt()))
                                if (hexLines.size > 1000) {
                                    break
                                }
                                size -= raf.read(buf)
                                hexLines.add(buf)
                            }
                            details.children.clear()
                            details.children.add(BoxPane(it))
                        }
                    }


                }
                splitpane(Orientation.VERTICAL) {
                    details = vbox {

                    }
                    tableview(hexLines) {
                        addClass(Styles.hexview)

                        selectionModel.isCellSelectionEnabled = true
                        selectionModel.selectionMode = SelectionMode.MULTIPLE

                        column(" 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F", String::class) {
                            value { param ->
                                Hex.encodeHex(param.value).replace(Regex("(.{2})"), "$1 ")
                            }
                            isSortable = false
                            prefWidth = 400.0
                        }
                        column("................", String::class) {
                            isSortable = false
                            prefWidth = 150.0
                            value { param ->
                                var s = ""
                                val itra = param.value.iterator()
                                while (itra.hasNext()) {
                                    val b = itra.nextByte()
                                    s += if (Character.isLetterOrDigit(b.toInt())) {
                                        Character.forDigit(b.toInt(), 10)
                                    } else {
                                        '.'
                                    }
                                }
                                s
                            }
                        }
                    }
                }
            }
        }
    }
}
