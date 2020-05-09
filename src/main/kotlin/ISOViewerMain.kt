import javafx.application.Application

// https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
class ISOViewerMain

fun main(args: Array<String>) {
    System.setProperty("apple.laf.useScreenMenuBar", "false");
    Application.launch(ISOViewer::class.java, *args)
}