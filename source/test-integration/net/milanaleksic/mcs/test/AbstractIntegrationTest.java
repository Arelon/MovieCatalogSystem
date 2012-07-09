package net.milanaleksic.mcs.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: Milan Aleksic
 * Date: 7/9/12
 * Time: 2:57 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring-beans-testing.xml",
        "classpath:spring-beans.xml"
})
public abstract class AbstractIntegrationTest {
}
