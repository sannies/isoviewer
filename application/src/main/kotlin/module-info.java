module ISOVIEWER {
    requires javafx.controls;
    requires javafx.base;
    requires tornadofx;
    requires kotlin.stdlib;
    requires isoparser;
    requires java.desktop;
    exports org.mp4parser.isoviewer to javafx.graphics, tornadofx;
    exports org.mp4parser.isoviewer.views to tornadofx;
    exports org.mp4parser.isoviewer.parser to isoparser;

}