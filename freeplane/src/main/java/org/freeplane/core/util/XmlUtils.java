package org.freeplane.core.util;

public class XmlUtils {
	/** \0 is not allowed: */
	public static String replaceAscii0BySpace(final String pXmlNoteText) {
		return pXmlNoteText.replace('\0', ' ');
	}

    public static boolean containsElementAt(final String text, String elementName, int start) {
        return text != null
                && (text.length() >= 2 + start + elementName.length())
                && (text.charAt(start) == '<')
                && (text.charAt(start + elementName.length() + 1) == '>')
                &&  text.substring(start + 1, start + elementName.length() + 1).equalsIgnoreCase(elementName);
    }

    public static boolean startsWithElement(final String text, String elementName) {
        for (int i = 0; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (ch == '<') {
                return containsElementAt(text, elementName, i);
            }
            if (!Character.isWhitespace(ch)) {
                return false;
            }
        }
        return false;
    }
}
