package net.milanaleksic.mcs.infrastructure.tmdb;

import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageSearchResult;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: Milan Aleksic
 * Date: 3/6/12
 * Time: 10:24 AM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class ImageSearchTest {

    @Test
    public void parse_response() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ImageSearchResult[] imageSearchResult = mapper.readValue("[{\"id\":550,\"name\":\"Fight Club\",\"posters\":[\n" +
                    "{\"image\":{\"id\":\"4ea5cc8334f8633bdc000a59\",\"type\":\"poster\",\"size\":\"thumb\",\"height\":138,\"width\":92,\"url\":\"http://cf2.imgobject.com/t/p/w92/2lECpi35Hnbpa4y46JX0aY3AWTy.jpg\"}},\n" +
                    "{\"image\":{\"id\":\"4ea5cc8334f8633bdc000a59\",\"type\":\"poster\",\"size\":\"w154\",\"height\":231,\"width\":154,\"url\":\"http://cf2.imgobject.com/t/p/w154/2lECpi35Hnbpa4y46JX0aY3AWTy.jpg\"}}],\"backdrops\":[{\"image\":{\"id\":\"4ec23b495e73d6476b004f09\",\"type\":\"backdrop\",\"size\":\"thumb\",\"height\":169,\"width\":300,\"url\":\"http://cf2.imgobject.com/t/p/w300/jeIYT2hvnXKz7v4OrEut5WKt3em.jpg\"}},\n" +
                    "{\"image\":{\"id\":\"4ec23b495e73d6476b004f09\",\"type\":\"backdrop\",\"size\":\"poster\",\"height\":439,\"width\":780,\"url\":\"http://cf2.imgobject.com/t/p/w780/jeIYT2hvnXKz7v4OrEut5WKt3em.jpg\"}}]}]",
                    ImageSearchResult[].class);
            assertThat("Id is not correct", imageSearchResult[0].getId(), equalTo("550"));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
