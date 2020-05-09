import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import org.mp4parser.isoviewer.Styles
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