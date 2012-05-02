package com.coremedia.iso.gui;

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * Simple convenience class for non-editable textfield
 */
public class NonEditableJTextField extends JTextField {
  public NonEditableJTextField() {
  }

  public NonEditableJTextField(String text) {
    this(null, text, 0);
  }

  public NonEditableJTextField(int columns) {
    this(null, null, columns);
  }

  public NonEditableJTextField(String text, int columns) {
    this(null, text, columns);
  }

  public NonEditableJTextField(Document doc, String text, int columns) {
    super(doc, text, columns);
    this.setEditable(false);
  }
}
