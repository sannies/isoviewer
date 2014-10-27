package com.googlecode.mp4parser.isoviewer;

import com.coremedia.iso.gui.IsoViewerPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FileOpenEventHandler implements EventHandler<ActionEvent> {
    Stage stage;
    IsoViewerPanel treeView;

    public FileOpenEventHandler(Stage stage, IsoViewerPanel treeView) {
        this.stage = stage;
        this.treeView = treeView;
    }


    public void handle(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

//Set extension filter
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP4 files", "*.mp4", "*.uvu", "*.m4v"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

//Show open file dialog
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            stage.setTitle(file.getPath());
            try {
                treeView.open(file);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }
}
