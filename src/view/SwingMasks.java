package view;

import javax.swing.*;
import javax.swing.text.*;
import java.util.regex.Pattern;

/** Filtros simples para permitir apenas dígitos nos campos. */
public class SwingMasks {
    private static class DigitsFilter extends DocumentFilter {
        private final Pattern p = Pattern.compile("\\d*");
        @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null && p.matcher(string).matches()) super.insertString(fb, offset, string, attr);
        }
        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text != null && p.matcher(text).matches()) super.replace(fb, offset, length, text, attrs);
        }
    }
    public static void digitsOnly(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DigitsFilter());
    }
}
