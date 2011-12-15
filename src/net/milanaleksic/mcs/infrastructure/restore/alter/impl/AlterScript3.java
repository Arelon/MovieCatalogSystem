package net.milanaleksic.mcs.infrastructure.restore.alter.impl;

import net.milanaleksic.mcs.infrastructure.restore.alter.AlterScript;
import net.milanaleksic.mcs.infrastructure.util.DBUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Milan Aleksic
 * Date: 25/08/11
 * Time: 10:22
 *
 * This Script fills up the denormalized column "MEDIJ_LIST" to allow fast fetching
 * and paging as part of the main movie fetch algorithm
 */
public class AlterScript3 implements AlterScript {

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void executeAlterOnConnection(Connection conn) {
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            conn.setAutoCommit(false);
            st = conn.prepareStatement("SELECT idfilm from DB2ADMIN.Film");
            rs = st.executeQuery();
            Set<Integer> filmIds = new HashSet<Integer>();
            while (rs.next()) {
                filmIds.add(rs.getInt(1));
            }

            if (log.isInfoEnabled())
                log.info("Film Ids size: " + filmIds.size());

            DBUtil.close(rs);
            DBUtil.close(st);

            String defaultPosition = "присутан";

            for (Integer idfilm : filmIds) {
                st = conn.prepareStatement("SELECT naziv, indeks, pozicija from DB2ADMIN.Zauzima z\n" +
                        "inner join db2admin.medij m on z.idmedij=m.idmedij \n" +
                        "inner join db2admin.tipmedija t on t.idtip=m.idtip \n" +
                        "inner join db2admin.pozicija p on m.idpozicija=p.idpozicija \n" +
                        "where idfilm=?\n" +
                        "order by m.idtip, m.indeks");
                st.setInt(1, idfilm);
                rs = st.executeQuery();

                String filmPosition = defaultPosition;
                StringBuilder builder = new StringBuilder();
                while (rs.next()) {
                    if (builder.length()!=0)
                        builder.append(" ");
                    String indeks = rs.getString(2);
                    while (indeks.length()<3)
                        indeks = '0'+indeks;
                    builder.append(rs.getString(1)).append(indeks);
                    String position = rs.getString(3);
                    if (!defaultPosition.equals(position)) {
                        filmPosition = position;
                    }
                }
                String medijList = builder.toString();

                if (log.isInfoEnabled())
                    log.info("Setting medij list: "+medijList+" for movie "+idfilm);

                DBUtil.close(rs);
                DBUtil.close(st);

                st = conn.prepareStatement("update DB2ADMIN.Film set MEDIJ_LIST=?, POZICIJA=? where idfilm=?");
                st.setString(1, medijList);
                st.setString(2, filmPosition);
                st.setInt(3, idfilm);
                int cntRows = st.executeUpdate();
                if (cntRows != 1)
                    log.warn("Could not update movie with id:" + idfilm);

                DBUtil.close(rs);
                DBUtil.close(st);
            }
            conn.commit();
            conn.setAutoCommit(false);
        } catch (Exception e) {
            log.error("Validation failed - " + e.getMessage());
        } finally {
            DBUtil.close(rs);
            DBUtil.close(st);
        }
    }

}
