package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.domain.*;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

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

    @Autowired
    private ApplicationManager applicationManager;

    @Before
    public void prepare() {
        DOMConfigurator.configure("log4j.xml");
    }

    @Test
    public void get_films() {
        Set<Film> films = filmRepository.getFilmByCriteria(0, 10, null, null, null, null);
        Assert.assertTrue("Films are empty", films.size() != 0);
        Assert.assertEquals("Size is not as expected!", films.size(), 10);
    }

    @Test
    public void get_films_no_limit() {
        Set<Film> films = filmRepository.getFilmByCriteria(0, 0, null, null, null, null);
        Assert.assertTrue("Films are empty", films.size() != 0);
    }

    @Test
    public void get_films_with_zanr() {
        Zanr zanr = zanrRepository.getZanrByName("акција");
        Set<Film> films = filmRepository.getFilmByCriteria(0, 23, zanr, null, null, null);
        for (Film film:films) {
            Assert.assertEquals(zanr.getIdzanr(), film.getZanr().getIdzanr());
        }
        Assert.assertEquals("Size is not as expected!", films.size(), 23);
    }

    @Test
    public void get_films_with_zanr_no_limit() {
        Zanr zanr = zanrRepository.getZanrByName("акција");
        Set<Film> films = filmRepository.getFilmByCriteria(0, 0, zanr, null, null, null);
        Assert.assertTrue("Films are empty", films.size() != 0);
        for (Film film:films) {
            Assert.assertEquals(zanr.getIdzanr(), film.getZanr().getIdzanr());
        }
    }

}
