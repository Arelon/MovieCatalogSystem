package net.milanaleksic.mcs.domain;

// Generated 07.11.2007. 23.32.08 by Hibernate Tools 3.2.0.b9

import java.util.HashSet;
import java.util.Set;


/**
 * Medij generated by hbm2java
 */
public class Medij implements java.io.Serializable, Comparable<Medij> {

	private int idmedij;
	private TipMedija tipMedija;
	private Pozicija pozicija;
	private int indeks;
	private Set<Film> films = new HashSet<Film>(0);
	
	private String stringVal= null;

	public Medij() {
	}

	public Medij(int idmedij, TipMedija tipMedija, Pozicija pozicija, int indeks) {
		this.idmedij = idmedij;
		this.tipMedija = tipMedija;
		this.pozicija = pozicija;
		this.indeks = indeks;
	}

	public Medij(int idmedij, TipMedija tipMedija, Pozicija pozicija, int indeks, Set<Film> films) {
		this.idmedij = idmedij;
		this.tipMedija = tipMedija;
		this.pozicija = pozicija;
		this.indeks = indeks;
		this.films = films;
	}

	public int getIdmedij() {
		return this.idmedij;
	}

	public void setIdmedij(int idmedij) {
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

	@Override
	public String toString() {
		if (stringVal == null) {
			String tmpId = String.valueOf(getIndeks());
			while (tmpId.length()<3)
				tmpId = '0'+tmpId;
			stringVal = tipMedija.getNaziv()+tmpId;
		}
		return stringVal;
	}

	@Override
	public int compareTo(Medij o) {
		return toString().compareTo(o.toString());
	}

}