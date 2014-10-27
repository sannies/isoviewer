package com.googlecode.mp4parser.isoviewer;

import com.coremedia.iso.gui.IsoViewerPanel;
import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by sannies on 27.10.2014.
 */
public class IsoViewerFx  extends Application {
    IsoViewerPanel isoViewerPanel;
    @Override
    public void start(Stage stage) throws Exception {
        BorderPane hBox = new BorderPane();
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        Menu fileMenu = new Menu();
        fileMenu.setText("_File");
        MenuItem openMenuItem = new MenuItem("_Open");
        openMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        fileMenu.getItems().addAll(openMenuItem);
        menuBar.getMenus().addAll(fileMenu);
        hBox.setTop(menuBar);

        isoViewerPanel = new IsoViewerPanel();
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(isoViewerPanel);
        hBox.setCenter(swingNode);

        Scene scene = new Scene(hBox, 450, 100);
        stage.setScene(scene);
        stage.show();

        openMenuItem.setOnAction(new FileOpenEventHandler(stage, isoViewerPanel));

    }

    public static void main(String[] args) {
        launch(args);
    }
}
