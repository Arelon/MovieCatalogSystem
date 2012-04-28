package net.milanaleksic.mcs.domain.service;

import net.milanaleksic.mcs.domain.model.Medij;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 2/28/12
 * Time: 12:19 PM
 */
public interface MedijService {

    int getNextMedijIndeks(String s) ;

    List<Medij> getListOfUnusedMediums();

}
