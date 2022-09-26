package org.mp4parser.isoviewer.views

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.VBox
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import org.mp4parser.Box
import org.mp4parser.Container
import org.mp4parser.isoviewer.BoxParser
import org.mp4parser.isoviewer.Dummy
import org.mp4parser.isoviewer.MyIsoFile
import org.mp4parser.isoviewer.Styles
import org.mp4parser.isoviewer.util.Toast
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
    var pathsOfBoxes: MutableMap<Box, String> = mutableMapOf()
    var raf: RandomAccessFile = RandomAccessFile(File.createTempFile("MainView", ""), "r");
    var details: VBox by singleAssign()
    var openedFile: File = File("");
    var selectedBoxPath: String? = null

    override fun onBeforeShow() {
        // try loading file from command line parameters if set
        if (app.parameters.unnamed.size == 1) {
            try {
                loadIso(File(app.parameters.unnamed[0]), false)
            } catch (e: Exception) {
                // todo: popup exception
            }
        }
    }

    fun loadIso(file: File, reopenBox: Boolean) {
        raf = RandomAccessFile(file, "r")
        openedFile = file
        runAsync {
            val isoFile = MyIsoFile(file)
            offsets = mutableMapOf(Pair(isoFile, 0L))
            hexLines.clear()
            isoFile.initContainer(FileInputStream(file).getChannel(), file.length(), BoxParser(offsets))
            pathsOfBoxes = resolvePathsForBoxes(isoFile, emptyList(), mutableMapOf())
            Platform.runLater {
                hexLines.clear()
                details.children.clear()
            }

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

            if (reopenBox) {
                // re-open previously opened box if same path exists
                val path = selectedBoxPath
                if (path != null) {
                    if (pathsOfBoxes.containsValue(path)) {
                        Platform.runLater {
                            expandTreeView(path, tree.root)
                        }
                    }
                }
            }
        }
    }

    private fun expandTreeView(fullPath: String, node: TreeItem<Box>) {
        node.expandTo(1)

        val children = node.children
        for (child in children) {
            val childPath = pathsOfBoxes[child.value as Box] ?: continue
            if (fullPath == childPath) {
                tree.selectionModel.select(child)
                break
            } else if (fullPath.startsWith(childPath)) {
                expandTreeView(fullPath, child)
            }
        }
    }

    fun displayDetails(box: Box) {
        hexLines.clear()
        raf.seek(offsets.getOrDefault(box, 0))
        var size = box.size
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
        details.children.add(BoxPane(box))
        selectedBoxPath = pathsOfBoxes.get(box)
    }

    fun resolvePathsForBoxes(container: Container, chain: List<String>, results: MutableMap<Box, String>): MutableMap<Box, String> {
        for (box in container.boxes) {
            val boxes = container.getBoxes(box.javaClass)

            var chainHere = if (boxes.size > 1) {
                chain + (box.type + "[" + boxes.indexOf(box) + "]");
            } else {
                chain + box.type;
            }

            results.put(box, chainHere.joinToString(separator = "/"))

            if (box is Container) {
                resolvePathsForBoxes(box, chainHere, results)
            }
        }
        return results
    }

    fun copyHex() {

        val path = selectedBoxPath
        if (path != null) {
            if (pathsOfBoxes.containsValue(path)) {
                val reversed = pathsOfBoxes.entries.associate { (k, v) -> v to k }
                val box = reversed[path] ?: Dummy()
                raf.seek(offsets.getOrDefault(box, 0))
                var size = box.size

                val content = ByteArray(Math.min(size, 10000L).toInt())
                raf.readFully(content)
                val clipboard: Clipboard = Clipboard.getSystemClipboard()
                val cc = ClipboardContent()
                // doesn't have to be efficient
                cc.putString(content.hex.chunked(2).joinToString(separator = " ").chunked(32 + 16).joinToString(separator = "\n"))
                clipboard.setContent(cc)

                val toastText = if (box.size > 1000) "Copied trimmed" else "Copied";
                Toast.makeText(currentStage, toastText, 50, 200, 200)
            }
        } else {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = "Problem"
            alert.headerText = "Misssing selection"
            alert.contentText = "Select box from the tree menu on the left."
            alert.showAndWait()
        }
    }

    override val root = borderpane {
        top {
            menubar {
                menu("File") {
                    item("Open", "Shortcut+O").action {
                        val file = chooseFile(initialDirectory = openedFile.parentFile, filters = arrayOf(
                                FileChooser.ExtensionFilter("MP4 files", "*.mp4", "*.uvu", "*.m4v", "*.m4a", "*.uva", "*.uvv", "*.uvt", "*.mov", "*.m4s", "*.ism?"),
                                FileChooser.ExtensionFilter("All files", "*.*")))
                        if (file.isNotEmpty()) {
                            loadIso(file[0], false)
                        }
                    }

                    item("Reload", "Shortcut+R").action {
                        loadIso(openedFile, true)
                    }
                    item("Copy hex", "Shortcut+C").action {
                        copyHex()
                    }
                    separator()

                    item("Exit", "Shortcut+Q").action {
                        Platform.exit()
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
                            displayDetails(it)
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
                            addClass("")
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
