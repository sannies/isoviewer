/*
 * Copyright 2008 Germán Laullón
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

package com.coremedia.iso.gui.hex;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.ByteBuffer;

/**
 * Combines a hex view and an ASCII view to a fully fledged hex editor.
 */
public class JHexEditor extends JPanel implements FocusListener, AdjustmentListener, MouseWheelListener {
    ByteBuffer buff;
    public int cursor;
    protected static Font font = new Font("Monospaced", 0, 12);
    protected int border = 2;
    public boolean DEBUG = false;
    private JScrollBar scrollBar;
    private int inicio = 0;
    private int numberOfVisibleLines = 10;

    public JHexEditor(ByteBuffer buff) {
        super();
        this.buff = buff;

        this.addMouseWheelListener(this);

        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        scrollBar.addAdjustmentListener(this);
        scrollBar.setMinimum(0);
        scrollBar.setMaximum((int) (buff.limit() / getNumberOfVisibleLines()));

        JPanel p1, p2, p3;
        //centro
        p1 = new JPanel(new BorderLayout(1, 1));
        p1.add(new JHexEditorHEX(this), BorderLayout.CENTER);
        p1.add(new Columnas(), BorderLayout.NORTH);

        // izq.
        p2 = new JPanel(new BorderLayout(1, 1));
        p2.add(new Filas(), BorderLayout.CENTER);
        p2.add(new Caja(), BorderLayout.NORTH);

        // der
        p3 = new JPanel(new BorderLayout(1, 1));
        p3.add(scrollBar, BorderLayout.EAST);
        p3.add(new JHexEditorASCII(this), BorderLayout.CENTER);
        p3.add(new Caja(), BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(1, 1));
        panel.add(p1, BorderLayout.CENTER);
        panel.add(p2, BorderLayout.WEST);
        panel.add(p3, BorderLayout.EAST);

        this.setLayout(new BorderLayout(1, 1));
        this.add(panel, BorderLayout.CENTER);
    }

    public void paint(Graphics g) {
        FontMetrics fn = getFontMetrics(font);
        Rectangle rec = this.getBounds();
        numberOfVisibleLines = (rec.height / fn.getHeight()) - 2;
        int n = (int) (buff.limit() / 16);
        if ((buff.limit() % 16) > 0) {
            n++;
        }
        if (numberOfVisibleLines > n) {
            numberOfVisibleLines = n;
            inicio = 0;
        }

        scrollBar.setValues(getInicio(), getNumberOfVisibleLines(), 0, n);
        scrollBar.setValueIsAdjusting(true);
        super.paint(g);
    }

    protected void actualizaCursor() {
        int n = (cursor / 16);
        if ((buff.limit() % 16) > 0) {
            n++;
        }
        if (n < inicio) {
            inicio = n;
        } else if (n >= inicio + numberOfVisibleLines) {
            inicio = n - numberOfVisibleLines + 2;
        }
        repaint();
    }

    protected int getInicio() {
        return inicio;
    }

    protected int getNumberOfVisibleLines() {
        if (DEBUG) {
            System.err.println("numberOfVisibleLines: " + numberOfVisibleLines);
        }
        return numberOfVisibleLines;
    }

    protected void filledCursor(Graphics g, int x, int y, int s) {
        FontMetrics fn = getFontMetrics(font);
        g.fillRect(((fn.stringWidth(" ") + 1) * x) + border, (fn.getHeight() * y) + border, ((fn.stringWidth(" ") + 1) * s), fn.getHeight() + 1);
    }

    protected void cuadro(Graphics g, int x, int y, int s) {
        FontMetrics fn = getFontMetrics(font);
        g.drawRect(((fn.stringWidth(" ") + 1) * x) + border, (fn.getHeight() * y) + border, ((fn.stringWidth(" ") + 1) * s), fn.getHeight() + 1);
    }

    protected void printString(Graphics g, String s, int x, int y) {
        FontMetrics fn = getFontMetrics(font);
        g.drawString(s, ((fn.stringWidth(" ") + 1) * x) + border, ((fn.getHeight() * (y + 1)) - fn.getMaxDescent()) + border);
    }

