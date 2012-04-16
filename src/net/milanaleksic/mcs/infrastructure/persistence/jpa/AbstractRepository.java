package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:38 PM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public abstract class AbstractRepository {

    protected static final String HIBERNATE_HINT_CACHEABLE = "org.hibernate.cacheable";

    @PersistenceContext(name = "MovieCatalogSystemDB")
    protected EntityManager entityManager;

}
