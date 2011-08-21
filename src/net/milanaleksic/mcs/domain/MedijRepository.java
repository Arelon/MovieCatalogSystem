package net.milanaleksic.mcs.domain;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface MedijRepository {

    public int getNextMedijIndeks(String s) ;

    void saveMedij(int index, String mediumTypeName);

    List<Medij> getMedijs();
}
