package net.milanaleksic.mcs.restore;

import java.util.HashMap;

/**
 * Konverzija stringa koji sadrzi Unicode karaktere u DB2 string koji moze da se prepozna 
 * 
 * @author Milan Aleksic
 * 06.04.2008.
 */
public class DB2CyrillicToUnicodeConvertor {
	
	private static HashMap<Character, String> konverzionaTabela = null;
	
	static {
		konverzionaTabela = new HashMap<Character, String>();
		konverzionaTabela.put(new Character('а'), new String("0430"));
		konverzionaTabela.put(new Character('б'), new String("0431"));
		konverzionaTabela.put(new Character('в'), new String("0432"));
		konverzionaTabela.put(new Character('г'), new String("0433"));
		konverzionaTabela.put(new Character('д'), new String("0434"));
		konverzionaTabela.put(new Character('ђ'), new String("0452"));
		konverzionaTabela.put(new Character('е'), new String("0435"));
		konverzionaTabela.put(new Character('ж'), new String("0436"));
		konverzionaTabela.put(new Character('з'), new String("0437"));
		konverzionaTabela.put(new Character('и'), new String("0438"));
		konverzionaTabela.put(new Character('ј'), new String("0458"));
		konverzionaTabela.put(new Character('к'), new String("043A"));
		konverzionaTabela.put(new Character('л'), new String("043B"));
		konverzionaTabela.put(new Character('љ'), new String("0459"));
		konverzionaTabela.put(new Character('м'), new String("043C"));
		konverzionaTabela.put(new Character('н'), new String("043D"));
		konverzionaTabela.put(new Character('њ'), new String("045A"));
		konverzionaTabela.put(new Character('о'), new String("043E"));
		konverzionaTabela.put(new Character('п'), new String("043F"));
		konverzionaTabela.put(new Character('р'), new String("0440"));
		konverzionaTabela.put(new Character('с'), new String("0441"));
		konverzionaTabela.put(new Character('т'), new String("0442"));
		konverzionaTabela.put(new Character('ћ'), new String("045B"));
		konverzionaTabela.put(new Character('у'), new String("0443"));
		konverzionaTabela.put(new Character('ф'), new String("0444"));
		konverzionaTabela.put(new Character('х'), new String("0445"));
		konverzionaTabela.put(new Character('ц'), new String("0446"));
		konverzionaTabela.put(new Character('ч'), new String("0447"));
		konverzionaTabela.put(new Character('џ'), new String("045F"));
		konverzionaTabela.put(new Character('ш'), new String("0448"));
		konverzionaTabela.put(new Character('А'), new String("0410"));
		konverzionaTabela.put(new Character('Б'), new String("0411"));
		konverzionaTabela.put(new Character('В'), new String("0412"));
		konverzionaTabela.put(new Character('Г'), new String("0413"));
		konverzionaTabela.put(new Character('Д'), new String("0414"));
		konverzionaTabela.put(new Character('Ђ'), new String("0402"));
		konverzionaTabela.put(new Character('Е'), new String("0415"));
		konverzionaTabela.put(new Character('Ж'), new String("0426"));
		konverzionaTabela.put(new Character('З'), new String("0417"));
		konverzionaTabela.put(new Character('И'), new String("0418"));
		konverzionaTabela.put(new Character('Ј'), new String("0408"));
		konverzionaTabela.put(new Character('К'), new String("041A"));
		konverzionaTabela.put(new Character('Л'), new String("041B"));
		konverzionaTabela.put(new Character('Љ'), new String("0409"));
		konverzionaTabela.put(new Character('М'), new String("041C"));
		konverzionaTabela.put(new Character('Н'), new String("041D"));
		konverzionaTabela.put(new Character('Њ'), new String("040A"));
		konverzionaTabela.put(new Character('О'), new String("041E"));
		konverzionaTabela.put(new Character('П'), new String("041F"));
		konverzionaTabela.put(new Character('Р'), new String("0420"));
		konverzionaTabela.put(new Character('С'), new String("0421"));
		konverzionaTabela.put(new Character('Т'), new String("0422"));
		konverzionaTabela.put(new Character('Ћ'), new String("040B"));
		konverzionaTabela.put(new Character('У'), new String("0423"));
		konverzionaTabela.put(new Character('Ф'), new String("0424"));
		konverzionaTabela.put(new Character('Х'), new String("0425"));
		konverzionaTabela.put(new Character('Ц'), new String("0426"));
		konverzionaTabela.put(new Character('Ч'), new String("0427"));
		konverzionaTabela.put(new Character('Џ'), new String("040F"));
		konverzionaTabela.put(new Character('Ш'), new String("0428"));
	}
	
	public static String obradiTekst(String text, boolean useCStyle) {
		StringBuffer complText = new StringBuffer();
		final byte MOD_KOPIRANJE = 0;
		final byte MOD_KOPIRANJE_NEUNICODE_STRINGA = 1;
		final byte MOD_KONVERZIJE = 2;
		byte modRada = MOD_KOPIRANJE;
		int i = 0;
		
		if (text.length() == 0)
			return "";
		
		while (i<text.length()) {
			char curr = text.charAt(i);
			if (modRada==MOD_KOPIRANJE) {
				if (curr=='\'') {
					if (containsUnicodeCharactersBeforeEnding(text, i+1))
						modRada = MOD_KONVERZIJE;
					else {
						complText.append(curr);
						modRada = MOD_KOPIRANJE_NEUNICODE_STRINGA;
					}
				}
				else
					complText.append(curr);
				i++;
			}
			else if (modRada==MOD_KOPIRANJE_NEUNICODE_STRINGA) {
				complText.append(curr);
				if (curr=='\'') {
					if (i != text.length()-1) 
						complText.append(curr); // 'His name was O'Brien' ---> 'His name was O''Brien'
					modRada = MOD_KOPIRANJE;
				}
				i++;
			}
			else {
				if (text.indexOf('\'', i) == -1)
					break;
				
				String source = text.substring(i, text.indexOf('\'', i));
				String encoded = unikoduj(source, useCStyle);
				//System.out.println(source+" -> "+encoded);
				complText.append(encoded);
				i += text.indexOf('\'', i) - i + 1;
				modRada = MOD_KOPIRANJE;
			}
		}
		return complText.toString();
	}
	
	private static boolean containsUnicodeCharactersBeforeEnding(String text, int posAfterApostr) {
		//System.out.println(text+":"+posAfterApostr);
		for (int i=posAfterApostr; i<text.length(); i++) {
			if (text.charAt(i)=='\'')
				return false;
			else if (text.charAt(i) > 255) 
				return true;
		}
		return false;
	}

	private static String unikoduj(String substr, boolean useCStyle) {
		StringBuffer buffer = new StringBuffer();
		String prefix = null;
		String sufix = "'";
		if (useCStyle)
			prefix = "'\\u";
		else
			prefix = "UX'";
		for (int i=0; i<substr.length(); i++) {
			Character key = new Character(substr.charAt(i));
			if (key.charValue() == '\'')
				continue;
			if (konverzionaTabela.containsKey(key))
				buffer.append(prefix).append(konverzionaTabela.get(key)).append(sufix);
			else
				buffer.append("'").append(key.charValue()).append("'");
			buffer.append(" || ");
		}
		if (buffer.length()>4 && (buffer.substring(buffer.length()-4, buffer.length()).equals(" || "))) {
			buffer.delete(buffer.length()-4, buffer.length());
		}
		return buffer.toString();
	}
}