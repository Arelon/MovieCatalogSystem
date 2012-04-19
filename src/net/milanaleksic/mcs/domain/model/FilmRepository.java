package net.milanaleksic.mcs.domain.model;

import com.google.common.base.Optional;

import javax.persistence.metamodel.SingularAttribute;
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

    void saveFilm(Film newFilm, Zanr zanr, Iterable<Medij> medijs, Pozicija position, Iterable<Tag> selectedTags);

    FilmsWithCount getFilmByCriteria(int startFrom, int maxItems, Optional<Zanr> zanrFilter, Optional<TipMedija> tipMedijaFilter,
                                     Optional<Pozicija> pozicijaFilter, Optional<Tag> tagFilter, Optional<String> filterText,
                                     SingularAttribute<Film, String> orderByAttribute, boolean ascending);

}
