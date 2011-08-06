package net.milanaleksic.mcs.domain;

import java.util.HashSet;
import java.util.Set;

public class Zanr implements java.io.Serializable {

	private int idzanr;
	private String zanr;
	private Set<Film> films = new HashSet<Film>(0);

	public Zanr() {
	}

	public Zanr(int idzanr, String zanr) {
		this.idzanr = idzanr;
		this.zanr = zanr;
	}

	public Zanr(int idzanr, String zanr, Set<Film> films) {
		this.idzanr = idzanr;
		this.zanr = zanr;
		this.films = films;
	}

	public int getIdzanr() {
		return this.idzanr;
	}

	public void setIdzanr(int idzanr) {
		this.idzanr = idzanr;
	}

	public String getZanr() {
		return this.zanr;
	}

	public void setZanr(String zanr) {
		this.zanr = zanr;
	}

	public Set<Film> getFilms() {
		return this.films;
	}

	public void setFilms(Set<Film> films) {
		this.films = films;
	}
	
	public String toString() {
		return getZanr();
	}
	
	public void addFilm(Film f) {
		films.add(f);
		f.setZanr(this);
	}


}
