package net.milanaleksic.mcs.infrastructure.persistence.jpa.service;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
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
        modificationLogService.pumpAllModificationLogItems(true);
        assertThat("Wrong log size after test data creation", getCurrentLogSize(), equalTo(185L));
        assertThat("Wrong maximum value for clock", getMaxClock(), equalTo(30L));
        assertThat("Wrong aggregated 'medijs' content", getMedijsContent(), equalTo("1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12"));
        checkNumbersOfClockValues( 1,  3); // Pozicija
        checkNumbersOfClockValues( 3,  2); // TipMedija
        checkNumbersOfClockValues( 4,  4); // Medij
        checkNumbersOfClockValues(16,  2); // Zanr
        checkNumbersOfClockValues(18,  2); // Tag
        checkNumbersOfClockValues(20, 11); // Film
    }

    private void checkNumbersOfClockValues(long clockValue, long expectedCount) {
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
