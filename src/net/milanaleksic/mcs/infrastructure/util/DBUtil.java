package net.milanaleksic.mcs.infrastructure.util;

import net.milanaleksic.mcs.infrastructure.restore.DB2CyrillicToUnicodeConvertor;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;

/**
 * User: Milan Aleksic
 * Date: 25/08/11
 * Time: 11:31
 */
public final class DBUtil {

    private final static Logger log = Logger.getLogger(DBUtil.class);

    public static void close(InputStream is) {
        if (is != null) {
            try { is.close(); } catch(IOException ignored) {}
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null)
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Failure while closing ResultSet", e);
            }
    }

    public static void close(PreparedStatement ps) {
        if (ps != null)
            try {
                ps.close();
            } catch (SQLException e) {
                log.error("Failure while closing PreparedStatement", e);
            }
    }

    public static void close(Connection conn) {
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Failure while closing DB Connection", e);
            }
    }

    public static void executeScriptOnConnection(InputStream stream, Connection conn) throws IOException, SQLException {
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        StringBuilder script = new StringBuilder();
        String line;
        while ((line = scriptStreamReader.readLine()) != null) {
            script.append(line).append("\r\n");
        }

        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(script.toString());
            st.execute();
            conn.commit();
        } finally {
            DBUtil.close(st);
        }
    }

    public static void executeScriptOnConnection(String restartCountersScript, Connection conn) throws IOException, SQLException {
        File fileRestartCountersScript = new File(restartCountersScript);
        log.info("Executing script file: " + fileRestartCountersScript.getName());
        if (fileRestartCountersScript.exists()) {
            FileInputStream fis = new FileInputStream(fileRestartCountersScript);
            try {
                DBUtil.executeScriptOnConnection(fis, conn);
            } finally {
                DBUtil.close(fis);
            }
        }
    }

    public static String getSQLString(String input) {
        if (input == null)
            return "NULL";
        return '\'' + input.replaceAll("'", "''") + '\'';
    }

    public static String getSQLStringForDB2(String input) {
        if (input == null)
            return "NULL";
        //log.debug("DB2 Unicode konvertor vratio: "+input+" -> "+tmp);
        return DB2CyrillicToUnicodeConvertor.obradiTekst('\'' + input + '\'');
    }

}
