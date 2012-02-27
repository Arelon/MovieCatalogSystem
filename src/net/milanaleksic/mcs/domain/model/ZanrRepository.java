package net.milanaleksic.mcs.domain.model;

import net.milanaleksic.mcs.application.util.ApplicationException;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface ZanrRepository {

    List<Zanr> getZanrs();

    void addZanr(String newZanr);

    void deleteZanrByName(String zanr) throws ApplicationException;

    Zanr getZanrByName(String genreName);

    void updateZanr(Zanr zanr);
}
