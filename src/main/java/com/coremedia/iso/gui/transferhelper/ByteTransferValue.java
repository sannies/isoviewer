package com.coremedia.iso.gui.transferhelper;

import com.coremedia.iso.boxes.Box;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 11.11.2008
 * Time: 23:13:14
 * To change this template use File | Settings | File Templates.
 */
public class ByteTransferValue implements TransferValue {
    JFormattedTextField source;
    Method writeMethod;
    Box box;

    public ByteTransferValue(JFormattedTextField source, Box box, Method writeMethod) {
        this.source = source;
        this.writeMethod = writeMethod;
        this.box = box;
    }

    public void go() {
        try {
            writeMethod.invoke(box, ((Number) source.getFormatter().stringToValue(source.getText())).byteValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}
