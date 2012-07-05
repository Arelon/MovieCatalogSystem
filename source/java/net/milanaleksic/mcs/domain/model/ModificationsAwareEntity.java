package net.milanaleksic.mcs.domain.model;

import net.milanaleksic.mcs.domain.service.ModificationLogService;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.Configurable;

import javax.inject.Inject;
import javax.persistence.*;

/**
 * User: Milan Aleksic
 * Date: 7/2/12
 * Time: 3:02 PM
 */
@MappedSuperclass
@Configurable
public abstract class ModificationsAwareEntity {

    @Transient
    protected final Log log = LogFactory.getLog(this.getClass());

    @Transient
    @Inject
    private ModificationLogService modificationLogService;

    @Transient
    public abstract int getId();

    @PostUpdate
    void postUpdate() {
        modificationLogService.reportUpdate(getId(), this);
    }

    @PostPersist
    void postPersist() {
        modificationLogService.reportInsert(getId(), this);
    }

    @PostRemove
    void postRemove() {
        modificationLogService.reportDelete(getId(), this);
    }

}
