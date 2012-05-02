package net.milanaleksic.mcs.infrastructure.gui.transformer;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 5/2/12
 * Time: 11:55 AM
 */
public interface Builder<T> {

    T create(List<String> parameters);

}
