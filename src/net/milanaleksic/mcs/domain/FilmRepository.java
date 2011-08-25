package net.milanaleksic.mcs.domain;

import java.util.Set;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 11:32 PM
 */
public interface FilmRepository {

    public class FilmsWithCount {
        public final Set<Film> films;
        public final long count;

        public FilmsWithCount(Set<Film> films, long count) {
            this.films = films;
            this.count = count;
        }
    }

    Film getCompleteFilm(Film rawFilm);

    void deleteFilm(Film film);

    void saveFilm(Film newFilm);

    FilmsWithCount getFilmByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter, Pozicija pozicijaFilter, String filterText);
}
