package org.mp4parser.isoviewer

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import org.mp4parser.isoviewer.views.MainView
import tornadofx.App
import tornadofx.FX

class ISOViewer: App(Image("icon.png"), MainView::class, Styles::class) {
    override fun start(stage: Stage) {

        stage.title = "ISO Viewer"
        super.start(stage)

        trayicon(resources.stream("/icon.png")) {
            setOnMouseClicked(fxThread = true) {
                FX.primaryStage.show()
                FX.primaryStage.toFront()
            }

            menu("MyApp") {
                item("Show...") {
                    setOnAction(fxThread = true) {
                        FX.primaryStage.show()
                        FX.primaryStage.toFront()
                    }
                }
                item("Exit") {
                    setOnAction(fxThread = true) {
                        Platform.exit()
                    }
                }
            }
        }
    }
}

/**
 * The main method is needed to support the mvn jfx:run goal.
 */
fun main(args: Array<String>) {
    System.setProperty("apple.laf.useScreenMenuBar", "false");
    Application.launch(ISOViewer::class.java, *args)

}