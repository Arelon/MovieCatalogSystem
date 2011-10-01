package net.milanaleksic.mcs.domain.model;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 11:32 PM
 */
public interface FilmRepository {

    public class FilmsWithCount {
        public final List<Film> films;
        public final long count;

        public FilmsWithCount(List<Film> films, long count) {
            this.films = films;
            this.count = count;
        }
    }

    Film getCompleteFilm(Film rawFilm);

    void deleteFilm(Film film);

    void saveFilm(Film newFilm, Zanr zanr, List<Medij> medijs, Pozicija position);

    void updateFilm(Film modifiedFilm);

    FilmsWithCount getFilmByCriteria(int startFrom, int maxItems, Zanr zanrFilter, TipMedija tipMedijaFilter, Pozicija pozicijaFilter, String filterText);
}
