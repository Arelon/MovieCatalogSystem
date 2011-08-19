package net.milanaleksic.mcs.domain;

import net.milanaleksic.mcs.util.ApplicationException;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface PozicijaRepository {

    List<Pozicija> getPozicijas();

    void addPozicija(String newPozicija);

    void deletePozicija(String pozicija) throws ApplicationException;

    Pozicija getDefaultPozicija();

}
