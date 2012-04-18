package net.milanaleksic.mcs.domain.model;

import com.google.common.collect.*;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;

import javax.annotation.concurrent.Immutable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Film implements Serializable, Comparable<Film> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idfilm;

    @ManyToOne
    @JoinColumn(name="IDZANR", nullable=false)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SELECT)
	private Zanr zanr;

    @Column(length = 100, nullable = false)
	private String nazivfilma;

    @Column(length = 100, nullable = false)
	private String prevodnazivafilma;

    @Column(nullable = false)
	private int godina;

    @Column(length = 1000)
	private String komentar;

    @Column(name = "IMDB_ID", length = 10)
	private String imdbId;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "ZAUZIMA",
        schema = "DB2ADMIN",
        joinColumns = { @JoinColumn(name = "IDFILM") },
        inverseJoinColumns = { @JoinColumn(name = "IDMEDIJ") }
    )
    @org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
    @org.hibernate.annotations.BatchSize(size=15)
	private Set<Medij> medijs = null;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "HASTAG",
        schema = "DB2ADMIN",
        joinColumns = { @JoinColumn(name = "IDFILM") },
        inverseJoinColumns = { @JoinColumn(name = "IDTAG") }
    )
    @org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
    @org.hibernate.annotations.BatchSize(size=3)
	private Set<Tag> tags = null;

    @Column(name="MEDIJ_LIST")
    private String medijListAsString;

    @Column
    private String pozicija;

	public Film() {
	}

	public int getIdfilm() {
		return this.idfilm;
	}

	private void setIdfilm(int idfilm) {
		this.idfilm = idfilm;
	}

	public Zanr getZanr() {
		return this.zanr;
	}

	public void setZanr(Zanr zanr) {
		this.zanr = zanr;
	}

	public String getNazivfilma() {
		return this.nazivfilma;
	}

	public void setNazivfilma(String nazivfilma) {
		this.nazivfilma = nazivfilma;
	}

	public String getPrevodnazivafilma() {
		return this.prevodnazivafilma;
	}

	public void setPrevodnazivafilma(String prevodnazivafilma) {
		this.prevodnazivafilma = prevodnazivafilma;
	}

	public int getGodina() {
		return this.godina;
	}

	public void setGodina(int godina) {
		this.godina = godina;
	}

	public String getKomentar() {
		return this.komentar;
	}

	public void setKomentar(String komentar) {
		this.komentar = komentar;
	}

	public String getImdbId() {
		return this.imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

    public Set<Tag> getTags() {
		return ImmutableSet.copyOf(this.tags);
	}

	public Set<Medij> getMedijs() {
		return ImmutableSet.copyOf(this.medijs);
	}

	public void setMedijs(Set<Medij> medijs) {
        if ((medijs != this.medijs && this.medijs != null) || (this.medijs == null && medijListAsString == null)) {
            refreshDeNormalizedAttributes();
        }
        this.medijs = medijs;
    }

	public String toString() {
		return getNazivfilma();
	}

	public void addMedij(Medij m) {
        if (medijs == null)
            medijs = Sets.newHashSet();
		medijs.add(m);
		m.getFilms().add(this);
        refreshDeNormalizedAttributes();
	}

    public void removeMedij(Medij medij) {
		medijs.remove(medij);
        refreshDeNormalizedAttributes();
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

	public void addTag(Tag tag) {
        if (tags == null)
            tags = Sets.newHashSet();
		tags.add(tag);
		tag.getFilms().add(this);
	}

    public void removeTag(Tag tag) {
		tags.remove(tag);
    }

    private void refreshDeNormalizedAttributes() {
        this.refreshMedijListAsString();
        this.refreshFilmLocation();
    }

    public String getPozicija() {
        return pozicija;
    }

    public void refreshFilmLocation() {
		// priprema informacija za narednu obradu (polje "prisutan")
        String defaultPozicija = null;
        for (Medij medij : getMedijs()) {
			if (medij.getPozicija().isDefault())
				defaultPozicija = medij.getPozicija().toString();
		}

		int brojNeprisutnih = 0;
		for (Medij medij : getMedijs()) {
			if (!medij.getPozicija().isDefault())
				brojNeprisutnih++;
		}

		if (brojNeprisutnih!=0) {
            StringBuilder builder = new StringBuilder();
			for (Medij medij : getMedijs()) {
                builder.append(medij.toString()).append("-").append(medij.getPozicija().toString()).append("; ");
			}
			this.pozicija = builder.substring(0, builder.length()-2);
		} else {
            this.pozicija = defaultPozicija;
        }
	}

    public String getMedijListAsString() {
        return medijListAsString;
    }

    public void refreshMedijListAsString() {
        StringBuilder medijInfo = new StringBuilder();
        for (Medij medij : Ordering.natural().sortedCopy(getMedijs()))
            medijInfo.append(medij.toString()).append(' ');
        medijListAsString = medijInfo.toString();
    }

    @Override
    public int compareTo(Film o) {
        if (medijListAsString == null)
            refreshMedijListAsString();
        if (o.getMedijListAsString() == null)
            o.refreshMedijListAsString();
		return medijListAsString.compareTo(o.medijListAsString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Film film = (Film) o;

        if (godina != film.godina) return false;
        if (komentar != null ? !komentar.equals(film.komentar) : film.komentar != null) return false;
        if (nazivfilma != null ? !nazivfilma.equals(film.nazivfilma) : film.nazivfilma != null) return false;
        if (prevodnazivafilma != null ? !prevodnazivafilma.equals(film.prevodnazivafilma) : film.prevodnazivafilma != null)
            return false;
        if (zanr != null ? !zanr.equals(film.zanr) : film.zanr != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zanr != null ? zanr.hashCode() : 0;
        result = 31 * result + (nazivfilma != null ? nazivfilma.hashCode() : 0);
        result = 31 * result + (prevodnazivafilma != null ? prevodnazivafilma.hashCode() : 0);
        result = 31 * result + godina;
        result = 31 * result + (komentar != null ? komentar.hashCode() : 0);
        return result;
    }

    public void copyFromMovie(Movie movie) {
        this.setKomentar(movie.getOverview());
        try {
            this.setGodina(Integer.parseInt(movie.getReleasedYear()));
        } catch (NumberFormatException e) {
            this.setGodina(0);
        }
        this.setImdbId(movie.getImdbId());
        this.setNazivfilma(movie.getName());
    }
}