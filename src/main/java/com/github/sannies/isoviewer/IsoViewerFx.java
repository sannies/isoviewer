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

package com.github.sannies.isoviewer;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReaderVariable;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.SchemeTypeBox;
import com.coremedia.iso.boxes.TrackBox;
import com.github.sannies.isoviewer.hex.JHexEditor;
import com.googlecode.mp4parser.authoring.CencMp4TrackImplImpl;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.util.Path;
import com.mp4parser.iso14496.part15.AvcConfigurationBox;
import com.mp4parser.iso14496.part15.HevcConfigurationBox;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.stage.Screen;
import javafx.scene.Group;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import javafx.geometry.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.prefs.Preferences;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 27.10.2014.
 */
public class IsoViewerFx extends Application {
    Preferences userPrefs = Preferences.userNodeForPackage(getClass());
    IsoFileTreeView isoFileTreeView;
    Stage stage;
    TabPane boxesOrTracksTabPane;
    SplitPane boxTreeAndDetails;


    public void openFile(File f) throws IOException {
        IsoFile isoFile = new IsoFile(f.getAbsolutePath());
        stage.setTitle(f.getAbsolutePath());

        boxesOrTracksTabPane.getTabs().clear();
        boxesOrTracksTabPane.getTabs().add(createBoxAndDetailTab(isoFile));

        if (isoFile.getMovieBox() != null) {
            List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);

            for (TrackBox trackBox : trackBoxes) {
                SchemeTypeBox schm = Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schm[0]");
                if (schm != null && (schm.getSchemeType().equals("cenc") || schm.getSchemeType().equals("cbc1"))) {
                    boxesOrTracksTabPane.getTabs().add(
                            createTrackTab(new CencMp4TrackImplImpl(f.getName() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox)));
                } else {
                    boxesOrTracksTabPane.getTabs().add(
                            createTrackTab(new Mp4TrackImpl(f.getName() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox)));
                }
            }

        }
        /*userPrefs.put("openedFile", f.getAbsolutePath());*/
    }

    @SuppressWarnings("restriction")
	@Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
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


        boxesOrTracksTabPane = new TabPane();
        boxesOrTracksTabPane.setSide(Side.LEFT);


        hBox.setCenter(boxesOrTracksTabPane);
        Scene scene = new Scene(hBox, 450, 300);
