package net.milanaleksic.mcs.domain.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * User: Milan Aleksic
 * Date: 7/2/12
 * Time: 4:16 PM
 */
@Entity
public class Modification implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idmodification;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private ModificationType modificationType;

    @Column(nullable = false)
    private int entityId;

    @Column(nullable = false)
    private String entity;

    @Column(nullable = false)
    private long clock;

    @Column
    private String field;

    @Column
    private String value;

    @Column(nullable = false)
    private int dbVersion;

    public int getIdmodification() {
        return idmodification;
    }

    private void setIdmodification(int idmodification) {
        this.idmodification = idmodification;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public long getClock() {
        return clock;
    }

    public void setClock(long clock) {
        this.clock = clock;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public void setModificationType(ModificationType modificationType) {
        this.modificationType = modificationType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
}
