package net.milanaleksic.mcs.domain;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 11:32 PM
 */
public interface FilmRepository {

    Film getFilm(int idfilm);

    void deleteFilm(int filmId);
}
