package net.milanaleksic.mcs.domain.model;

import net.milanaleksic.mcs.application.util.ApplicationException;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface PozicijaRepository {

    List<Pozicija> getPozicijas();

    void addPozicija(String newPozicija);

    void deletePozicijaByName(String pozicija) throws ApplicationException;

    Pozicija getDefaultPozicija();

    Pozicija getByName(String locationName);

}
