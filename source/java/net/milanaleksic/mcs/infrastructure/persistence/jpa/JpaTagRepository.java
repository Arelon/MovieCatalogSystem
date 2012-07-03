package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 8/18/11
 * Time: 10:37 PM
 */
@Repository
@Transactional
@SuppressWarnings({"HardCodedStringLiteral"})
public class JpaTagRepository extends AbstractRepository implements TagRepository {

    @Override
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public List<Tag> getTags() {
        return entityManager.createNamedQuery("getTagsOrdered", Tag.class).getResultList();
    }

    @Override
    public Tag addTag(String tag) {
        Tag ofTheJedi = new Tag();
        ofTheJedi.setNaziv(tag);
        entityManager.persist(ofTheJedi);
        return ofTheJedi;
    }

    @Override
    public void deleteTagByName(String tag) {
        Tag tagToDelete = getTagByName(tag);
        entityManager.remove(tagToDelete);
    }

    @Override
    @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
    public Tag getTagByName(String tagName) {
        TypedQuery<Tag> tagByName = entityManager.createNamedQuery("getTagByName", Tag.class);
        tagByName.setParameter("tagName", tagName);
        return tagByName.getSingleResult();
    }

    @Override
    public void updateTag(Tag tag) {
        entityManager.merge(tag);
    }

}
