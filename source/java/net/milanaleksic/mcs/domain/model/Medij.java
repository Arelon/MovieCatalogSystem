package net.milanaleksic.mcs.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.util.Set;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Medij implements java.io.Serializable, Comparable<Medij> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idmedij;

    @ManyToOne
    @JoinColumn(name="IDTIP", nullable = false)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SELECT)
    private TipMedija tipMedija;

    @ManyToOne
    @JoinColumn(name="IDPOZICIJA", nullable = false)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SELECT)
    private Pozicija pozicija;

    @Column(nullable = false)
    private int indeks;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "medijs")
    @org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
    @org.hibernate.annotations.BatchSize(size=3)
	private Set<Film> films = Sets.newHashSet();
	
	@Transient
    private String toStringValue = null;

	public Medij() {
	}

    public Medij(int indeks, TipMedija tipMedija, Pozicija pozicija) {
        this.indeks = indeks;
        this.tipMedija = tipMedija;
        this.pozicija = pozicija;
    }

    public int getIdmedij() {
		return this.idmedij;
	}

	private void setIdmedij(int idmedij) {
		this.idmedij = idmedij;
	}

	public TipMedija getTipMedija() {
		return this.tipMedija;
	}

	public void setTipMedija(TipMedija tipMedija) {
		this.tipMedija = tipMedija;
	}

	public Pozicija getPozicija() {
		return this.pozicija;
	}

	public void setPozicija(Pozicija pozicija) {
		this.pozicija = pozicija;
	}

	public int getIndeks() {
		return this.indeks;
	}

	public void setIndeks(int indeks) {
		this.indeks = indeks;
	}

	public Set<Film> getFilms() {
		return this.films;
	}

	public void setFilms(Set<Film> films) {
		this.films = films;
	}

    public void removeFilm(Film film) {
        films.remove(film);
    }

	@Override
	public String toString() {
		if (toStringValue == null) {
			String tmpId = String.valueOf(getIndeks());
			while (tmpId.length()<3)
				tmpId = '0'+tmpId;
			toStringValue = tipMedija.getNaziv()+tmpId;
		}
		return toStringValue;
	}

	@Override
	public int compareTo(Medij o) {
		return toString().compareTo(o.toString());
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Medij medij = (Medij) o;

        if (indeks != medij.indeks) return false;
        if (pozicija != null ? !pozicija.equals(medij.pozicija) : medij.pozicija != null) return false;
        if (tipMedija != null ? !tipMedija.equals(medij.tipMedija) : medij.tipMedija != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tipMedija != null ? tipMedija.hashCode() : 0;
        result = 31 * result + (pozicija != null ? pozicija.hashCode() : 0);
        result = 31 * result + indeks;
        return result;
    }

}
