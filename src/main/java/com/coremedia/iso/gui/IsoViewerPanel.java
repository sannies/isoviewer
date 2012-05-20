/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.coremedia.iso.gui.hex.JHexEditor;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import com.googlecode.mp4parser.util.Path;
import org.jdesktop.application.Action;
import org.jdesktop.application.Resource;
import org.jdesktop.application.session.PropertySupport;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * The main UI class for the ISO viewer. Contains all other UI components.
 */
public class IsoViewerPanel extends JPanel implements PropertySupport {


    private JTree tree;
    private JList trackList;
    private JPanel detailPanel;
    private JSplitPane rawDataSplitPane;

    @Resource
    private String trackViewDetailPaneHeader = "T %s";
    @Resource
    private String tabbedPaneHeaderTrack = "T&S";
    @Resource
    private String tabbedPaneHeaderBox = "BS";

    private Object details;

    private Frame mainFrame;
    private File file;


    public IsoViewerPanel(Frame mainFrame) {
        this.mainFrame = mainFrame;
        this.setName("IsoViewerPanel");

    }


    public void createLayout() {
        IsoFile dummy = new IsoFile();
        tree = new BoxJTree();
        tree.setModel(new IsoFileTreeModel(dummy));

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                Box node = (Box) e.getPath().getLastPathComponent();
                showDetails(node);
            }
        });


        detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        rawDataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rawDataSplitPane.setName("rawDataSplitPane");
        rawDataSplitPane.setBorder(null);
        rawDataSplitPane.setOneTouchExpandable(true);
        JScrollPane scrollPane = new JScrollPane(detailPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rawDataSplitPane.setTopComponent(scrollPane);
        rawDataSplitPane.setBottomComponent(new JHexEditor(ByteBuffer.allocate(0)));
        rawDataSplitPane.setResizeWeight(0.8);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.setName("tracksOrBoxes");
        JScrollPane jScrollPane = new JScrollPane(tree);
        jScrollPane.setName("boxTreeScrollPane");
        jTabbedPane.add(tabbedPaneHeaderBox, jScrollPane);
        jTabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int index = ((JTabbedPane) e.getSource()).getSelectedIndex();
                if (index == 0) {
                    if (tree.getSelectionPath() != null) {
                        Object selected = tree.getSelectionPath().getLastPathComponent();
                        if (selected != null) {
                            showDetails(selected);
                        }
                    }
                } else if (index == 1) {
                    if (trackList.getSelectedValue() != null) {
                        showSamples((TrackBox) trackList.getSelectedValue());
                    }
                }
            }
        });

        trackList = new JList();
        trackList.setCellRenderer(new TrackListRenderer());
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trackList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showSamples((TrackBox) ((JList) e.getSource()).getSelectedValue());
            }
        });

        jTabbedPane.add(tabbedPaneHeaderTrack, trackList);
        splitPane.setLeftComponent(jTabbedPane);
        splitPane.setRightComponent(rawDataSplitPane);
        splitPane.setResizeWeight(0.6);
        splitPane.setName("treeLeftdataRight");

        this.setLayout(new BorderLayout());
        this.add(splitPane, BorderLayout.CENTER);


    }

    private void showSamples(TrackBox tb) {
        detailPanel.removeAll();
        JComponent detailPane = new JPanel(new BorderLayout());

        JList jlist = new JList();
        jlist.setCellRenderer(new SampleListRenderer());

        SampleDescriptionBox sampleDescriptionBox = tb.getMediaBox().getMediaInformationBox().getSampleTableBox().getSampleDescriptionBox();
        jlist.setModel(new SampleListModel(new SampleList(tb), tb.getTrackHeaderBox().getTrackId(), sampleDescriptionBox.getSampleEntry()));
        jlist.setLayoutOrientation(JList.VERTICAL);
        jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jlist.setPrototypeCellValue(new SampleListModel.Entry(ByteBuffer.allocate(1000), 1000000000, 0, null));
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.getViewport().add(jlist);
        detailPane.add(new JLabel(String.format(trackViewDetailPaneHeader, tb.getTrackHeaderBox().getTrackId())), BorderLayout.PAGE_START);
        detailPane.add(jScrollPane, BorderLayout.CENTER);
        jlist.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    rawDataSplitPane.setBottomComponent(new JHexEditor(((SampleListModel.Entry) ((JList) e.getSource()).getSelectedValue()).sample));

                }
            }
        });
        detailPanel.add(detailPane);
        detailPanel.revalidate();
    }

    JFileChooser fileChooser = new JFileChooser();


    public void open(File f) throws IOException {
        this.file = f;
        IsoFile isoFile = new IsoFile(new RandomAccessFile(f, "r").getChannel());
        long start = System.nanoTime();
        final List<LogRecord> messages = new LinkedList<LogRecord>();
        Handler myTemperaryLogHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        Logger.getLogger("").addHandler(myTemperaryLogHandler);


        Logger.getAnonymousLogger().removeHandler(myTemperaryLogHandler);
        System.err.println("Parsing took " + ((System.nanoTime() - start) / 1000000d) + "ms.");

        tree.setModel(new IsoFileTreeModel(isoFile));
        tree.revalidate();


        trackList.setModel(new TrackListModel(isoFile));
        if (!messages.isEmpty()) {
            String message = "";
            for (LogRecord logRecord : messages) {
                message += logRecord.getMessage() + "\n";
            }
            JOptionPane.showMessageDialog(this,
                    message,
                    "Parser Messages",
                    JOptionPane.WARNING_MESSAGE);

        }
        if (details instanceof Box) {
            String path = Path.createPath((Box) details);
            Box nuDetail = Path.getPath(isoFile, path);
            if (nuDetail != null) {
                showDetails(nuDetail);
            } else {
                showDetails(isoFile);
            }
        } else {
            showDetails(isoFile);
        }
        mainFrame.setTitle("Iso Viewer - " + f.getAbsolutePath());

    }


    @Action(name = "open-iso-file")
    public void open() {

        int state = fileChooser.showOpenDialog(IsoViewerPanel.this);
        if (state == JFileChooser.APPROVE_OPTION) {
            try {
                open(fileChooser.getSelectedFile());
            } catch (IOException e) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                e.printStackTrace(ps);
                JOptionPane.showMessageDialog(this,
                        new String(baos.toByteArray()),
                        "e.getMessage()",
                        JOptionPane.ERROR_MESSAGE);
                ;
            }

        }

    }

    public void showDetails(Object object) {
        details = object;
        Cursor oldCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JComponent detailPane = new JPanel();
            if (object instanceof Box) {
                detailPane = new GenericBoxPane((Box) object);
            }
            detailPanel.removeAll();
            detailPanel.add(detailPane, BorderLayout.CENTER);
            detailPanel.revalidate();
            ByteBuffer displayMe;
            if (object instanceof Box && !(object instanceof IsoFile)) {
                displayMe = ByteBuffer.allocate(l2i(((Box) object).getSize()));
                try {
                    ((Box) object).getBox(new ByteBufferByteChannel(displayMe));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (object instanceof IsoFile) {
                FileChannel fc = new FileInputStream(this.file).getChannel();
                displayMe = fc.map(FileChannel.MapMode.READ_ONLY, 0, Math.min(this.file.length(), Integer.MAX_VALUE));
                fc.close();
            } else {
                displayMe = ByteBuffer.allocate(0);
            }
            rawDataSplitPane.setBottomComponent(new JHexEditor(displayMe));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            setCursor(oldCursor);

        }
    }

    public Object getSessionState(Component c) {
        return this.file.getAbsolutePath();
    }

    public void setSessionState(Component c, Object state) {
        try {
            open(new File(state.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
