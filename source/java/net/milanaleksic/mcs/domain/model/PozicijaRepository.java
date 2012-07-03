package net.milanaleksic.mcs.domain.model;

import com.google.common.base.Optional;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface PozicijaRepository {

    List<Pozicija> getPozicijas();

    Pozicija addPozicija(Pozicija position);

    void deletePozicijaByName(String pozicija);

    Optional<Pozicija> getDefaultPozicija();

    Pozicija getByName(String locationName);

    void updatePozicija(Pozicija pozicija);
}
