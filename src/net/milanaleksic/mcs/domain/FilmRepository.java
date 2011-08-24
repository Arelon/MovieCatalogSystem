package net.milanaleksic.mcs.domain;

import java.util.Set;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 11:32 PM
 */
public interface FilmRepository {

    void deleteFilm(Film film);

    void saveFilm(Film newFilm);

    Set<Film> getFilmByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter, Pozicija pozicijaFilter, String filterText);
}
