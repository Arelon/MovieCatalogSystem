package net.milanaleksic.mcs.infrastructure.tenrec.impl;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.infrastructure.tenrec.TenrecService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * User: Milan Aleksic
 * Date: 5/11/12
 * Time: 9:02 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-beans.xml"})
public class TenrecServiceImplTest {

    @Inject
    private TenrecService tenrecService;

    @Inject
    private ApplicationManager applicationManager;

    @Before
    public void prepare() {
        ((TenrecServiceImpl)tenrecService).applicationStarted(applicationManager.getApplicationConfiguration(), applicationManager.getUserConfiguration());
    }

    @Test
    public void connect_and_login() {
        try {
            assertThat("failure to log in to Tenrec", tenrecService.login(), equalTo(true));
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tenrecService.logOut();
        }
    }

    @Test
    public void connect_and_save_db() {
        try {
            connect_and_login();
            assertThat("failure to save couple of bytes in Tenrec", tenrecService.saveDatabase(new byte[] {1,2,3}), equalTo(true));
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tenrecService.logOut();
        }
    }

    @Test
    public void save_db_without_prior_connection() {
        try {
            assertThat("failure to save couple of bytes in Tenrec", tenrecService.saveDatabase(new byte[] {1,2,3}), equalTo(true));
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tenrecService.logOut();
        }
    }

}
