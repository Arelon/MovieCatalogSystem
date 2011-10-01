package net.milanaleksic.mcs.infrastructure.restore;

import java.util.HashMap;

/**
 * Konverzija stringa koji sadrzi Unicode karaktere u DB2 string koji moze da se prepozna 
 * 
 * @author Milan Aleksic
 * 06.04.2008.
 */
public class DB2CyrillicToUnicodeConvertor {
	
	private final static HashMap<Character, String> konverzionaTabela;
	
	static {
		konverzionaTabela = new HashMap<Character, String>();
		konverzionaTabela.put('а', "0430");
		konverzionaTabela.put('б', "0431");
		konverzionaTabela.put('в', "0432");
		konverzionaTabela.put('г', "0433");
		konverzionaTabela.put('д', "0434");
		konverzionaTabela.put('ђ', "0452");
		konverzionaTabela.put('е', "0435");
		konverzionaTabela.put('ж', "0436");
		konverzionaTabela.put('з', "0437");
		konverzionaTabela.put('и', "0438");
		konverzionaTabela.put('ј', "0458");
		konverzionaTabela.put('к', "043A");
		konverzionaTabela.put('л', "043B");
		konverzionaTabela.put('љ', "0459");
		konverzionaTabela.put('м', "043C");
		konverzionaTabela.put('н', "043D");
		konverzionaTabela.put('њ', "045A");
		konverzionaTabela.put('о', "043E");
		konverzionaTabela.put('п', "043F");
		konverzionaTabela.put('р', "0440");
		konverzionaTabela.put('с', "0441");
		konverzionaTabela.put('т', "0442");
		konverzionaTabela.put('ћ', "045B");
		konverzionaTabela.put('у', "0443");
		konverzionaTabela.put('ф', "0444");
		konverzionaTabela.put('х', "0445");
		konverzionaTabela.put('ц', "0446");
		konverzionaTabela.put('ч', "0447");
		konverzionaTabela.put('џ', "045F");
		konverzionaTabela.put('ш', "0448");
		konverzionaTabela.put('А', "0410");
		konverzionaTabela.put('Б', "0411");
		konverzionaTabela.put('В', "0412");
		konverzionaTabela.put('Г', "0413");
		konverzionaTabela.put('Д', "0414");
		konverzionaTabela.put('Ђ', "0402");
		konverzionaTabela.put('Е', "0415");
		konverzionaTabela.put('Ж', "0426");
		konverzionaTabela.put('З', "0417");
		konverzionaTabela.put('И', "0418");
		konverzionaTabela.put('Ј', "0408");
		konverzionaTabela.put('К', "041A");
		konverzionaTabela.put('Л', "041B");
		konverzionaTabela.put('Љ', "0409");
		konverzionaTabela.put('М', "041C");
		konverzionaTabela.put('Н', "041D");
		konverzionaTabela.put('Њ', "040A");
		konverzionaTabela.put('О', "041E");
		konverzionaTabela.put('П', "041F");
		konverzionaTabela.put('Р', "0420");
		konverzionaTabela.put('С', "0421");
		konverzionaTabela.put('Т', "0422");
		konverzionaTabela.put('Ћ', "040B");
		konverzionaTabela.put('У', "0423");
		konverzionaTabela.put('Ф', "0424");
		konverzionaTabela.put('Х', "0425");
		konverzionaTabela.put('Ц', "0426");
		konverzionaTabela.put('Ч', "0427");
		konverzionaTabela.put('Џ', "040F");
		konverzionaTabela.put('Ш', "0428");
	}
	
	public static String obradiTekst(String text) {
        StringBuilder complText = new StringBuilder();
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
				String encoded = unikoduj(source);
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

	private static String unikoduj(String substr) {
        StringBuilder buffer = new StringBuilder();
		String sufix = "'";
        String prefix = "UX'";
		for (int i=0; i<substr.length(); i++) {
			Character key = substr.charAt(i);
			if (key == '\'')
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