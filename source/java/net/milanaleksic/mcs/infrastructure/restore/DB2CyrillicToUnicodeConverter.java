package net.milanaleksic.mcs.infrastructure.restore;

import java.util.HashMap;

/**
 * Konverzija stringa koji sadrzi Unicode karaktere u DB2 string koji moze da se prepozna 
 * 
 * @author Milan Aleksic
 * 06.04.2008.
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class DB2CyrillicToUnicodeConverter {

    //TODO: deprecated approach, should be updated with dynamic calculation!
	
	private final static HashMap<Character, String> konverzionaTabela;
	
	static {
		konverzionaTabela = new HashMap<>();
		konverzionaTabela.put('\u0430', "0430");
		konverzionaTabela.put('\u0431', "0431");
		konverzionaTabela.put('\u0432', "0432");
		konverzionaTabela.put('\u0433', "0433");
		konverzionaTabela.put('\u0434', "0434");
		konverzionaTabela.put('\u0452', "0452");
		konverzionaTabela.put('\u0435', "0435");
		konverzionaTabela.put('\u0436', "0436");
		konverzionaTabela.put('\u0437', "0437");
		konverzionaTabela.put('\u0438', "0438");
		konverzionaTabela.put('\u0458', "0458");
		konverzionaTabela.put('\u043a', "043A");
		konverzionaTabela.put('\u043b', "043B");
		konverzionaTabela.put('\u0459', "0459");
		konverzionaTabela.put('\u043c', "043C");
		konverzionaTabela.put('\u043d', "043D");
		konverzionaTabela.put('\u045a', "045A");
		konverzionaTabela.put('\u043e', "043E");
		konverzionaTabela.put('\u043f', "043F");
		konverzionaTabela.put('\u0440', "0440");
		konverzionaTabela.put('\u0441', "0441");
		konverzionaTabela.put('\u0442', "0442");
		konverzionaTabela.put('\u045b', "045B");
		konverzionaTabela.put('\u0443', "0443");
		konverzionaTabela.put('\u0444', "0444");
		konverzionaTabela.put('\u0445', "0445");
		konverzionaTabela.put('\u0446', "0446");
		konverzionaTabela.put('\u0447', "0447");
		konverzionaTabela.put('\u045f', "045F");
		konverzionaTabela.put('\u0448', "0448");
		konverzionaTabela.put('\u0410', "0410");
		konverzionaTabela.put('\u0411', "0411");
		konverzionaTabela.put('\u0412', "0412");
		konverzionaTabela.put('\u0413', "0413");
		konverzionaTabela.put('\u0414', "0414");
		konverzionaTabela.put('\u0402', "0402");
		konverzionaTabela.put('\u0415', "0415");
		konverzionaTabela.put('\u0416', "0426");
		konverzionaTabela.put('\u0417', "0417");
		konverzionaTabela.put('\u0418', "0418");
		konverzionaTabela.put('\u0408', "0408");
		konverzionaTabela.put('\u041a', "041A");
		konverzionaTabela.put('\u041b', "041B");
		konverzionaTabela.put('\u0409', "0409");
		konverzionaTabela.put('\u041c', "041C");
		konverzionaTabela.put('\u041d', "041D");
		konverzionaTabela.put('\u040a', "040A");
		konverzionaTabela.put('\u041e', "041E");
		konverzionaTabela.put('\u041f', "041F");
		konverzionaTabela.put('\u0420', "0420");
		konverzionaTabela.put('\u0421', "0421");
		konverzionaTabela.put('\u0422', "0422");
		konverzionaTabela.put('\u040b', "040B");
		konverzionaTabela.put('\u0423', "0423");
		konverzionaTabela.put('\u0424', "0424");
		konverzionaTabela.put('\u0425', "0425");
		konverzionaTabela.put('\u0426', "0426");
		konverzionaTabela.put('\u0427', "0427");
		konverzionaTabela.put('\u040f', "040F");
		konverzionaTabela.put('\u0428', "0428");
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