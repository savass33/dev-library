package view;

import javax.swing.*;
import javax.swing.text.*;
import java.util.regex.Pattern;

/** Filtros simples para campos Swing. */
public class SwingMasks {

    /** Só dígitos, sem limite. */
    public static void digitsOnly(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DigitsFilter(Integer.MAX_VALUE));
    }

    /** Só dígitos, com limite máximo. */
    public static void digitsOnlyMax(JTextField tf, int maxLen) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DigitsFilter(maxLen));
    }

    /** Somente letras (com acentos), espaços, ponto, apóstrofo e hífen. */
    public static void lettersAndSpacesOnly(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new LettersFilter());
    }

    /* ====== Filters ====== */

    private static class DigitsFilter extends DocumentFilter {
        private final Pattern digits = Pattern.compile("\\d*");
        private final int max;
        private DigitsFilter(int max) { this.max = max; }

        @Override
        public void insertString(FilterBypass fb, int off, String str, AttributeSet a) throws BadLocationException {
            if (str != null && digits.matcher(str).matches()
                    && fb.getDocument().getLength() + str.length() <= max) {
                super.insertString(fb, off, str, a);
            }
        }

        @Override
        public void replace(FilterBypass fb, int off, int len, String txt, AttributeSet a) throws BadLocationException {
            if (txt != null && digits.matcher(txt).matches()
                    && fb.getDocument().getLength() - len + txt.length() <= max) {
                super.replace(fb, off, len, txt, a);
            }
        }
    }

    private static class LettersFilter extends DocumentFilter {
        // \p{L} = qualquer letra unicode; permite espaço, ponto, hífen e apóstrofo
        private final Pattern allowed = Pattern.compile("[\\p{L} .'-]*");

        @Override
        public void insertString(FilterBypass fb, int off, String str, AttributeSet a) throws BadLocationException {
            if (str != null && allowed.matcher(str).matches()) super.insertString(fb, off, str, a);
        }

        @Override
        public void replace(FilterBypass fb, int off, int len, String txt, AttributeSet a) throws BadLocationException {
            if (txt != null && allowed.matcher(txt).matches()) super.replace(fb, off, len, txt, a);
        }
    }
}
