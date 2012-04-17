package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.*;
import net.milanaleksic.mcs.infrastructure.restore.DB2CyrillicToUnicodeConvertor;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.*;

/**
 * User: Milan Aleksic
 * Date: 25/08/11
 * Time: 11:31
 */
public final class DBUtil {

    private final static Logger log = Logger.getLogger(DBUtil.class);
    public static final String STRING_WHEN_NULL = "NULL"; //NON-NLS

    public static void executeScriptOnConnection(InputStream stream, Connection conn) throws IOException, SQLException {
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
        StringBuilder script = new StringBuilder();
        String line;
        while ((line = scriptStreamReader.readLine()) != null) {
            script.append(line).append("\r\n");
        }

        try (PreparedStatement st = conn.prepareStatement(script.toString())) {
            st.execute();
            conn.commit();
        }
    }

    public static void executeScriptOnConnection(String restartCountersScript, Connection conn) throws IOException, SQLException {
        File fileRestartCountersScript = new File(restartCountersScript);
        if (log.isInfoEnabled())
            log.info("Executing script file: " + fileRestartCountersScript.getName()); //NON-NLS
        if (fileRestartCountersScript.exists()) {
            try (FileInputStream fis = new FileInputStream(fileRestartCountersScript)) {
                DBUtil.executeScriptOnConnection(fis, conn);
            }
        }
    }

    public static String getSQLString(Optional<String> input) {
        if (!input.isPresent())
            return STRING_WHEN_NULL;
        return '\'' + input.get().replaceAll("'", "''") + '\'';
    }

    public static String getSQLStringForDB2(Optional<String> input) {
        if (!input.isPresent())
            return STRING_WHEN_NULL;
        return DB2CyrillicToUnicodeConvertor.obradiTekst('\'' + input.get() + '\'');
    }

}
