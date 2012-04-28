package net.milanaleksic.mcs.domain.model;

import net.milanaleksic.mcs.application.util.ApplicationException;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
public interface TagRepository {

    List<Tag> getTags();

    Tag getTagByName(String name);

    void deleteTagByName(String name) throws ApplicationException;

    void addTag(String newTag);

    void updateTag(Tag tag);
}
