package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:38 PM
 */
public abstract class AbstractRepository {

    @PersistenceContext(name = "MovieCatalogSystemDB")
    protected EntityManager entityManager;

}
