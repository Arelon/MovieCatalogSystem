package net.milanaleksic.mcs.config;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * User: Milan Aleksic
 * Date: 8/1/11
 * Time: 11:50 PM
 */
@XmlRootElement
public class UserConfiguration {

    private int elementsPerPage;

    @SuppressWarnings("unused")
    public UserConfiguration() {
        elementsPerPage = 30;
    }

    public int getElementsPerPage() {
        return elementsPerPage;
    }

    public void setElementsPerPage(int elementsPerPage) {
        this.elementsPerPage = elementsPerPage;
    }

    @Override
    public String toString() {
        return "UserConfiguration{" +
                "elementsPerPage=" + elementsPerPage +
                '}';
    }
}
