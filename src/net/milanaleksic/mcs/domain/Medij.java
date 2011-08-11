package net.milanaleksic.mcs.domain;

import java.util.HashSet;
import java.util.Set;

public class Medij implements java.io.Serializable, Comparable<Medij> {

	private int idmedij;
	private TipMedija tipMedija;
	private Pozicija pozicija;
	private int indeks;
	private Set<Film> films = new HashSet<Film>(0);
	
	private String stringVal= null;

	public Medij() {
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
