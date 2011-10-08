package net.milanaleksic.mcs.domain.model;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="FILM", schema="DB2ADMIN")
@Cacheable
@org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Film implements Serializable, Comparable<Film> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDFILM")
	private int idfilm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="IDZANR", nullable=false)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SELECT)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ZAUZIMA",
        schema = "DB2ADMIN",
        joinColumns = { @JoinColumn(name = "IDFILM") },
        inverseJoinColumns = { @JoinColumn(name = "IDMEDIJ") }
    )
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    @org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
    @org.hibernate.annotations.BatchSize(size=15)
	private Set<Medij> medijs = new HashSet<Medij>(0);

    @Column(name="MEDIJ_LIST")
    private String medijListAsString;

    @Column(name="POZICIJA")
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

	public Set<Medij> getMedijs() {
		return this.medijs;
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
		medijs.add(m);
		m.getFilms().add(this);
        refreshDeNormalizedAttributes();
	}

    public void removeMedij(Medij medij) {
		medijs.remove(medij);
        refreshDeNormalizedAttributes();
    }

    private void refreshDeNormalizedAttributes() {
        this.refreshMedijListAsString();
        this.refreshFilmLocation();
    }

    public String getPozicija() {
        return pozicija;
    }

    private void refreshFilmLocation() {
		// priprema informacija za narednu obradu (polje "prisutan")
		int brojNeprisutnih = 0;
		for (Medij medij : getMedijs()) {
			if (!medij.getPozicija().getPozicija().equals("присутан"))
				brojNeprisutnih++;
		}
		
		if (brojNeprisutnih==0)
			pozicija = "присутан";
		else {
            StringBuilder builder = new StringBuilder();
			for (Medij medij : getMedijs()) {
                builder.append(medij.toString()).append("-").append(medij.getPozicija().toString()).append("; ");
			}
			pozicija = builder.substring(0, builder.length()-2);
		}						
		for (Medij medij : getMedijs()) {
			if (!medij.getPozicija().getPozicija().equals("присутан"))
				pozicija = medij.getPozicija().toString();
		}
	}

    public String getMedijListAsString() {
        return medijListAsString;
    }

    public void refreshMedijListAsString() {
        StringBuilder medijInfo = new StringBuilder();
        Object[] mediji = getMedijs().toArray();
        Arrays.sort(mediji);
        for (Object medij : mediji)
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

}