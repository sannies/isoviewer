package com.coremedia.iso.gui.transferhelper;

import com.coremedia.iso.boxes.Box;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 11.11.2008
 * Time: 23:12:10
 * To change this template use File | Settings | File Templates.
 */
public class DateTransferValue implements TransferValue {
    JTextField source;
    Method writeMethod;
    Box box;

    public DateTransferValue(JTextField source, Box box, Method writeMethod) {
        this.source = source;
        this.writeMethod = writeMethod;
        this.box = box;
    }

    public void go() {
        try {
            writeMethod.invoke(box, SimpleDateFormat.getDateTimeInstance().parse(source.getText()));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }
}
