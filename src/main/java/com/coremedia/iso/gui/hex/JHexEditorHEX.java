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

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Shows the left part of the hex editor. Hex view.
 */
public class JHexEditorHEX extends JComponent implements MouseListener, KeyListener {
    private JHexEditor he;
    private int cursor = 0;

    public JHexEditorHEX(JHexEditor he) {
        this.he = he;
        addMouseListener(this);
        addKeyListener(this);
        addFocusListener(he);
    }

    public Dimension getPreferredSize() {
        debug("getPreferredSize()");
        return getMinimumSize();
    }

    public Dimension getMaximumSize() {
        debug("getMaximumSize()");
        return getMinimumSize();
    }

    public Dimension getMinimumSize() {
        debug("getMinimumSize()");

        Dimension d = new Dimension();
        FontMetrics fn = getFontMetrics(JHexEditor.font);
        int h = fn.getHeight();
        int nl = he.getNumberOfVisibleLines();
        d.setSize(((fn.stringWidth(" ") + 1) * +((16 * 3) - 1)) + (he.border * 2) + 1, h * nl + (he.border * 2) + 1);
        return d;
    }

    public void paint(Graphics g) {
        debug("paint(" + g + ")");
        debug("cursor=" + he.cursor + " buff.length=" + he.buff.limit());
        Dimension d = getMinimumSize();
        g.setColor(Color.white);
        g.fillRect(0, 0, d.width, d.height);
        g.setColor(Color.black);

        g.setFont(JHexEditor.font);

        int ini = he.getInicio() * 16;
        long fin = ini + (he.getNumberOfVisibleLines() * 16);
        if (fin > he.buff.limit()) fin = he.buff.limit();

        //datos hex
        int x = 0;
        int y = 0;
        he.buff.position(ini);
        for (int n = ini; n < fin; n++) {
            if (n == he.cursor) {
                if (hasFocus()) {
                    g.setColor(Color.black);
                    he.filledCursor(g, (x * 3), y, 2);
                    g.setColor(Color.blue);
                    he.filledCursor(g, (x * 3) + cursor, y, 1);
                } else {
                    g.setColor(Color.blue);
                    he.cuadro(g, (x * 3), y, 2);
                }

                if (hasFocus()) g.setColor(Color.white);
                else g.setColor(Color.black);
            } else {
                g.setColor(Color.black);
            }

            String s = ("0" + Integer.toHexString(he.buff.get()));
            s = s.substring(s.length() - 2);
            he.printString(g, s, ((x++) * 3), y);
            if (x == 16) {
                x = 0;
                y++;
            }
        }

    }

    private void debug(String s) {
        if (he.DEBUG) System.out.println("JHexEditorHEX ==> " + s);
    }

    // calcular la posicion del raton
    public int calcularPosicionRaton(int x, int y) {
        FontMetrics fn = getFontMetrics(JHexEditor.font);
        x = x / ((fn.stringWidth(" ") + 1) * 3);
        y = y / fn.getHeight();
        debug("x=" + x + " ,y=" + y);
        return x + ((y + he.getInicio()) * 16);
    }

    // mouselistener
    public void mouseClicked(MouseEvent e) {
        debug("mouseClicked(" + e + ")");
        he.cursor = calcularPosicionRaton(e.getX(), e.getY());
        this.requestFocus();
        he.repaint();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    //KeyListener
    public void keyTyped(KeyEvent e) {


    }

    public void keyPressed(KeyEvent e) {
        debug("keyPressed(" + e + ")");
        he.keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
        debug("keyReleased(" + e + ")");
    }

    public boolean isFocusable() {
        return true;
    }
}
