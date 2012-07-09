package net.milanaleksic.mcs.test;

import com.google.common.collect.Lists;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.ModificationLogService;
import net.milanaleksic.mcs.infrastructure.restore.RestorePointRestorer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.*;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import javax.persistence.*;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * User: Milan Aleksic
 * Date: 7/9/12
 * Time: 11:12 AM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public abstract class AbstractDatabaseIntegrationTest extends AbstractIntegrationTest {

    @PersistenceContext(name = "MovieCatalogSystemDB")
    protected EntityManager entityManager;

    @Inject
    protected PlatformTransactionManager transactionManager;

    @Inject
    protected RestorePointRestorer restorePointRestorer;

    @Inject
    protected ModificationLogService modificationLogService;

    @Inject
    protected FilmRepository filmRepository;

    @Inject
    protected ZanrRepository zanrRepository;

    @Inject
    protected TagRepository tagRepository;

    @Inject
    protected PozicijaRepository pozicijaRepository;

    @Inject
    protected TipMedijaRepository tipMedijaRepository;

    @Inject
    protected MedijRepository medijRepository;

    @Before
    public void prepare() {
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            final Query nativeQuery = entityManager.createNativeQuery("drop schema if exists db2admin");
            nativeQuery.executeUpdate();
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            fail("Unexpected integration test DB failure");
        }
        restorePointRestorer.restoreDatabaseIfNeeded();
    }

    @After
    public void clearDB() {
        modificationLogService.pumpAllModificationLogItems(true);
    }

    protected void addSomeMovies() {
        pozicijaRepository.addPozicija(new Pozicija("at home", false));
        final Pozicija atJob = pozicijaRepository.addPozicija(new Pozicija("at job", true));
        final TipMedija dvdType = tipMedijaRepository.addTipMedija("DVD");
        final Medij[] allMedijs = {
                medijRepository.saveMedij(1, dvdType),
                medijRepository.saveMedij(2, dvdType),
                medijRepository.saveMedij(3, dvdType),
                medijRepository.saveMedij(4, dvdType),
                medijRepository.saveMedij(5, dvdType),
                medijRepository.saveMedij(6, dvdType),
                medijRepository.saveMedij(7, dvdType),
                medijRepository.saveMedij(8, dvdType),
                medijRepository.saveMedij(9, dvdType),
                medijRepository.saveMedij(10, dvdType),
                medijRepository.saveMedij(11, dvdType),
                medijRepository.saveMedij(12, dvdType)
        };
        final Zanr actionGenre = zanrRepository.addZanr("action");
        final Zanr horrorGenre = zanrRepository.addZanr("horror");
        final Tag primeTag = tagRepository.addTag("prime");
        final Tag oddTag = tagRepository.addTag("odd");
        for (int i = 0; i < 11; i++) {
            final Film film = new Film();
            film.setNazivfilma("Test movie " + i);
            film.setPrevodnazivafilma("Translation of test movie");
            film.setGodina(2000);
            film.setImdbId("");
            film.setKomentar("Test komentar");
            final List<Tag> tags = Lists.newLinkedList();
            if (i % 2 == 1)
                tags.add(oddTag);
            if (i == 1 || i == 3 || i == 5 || i == 7)
                tags.add(primeTag);
            filmRepository.saveFilm(
                    film,
                    i >= 9 ? horrorGenre : actionGenre,
                    Lists.newArrayList(allMedijs[i], allMedijs[i + 1]),
                    atJob,
                    tags);
        }
    }

}
