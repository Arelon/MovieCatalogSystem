package net.milanaleksic.mcs.domain;

import net.milanaleksic.mcs.util.ApplicationException;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface ZanrRepository {

    List<Zanr> getZanrs();

    void addZanr(String newZanr);

    void deleteZanr(String zanr) throws ApplicationException;
}
