package net.milanaleksic.mcs.domain.impl;

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
