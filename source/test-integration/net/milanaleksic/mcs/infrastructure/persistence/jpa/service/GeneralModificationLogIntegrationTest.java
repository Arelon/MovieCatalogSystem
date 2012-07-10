package net.milanaleksic.mcs.infrastructure.persistence.jpa.service;

import com.google.common.base.*;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.test.AbstractDatabaseIntegrationTest;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import javax.persistence.TypedQuery;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * User: Milan Aleksic
 * Date: 7/9/12
 * Time: 11:07 AM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
@DirtiesContext
public class GeneralModificationLogIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Test
    public void saved_properly_for_insertion() {
        assertThat("Log should be empty at start", getCurrentLogSize(), equalTo(0L));
        addSomeMovies();
        waitForLogToBeSaved();
        assertThat("Wrong log size after test data creation", getCurrentLogSize(), equalTo(185L));
        assertThat("Wrong maximum value for clock", getMaxClock(), equalTo(30L));
        assertThat("Wrong aggregated 'medijs' content", getMedijsContent(), equalTo("1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12"));
        assertNumbersOfClockValues(1, 3); // Pozicija
        assertNumbersOfClockValues(3, 2); // TipMedija
        assertNumbersOfClockValues(4, 4); // Medij
        assertNumbersOfClockValues(16, 2); // Zanr
        assertNumbersOfClockValues(18, 2); // Tag
        assertNumbersOfClockValues(20, 11); // Film
    }

    @Test
    public void saved_properly_for_modification() {
        addSomeMovies();

        final Zanr actionGenre = zanrRepository.getZanrByName("action");
        actionGenre.setZanr("stupid");
        zanrRepository.updateZanr(actionGenre); // creates new mod log and clock=31

        actionGenre.setZanr("stupid");
        zanrRepository.updateZanr(actionGenre); // doesn't create new mod log, doesn't initiate clock increase

        actionGenre.setZanr("action");
        zanrRepository.updateZanr(actionGenre); // creates new mod log and clock=32

        waitForLogToBeSaved();
        assertThat("Wrong log size after test data creation", getCurrentLogSize(), equalTo(187L));
        assertThat("Wrong maximum value for clock", getMaxClock(), equalTo(32L));
    }

    // Utilities

    private void waitForLogToBeSaved() {
        modificationLogService.pumpAllModificationLogItems(true);
    }

    private void assertNumbersOfClockValues(long clockValue, long expectedCount) {
        final TypedQuery<Long> query = entityManager.createQuery("select count(*) from Modification where clock = :clock", Long.class);
        query.setParameter("clock", clockValue);
        final Long actual = query.getSingleResult();
        assertThat("Wrong number of clock values for clockValue=" + clockValue, actual, equalTo(expectedCount));
    }

    private Long getCurrentLogSize() {
        final TypedQuery<Long> query = entityManager.createQuery("select count(*) from Modification", Long.class);
        return query.getSingleResult();
    }

    private Long getMaxClock() {
        final TypedQuery<Long> query = entityManager.createQuery("select max(clock) from Modification", Long.class);
        return query.getSingleResult();
    }

    private String getMedijsContent() {
        final TypedQuery<String> query = entityManager.createQuery("select value from Modification\n" +
                "where field='medijs'\n" +
                "and entity='Film'" +
                "order by id", String.class);
        return Joiner.on(',').join(query.getResultList());
    }

}