//        stage.setScene(scene);
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                
                final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".mp4")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".uvu")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".m4v")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".m4a")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".uva")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".uvv")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".uvt")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".mov")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".isma")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".ismv")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".m4s")
                        || db.getFiles().get(0).getName().toLowerCase().endsWith(".ism");
                if (db.hasFiles()) {
                	if (isAccepted) {
                        event.acceptTransferModes(TransferMode.COPY);
                    }
                } else {
                    event.consume();
                }
            }
        });
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                    }
                    try {
                        openFile(new File(filePath));
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icon.png")));
        stage.setScene(scene);
        loadPosAndSize();
        stage.show();

        openMenuItem.setOnAction(new FileOpenEventHandler(stage, this));

    }

    Tab createBoxAndDetailTab(IsoFile isoFile) throws IOException {
        Tab treeAndBoxDetailsTab = new Tab("Box Structure");
        treeAndBoxDetailsTab.setClosable(false);
        boxTreeAndDetails = new SplitPane();
        treeAndBoxDetailsTab.setContent(boxTreeAndDetails);
        IsoFileTreeView left = new IsoFileTreeView();
        left.loadIsoFile(isoFile);
        boxTreeAndDetails.getItems().add(left);
        TabPane right = new TabPane();
        boxTreeAndDetails.getItems().add(right);
        boxTreeAndDetails.setDividerPositions(0.3);
        left.getSelectionModel().selectedItemProperty().addListener(new AddBoxTabListener(right));

        this.isoFileTreeView = left;
        return treeAndBoxDetailsTab;
    }

    Tab createTrackTab(Track track) {
        final Tab trackTab = new Tab("Track " + track.getTrackMetaData().getTrackId());
        trackTab.setClosable(false);
        final SplitPane trackTabSplitPane = new SplitPane();
        trackTabSplitPane.setDividerPositions(0.3);
        trackTab.setContent(trackTabSplitPane);


        ListView<Sample> left = new ListView<Sample>(FXCollections.observableList(track.getSamples()));
        Box vcC = Path.getPath(track.getSampleDescriptionBox(), "....[0]/avcC[0]");
        int lengthSize = 0;
        if (vcC == null) {
            vcC = Path.getPath(track.getSampleDescriptionBox(), "....[0]/hvcC[0]");
            if (vcC != null) {
                lengthSize = ((HevcConfigurationBox) vcC).getLengthSizeMinusOne() + 1;
                left.setCellFactory(new HvcSampleRenderCallback(lengthSize));
            } else {
                left.setCellFactory(new SampleRenderCallback());
            }
        } else {
            lengthSize = ((AvcConfigurationBox) vcC).getLengthSizeMinusOne() + 1;
            left.setCellFactory(new AvcSampleRenderCallback(lengthSize));
        }
        trackTabSplitPane.getItems().add(left);

        SwingNode initialHex = new SwingNode();
        initialHex.setContent(new JHexEditor(ByteBuffer.allocate(0)));
        trackTabSplitPane.getItems().add(initialHex);


        final Box finalVcC = vcC;
        final int finalLengthSize = lengthSize;
        left.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sample>() {
            public void changed(ObservableValue<? extends Sample> observable, Sample oldValue, Sample newValue) {
                if (finalVcC == null) {
                    SwingNode hex = new SwingNode();
                    trackTabSplitPane.getItems().set(1, hex);
                    hex.setContent(new JHexEditor(newValue.asByteBuffer()));
                } else {
                    ScrollPane sp = new ScrollPane();
                    Accordion accordion = new Accordion();
                    sp.setContent(accordion);
                    trackTabSplitPane.getItems().set(1, sp);
                    ByteBuffer s = newValue.asByteBuffer();
                    s.rewind();

                    while (s.remaining() > 0) {
                        ByteBuffer hexSource = s.slice();
                        int length = l2i(IsoTypeReaderVariable.read(s, finalLengthSize));
                        Object nalWrapper;
                        if (finalVcC instanceof AvcConfigurationBox) {
                            nalWrapper = new AvcNalWrapper((ByteBuffer) s.slice().limit(length));
                        } else {// if (finalVcC instanceof HevcConfigurationBox) {
                            nalWrapper = new HvcNalWrapper((ByteBuffer) s.slice().limit(length));
                        }
                        s.position(s.position() + length);

                        hexSource.limit(length + finalLengthSize);
                        SwingNode nalSwingNode = new SwingNode();
                        JHexEditor jHexEditor = new JHexEditor(hexSource);
                        nalSwingNode.setContent(jHexEditor);
                        TitledPane titledPane = new TitledPane(nalWrapper.toString(), nalSwingNode);
                        titledPane.setPrefHeight(jHexEditor.getHeight());
                        accordion.getPanes().add(titledPane);
                    }
                }
            }
        });

        return trackTab;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        storePosAndSize(stage);
    }


    public void loadPosAndSize() {
        // get window location from user preferences: use x=100, y=100, width=400, height=400 as default
        double x = userPrefs.getDouble("stage.x", 100);
        double y = userPrefs.getDouble("stage.y", 100);
        double w = userPrefs.getDouble("stage.width", 400);
        double h = userPrefs.getDouble("stage.height", 400);
        
        javafx.geometry.Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        if(x < 0 || y < 0 || x > primaryScreenBounds.getWidth() || y > primaryScreenBounds.getHeight()) {
            x = 100;
            y = 100;
        }
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(w);
        stage.setHeight(h);

      /*  String openFile = userPrefs.get("openedFile", "doesnotexist");
        if (new File(openFile).exists()) {
            try {
                openFile(new File(openFile));
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public void storePosAndSize(Stage stage) {
        userPrefs.putDouble("stage.x", stage.getX());
        userPrefs.putDouble("stage.y", stage.getY());
        userPrefs.putDouble("stage.width", stage.getWidth());
        userPrefs.putDouble("stage.height", stage.getHeight());
    }

    private static class SampleRenderCallback implements Callback<ListView<Sample>, ListCell<Sample>> {
        public ListCell<Sample> call(ListView<Sample> p) {
            return new ListCell<Sample>() {
                @Override
                public void updateItem(Sample item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText("Sample(" + getIndex() + "): " + item.getSize() + " bytes");
                    }
                }
            };
        }
    }

    private static class AvcSampleRenderCallback implements Callback<ListView<Sample>, ListCell<Sample>> {
        int nalUnitLength;

        public AvcSampleRenderCallback(int nalUnitLength) {
            this.nalUnitLength = nalUnitLength;
        }

        public ListCell<Sample> call(ListView<Sample> p) {
            return new ListCell<Sample>() {
                @Override
                public void updateItem(Sample item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        ByteBuffer s = item.asByteBuffer();
                        s.rewind();
                        String text = "AvcSample(" + getIndex() + "): " + item.getSize() + " bytes [";
                        while (s.remaining() > 0) {
                            int length = l2i(IsoTypeReaderVariable.read(s, nalUnitLength));
                            try {
                                text += new AvcNalWrapper((ByteBuffer) s.slice().limit(length)).toString();
                                s.position(s.position() + length);
                                if (s.remaining() > 0) {
                                    text += ", ";
                                } else {
                                    text += "]";
                                }
                            } catch (BufferUnderflowException e) {
                                System.err.println("Sample " + getIndex() + " is not ok.");
                                e.printStackTrace();;
                                return;
                            }
                        }
                        setText(text);
                    }
                }
            };
        }
    }

    private static class HvcSampleRenderCallback implements Callback<ListView<Sample>, ListCell<Sample>> {
        int nalUnitLength;

        public HvcSampleRenderCallback(int nalUnitLength) {
            this.nalUnitLength = nalUnitLength;
        }

        public ListCell<Sample> call(ListView<Sample> p) {
            return new ListCell<Sample>() {
                @Override
                public void updateItem(Sample item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        ByteBuffer s = item.asByteBuffer();
                        s.rewind();
                        String text = "HvcSample(" + getIndex() + "): " + item.getSize() + " bytes [";
                        while (s.remaining() > 0) {
                            int length = l2i(IsoTypeReaderVariable.read(s, nalUnitLength));

                            text += new HvcNalWrapper((ByteBuffer) s.slice().limit(length)).toString();
                            s.position(s.position() + length);
                            if (s.remaining() > 0) {
                                text += ", ";
                            } else {
                                text += "]";
                            }
                        }
                        setText(text);
                    }
                }
            };
        }
    }

}
