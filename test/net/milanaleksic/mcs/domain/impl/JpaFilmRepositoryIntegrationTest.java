package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.*;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: Milan Aleksic
 * Date: 8/23/11
 * Time: 8:50 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-beans.xml"})
public class JpaFilmRepositoryIntegrationTest {

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private ZanrRepository zanrRepository;

    @Before
    public void prepare() {
        DOMConfigurator.configure("log4j.xml");
    }

    @Test
    public void get_films() {
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 10, null, null, null, null);
        assertThat("Films are empty", filmsWithCount.films.size(), not(0));
        assertThat("Size is not as expected!", filmsWithCount.films.size(), equalTo(10));
    }

    @Test
    public void get_films_second_page() {
        FilmRepository.FilmsWithCount filmsWithCountFirstPage = filmRepository.getFilmByCriteria(0, 50, null, null, null, null);
        assertThat("Films are empty", filmsWithCountFirstPage.films.size(), not(0));
        assertThat("Size is not as expected!", filmsWithCountFirstPage.films.size(), equalTo(50));

        FilmRepository.FilmsWithCount filmsWithCountSecondPage = filmRepository.getFilmByCriteria(50, 50, null, null, null, null);
        assertThat("Films are empty", filmsWithCountSecondPage.films.size(), not(0));
        assertThat("Size is not as expected!", filmsWithCountSecondPage.films.size(), equalTo(50));

        for(Film filmFromFirstPage : filmsWithCountFirstPage.films) {
            assertFalse("second page has at least one element from first page", filmsWithCountSecondPage.films.contains(filmFromFirstPage));
        }
    }

    @Test
    public void get_films_no_limit() {
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 0, null, null, null, null);
        assertThat("Films are empty", filmsWithCount.films.size(), not(0));
        assertThat("Films count is not identical to the collection size", 0L+filmsWithCount.films.size(), equalTo(filmsWithCount.count));
    }

    @Test
    public void get_films_with_zanr() {
        Zanr zanr = zanrRepository.getZanrByName("акција");
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 23, zanr, null, null, null);
        for (Film film : filmsWithCount.films) {
            assertThat("Zanr is not correct", zanr.getIdzanr(), equalTo(film.getZanr().getIdzanr()));
        }
        assertThat("Size is not as expected!", filmsWithCount.films.size(), equalTo(23));
    }

    @Test
    public void get_films_with_zanr_no_limit() {
        Zanr zanr = zanrRepository.getZanrByName("акција");
        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(0, 0, zanr, null, null, null);
        Assert.assertTrue("Films are empty", filmsWithCount.films.size() != 0);
        for (Film film : filmsWithCount.films) {
            assertThat("Zanr is not correct", zanr.getIdzanr(), equalTo(film.getZanr().getIdzanr()));
            Assert.assertEquals(zanr.getIdzanr(), film.getZanr().getIdzanr());
        }
        assertThat("Films count is not identical to the collection size!", 0L+filmsWithCount.films.size(), equalTo(filmsWithCount.count));
    }

}
