package net.milanaleksic.mcs.domain.impl;

import net.milanaleksic.mcs.domain.Film;
import net.milanaleksic.mcs.domain.FilmRepository;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

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

    @Before
    public void prepare() {
        DOMConfigurator.configure("log4j.xml");
    }

    @Test
    public void test_getFilmByCriteria() {
        List<Film> films = filmRepository.getFilmByCriteria(0, 10, null, null, null, null);
        Assert.assertTrue("Films are empty", films.size() != 0);
    }

}
