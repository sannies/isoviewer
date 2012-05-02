package com.coremedia.iso.gui.transferhelper;

import com.coremedia.iso.boxes.Box;

import javax.swing.*;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 11.11.2008
 * Time: 23:13:45
 * To change this template use File | Settings | File Templates.
 */
public final class TransferHelperFactory {
    public static TransferValue getNumberTransferHelper(Class aClass,
                                                        Box  box, Method writeMethod, JFormattedTextField jftf) {
        if (Byte.class == aClass || byte.class == aClass) {
            return new ByteTransferValue(jftf, box, writeMethod);
        } else if (Double.class == aClass || double.class == aClass) {
            return new DoubleTransferValue(jftf, box, writeMethod);
        } else if (Float.class == aClass || float.class == aClass) {
            return new FloatTransferValue(jftf, box, writeMethod);
        } else if (Integer.class == aClass || int.class == aClass) {
            return new IntegerTransferValue(jftf, box, writeMethod);
        } else if (Short.class == aClass || short.class == aClass) {
            return new ShortTransferValue(jftf, box, writeMethod);
        } else if (Long.class == aClass || long.class == aClass) {
            return new LongTransferValue(jftf, box, writeMethod);
        } else {
            throw new RuntimeException("unknown number class " + aClass);
        }

    }
}