    public void focusGained(FocusEvent e) {
        this.repaint();
    }

    public void focusLost(FocusEvent e) {
        this.repaint();
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        inicio = e.getValue();
        if (inicio < 0) inicio = 0;
        repaint();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        inicio += (e.getUnitsToScroll());
        int n = (int) (buff.limit() / 16);
        n += 4;
        if ((inicio + numberOfVisibleLines) >= n) {
            inicio = n - numberOfVisibleLines;
        }
        if (inicio < 0) {
            inicio = 0;
        }
        repaint();
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 33:    // rep
                if (cursor >= (16 * numberOfVisibleLines)) {
                    cursor -= (16 * numberOfVisibleLines);
                }
                actualizaCursor();
                break;
            case 34:    // fin
                if (cursor < (buff.limit() - (16 * numberOfVisibleLines))) {
                    cursor += (16 * numberOfVisibleLines);
                }
                actualizaCursor();
                break;
            case 35:    // fin
                cursor = (int) (buff.limit() - 1);
                actualizaCursor();
                break;
            case 36:    // ini
                cursor = 0;
                actualizaCursor();
                break;
            case 37:    // <--
                if (cursor != 0) cursor--;
                actualizaCursor();
                break;
            case 38:    // <--
                if (cursor > 15) cursor -= 16;
                actualizaCursor();
                break;
            case 39:    // -->
                if (cursor != (buff.limit() - 1)) cursor++;
                actualizaCursor();
                break;
            case 40:    // -->
                if (cursor < (buff.limit() - 16)) cursor += 16;
                actualizaCursor();
                break;

        }
    }

    private class Columnas extends JPanel {
        public Columnas() {
            this.setLayout(new BorderLayout(1, 1));
        }

        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        public Dimension getMinimumSize() {
            Dimension d = new Dimension();
            FontMetrics fn = getFontMetrics(font);
            int h = fn.getHeight();
            int nl = 1;
            d.setSize(((fn.stringWidth(" ") + 1) * +((16 * 3) - 1)) + (border * 2) + 1, h * nl + (border * 2) + 1);
            return d;
        }

        public void paint(Graphics g) {
            Dimension d = getMinimumSize();
            g.setColor(Color.white);
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(Color.black);
            g.setFont(font);

            for (int n = 0; n < 16; n++) {
                if (n == (cursor % 16)) cuadro(g, n * 3, 0, 2);
                String s = "00" + Integer.toHexString(n);
                s = s.substring(s.length() - 2);
                printString(g, s, n * 3, 0);
            }
        }
    }

    private class Caja extends JPanel {
        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        public Dimension getMinimumSize() {
            Dimension d = new Dimension();
            FontMetrics fn = getFontMetrics(font);
            int h = fn.getHeight();
            d.setSize((fn.stringWidth(" ") + 1) + (border * 2) + 1, h + (border * 2) + 1);
            return d;
        }

    }

    private class Filas extends JPanel {
        public Filas() {
            this.setLayout(new BorderLayout(1, 1));
        }

        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        public Dimension getMinimumSize() {
            Dimension d = new Dimension();
            FontMetrics fn = getFontMetrics(font);
            int h = fn.getHeight();
            int nl = getNumberOfVisibleLines();
            d.setSize((fn.stringWidth(" ") + 1) * (8) + (border * 2) + 1, h * nl + (border * 2) + 1);
            return d;
        }

        public void paint(Graphics g) {
            Dimension d = getMinimumSize();
            g.setColor(Color.white);
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(Color.black);
            g.setFont(font);

            int ini = getInicio();
            int fin = ini + getNumberOfVisibleLines();
            int y = 0;
            for (int n = ini; n < fin; n++) {
                if (n == (cursor / 16)) cuadro(g, 0, y, 8);
                String s = "0000000000000" + Integer.toHexString(n);
                s = s.substring(s.length() - 8);
                printString(g, s, 0, y++);
            }
        }
    }

}
