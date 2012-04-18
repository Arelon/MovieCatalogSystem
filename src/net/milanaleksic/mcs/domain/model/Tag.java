package net.milanaleksic.mcs.domain.model;

import javax.persistence.*;
import java.util.Set;

/**
 * User: Milan Aleksic
 * Date: 4/18/12
 * Time: 11:32 AM
 */
@Entity
@Cacheable
@org.hibernate.annotations.Cache(region = "mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idtag;

    @Column(length = 100)
    private String naziv;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    @org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
    @org.hibernate.annotations.BatchSize(size=15)
    private Set<Film> films = null;

    public Set<Film> getFilms() {
		return this.films;
	}

	public void setFilms(Set<Film> films) {
		this.films = films;
	}

    public void removeFilm(Film film) {
        films.remove(film);
    }

    public int getIdtag() {
        return idtag;
    }

    private void setIdtag(int idtag) {
        this.idtag = idtag;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        if (naziv != null ? !naziv.equals(tag.naziv) : tag.naziv != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return naziv != null ? naziv.hashCode() : 0;
    }

    @Override
    public String toString() {
        return naziv;
    }

}
