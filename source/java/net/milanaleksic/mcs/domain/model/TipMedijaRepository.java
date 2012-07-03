package net.milanaleksic.mcs.domain.model;

import net.milanaleksic.mcs.application.util.ApplicationException;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface TipMedijaRepository {

    List<TipMedija> getTipMedijas();

    TipMedija getTipMedija(String name);

    void deleteMediumTypeByName(String mediumTypeName) throws ApplicationException;

    TipMedija addTipMedija(String newMediumType);

    void updateTipMedija(TipMedija tipMedija);
}
