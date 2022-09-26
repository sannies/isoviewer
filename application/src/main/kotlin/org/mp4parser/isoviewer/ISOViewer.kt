package org.mp4parser.isoviewer

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import org.mp4parser.isoviewer.views.MainView
import tornadofx.App

class ISOViewer : App(Image("icon.png"), MainView::class, Styles::class) {

    override fun start(stage: Stage) {
        stage.title = "ISO Viewer"
        stage.setOnCloseRequest { Platform.exit() }
        super.start(stage)
        stage.show()
    }
}

/**
 * The main method is needed to support the mvn jfx:run goal.
 */
fun main(args: Array<String>) {
    System.setProperty("apple.laf.useScreenMenuBar", "false");
    Application.launch(ISOViewer::class.java, *args)

}