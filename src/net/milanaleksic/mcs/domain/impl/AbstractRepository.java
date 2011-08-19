package net.milanaleksic.mcs.domain.impl;

import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:38 PM
 */
public abstract class AbstractRepository {

    protected final Logger log = Logger.getLogger(this.getClass());

    @PersistenceContext(name = "MovieCatalogSystemDB")
    protected EntityManager entityManager;

}
