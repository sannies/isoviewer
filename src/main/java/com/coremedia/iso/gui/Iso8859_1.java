package com.coremedia.iso.gui;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public final class Iso8859_1 {


  public static String convert(byte[] b) {
    try {
      return new String(b, "ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    }
  }

}
