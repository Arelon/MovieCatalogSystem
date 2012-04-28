package net.milanaleksic.mcs.infrastructure.restore.alter;

import java.sql.Connection;

/**
 * User: Milan Aleksic
 * Date: 25/08/11
 * Time: 10:21
 */
public interface AlterScript {

    void executeAlterOnConnection(Connection conn);

}
