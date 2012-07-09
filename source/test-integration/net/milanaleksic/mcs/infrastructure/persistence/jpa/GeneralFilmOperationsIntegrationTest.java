package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.test.AbstractDatabaseIntegrationTest;
import org.junit.*;

import javax.inject.Inject;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * User: Milan Aleksic
 * Date: 8/23/11
 * Time: 8:50 PM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class GeneralFilmOperationsIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Inject
    private FilmService filmService;

    @Test
    public void get_films() {
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 10, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        assertThat("Films should be empty", filmsWithCount.films.size(), equalTo(0));
        addSomeMovies();
        filmsWithCount = filmRepository.getFilmByCriteria(0, 10, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        assertThat("Films should not be empty", filmsWithCount.films.size(), not(0));
        assertThat("Size is not as expected", filmsWithCount.films.size(), equalTo(10));
    }

    @Test
    public void get_films_second_page() {
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 7, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        assertThat("Films should be empty", filmsWithCount.films.size(), equalTo(0));
        addSomeMovies();

        FilmRepository.FilmsWithCount filmsWithCountFirstPage = filmRepository.getFilmByCriteria(0, 7, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        assertThat("Films are empty", filmsWithCountFirstPage.films.size(), not(0));
        assertThat("Size is not as expected", filmsWithCountFirstPage.films.size(), equalTo(7));

        FilmRepository.FilmsWithCount filmsWithCountSecondPage = filmRepository.getFilmByCriteria(7, 5, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        assertThat("Films are empty", filmsWithCountSecondPage.films.size(), not(0));
        assertThat("Size is not as expected", filmsWithCountSecondPage.films.size(), equalTo(4));

        for (Film filmFromFirstPage : filmsWithCountFirstPage.films) {
            assertFalse("second page has at least one element from first page", filmsWithCountSecondPage.films.contains(filmFromFirstPage));
        }
    }

    @Test
    public void get_films_no_limit() {
        addSomeMovies();
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 0, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        assertThat("There should be 11 movies", filmsWithCount.films.size(), equalTo(11));
        assertThat("Films count is not identical to the collection size", 0L + filmsWithCount.films.size(), equalTo(filmsWithCount.count));
    }

    @Test
    public void get_films_with_zanr() {
        addSomeMovies();
        Zanr zanr = zanrRepository.getZanrByName("action");
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 9, Optional.of(zanr), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        for (Film film : filmsWithCount.films) {
            assertThat("Zanr is not correct", zanr.getIdzanr(), equalTo(film.getZanr().getIdzanr()));
        }
        assertThat("Size is not as expected", filmsWithCount.films.size(), equalTo(9));
    }

    @Test
    public void get_films_with_zanr_no_limit() {
        addSomeMovies();
        Zanr zanr = zanrRepository.getZanrByName("action");
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 0, Optional.of(zanr), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        Assert.assertTrue("Films are empty", filmsWithCount.films.size() != 0);
        for (Film film : filmsWithCount.films) {
            assertThat("Zanr is not correct", zanr.getIdzanr(), equalTo(film.getZanr().getIdzanr()));
            Assert.assertEquals(zanr.getIdzanr(), film.getZanr().getIdzanr());
        }
        assertThat("Films count is not identical to the collection size", 0L + filmsWithCount.films.size(), equalTo(filmsWithCount.count));
    }

    @Test
    public void get_films_with_tag() {
        addSomeMovies();
        Tag tag = tagRepository.getTagByName("prime");
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 0, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.of(tag), Optional.<String>absent(), Film_.medijListAsString, true);
        for (Film film : filmsWithCount.films) {
            film = filmRepository.getCompleteFilm(film.getIdfilm());
            assertThat("Tag is not correct", film.getTags(), hasItem(tag));
        }
        assertThat("Number of prime items is not as expected", filmsWithCount.films.size(), equalTo(4));

        tag = tagRepository.getTagByName("odd");
        filmsWithCount = filmRepository.getFilmByCriteria(0, 0, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.of(tag), Optional.<String>absent(), Film_.medijListAsString, true);
        for (Film film : filmsWithCount.films) {
            film = filmRepository.getCompleteFilm(film.getIdfilm());
            assertThat("Tag is not correct", film.getTags(), hasItem(tag));
        }
        assertThat("Number of odd items is not as expected", filmsWithCount.films.size(), equalTo(5));
    }

    @Test
    public void change_location_for_a_movie() {
        addSomeMovies();
        final Pozicija atHome = pozicijaRepository.getByName("at home");
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 0, Optional.<Zanr>absent(), Optional.<TipMedija>absent(), Optional.<Pozicija>absent(), Optional.<Tag>absent(), Optional.<String>absent(), Film_.medijListAsString, true);
        Film film = filmRepository.getCompleteFilm(filmsWithCount.films.get(0).getIdfilm());
        assertThat("Movie should be at job", film.getPozicija(), equalTo("at job"));
        film = filmService.updateFilmWithChanges(film, film.getZanr(), film.getMedijs(), Optional.of(atHome), film.getTags());

        Film filmFromDb = filmRepository.getCompleteFilm(film.getIdfilm());
        assertThat("Movie should have consistent information", film, equalTo(filmFromDb));
        assertThat("Movie should be at home", film.getPozicija(), equalTo(atHome.getPozicija()));

        Film anotherFilm = filmRepository.getCompleteFilm(filmsWithCount.films.get(1).getIdfilm());
        assertThat("Movie should be marked as having one DVD at home and one at job", anotherFilm.getPozicija(), equalTo("at home: DVD002; at job: DVD003"));
    }

}
