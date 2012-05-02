package com.coremedia.iso.gui.transferhelper;

import com.coremedia.iso.boxes.Box;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 11.11.2008
 * Time: 23:12:10
 * To change this template use File | Settings | File Templates.
 */
public class StringTransferValue implements TransferValue {
    JTextField source;
    Method writeMethod;
    Box box;

    public StringTransferValue(JTextField source, Box box, Method writeMethod) {
        this.source = source;
        this.writeMethod = writeMethod;
        this.box = box;
    }

    public void go() {
        try {
            writeMethod.invoke(box, source.getText());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}
