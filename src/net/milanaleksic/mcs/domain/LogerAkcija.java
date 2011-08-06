package net.milanaleksic.mcs.domain;

import java.util.HashSet;
import java.util.Set;

public class LogerAkcija implements java.io.Serializable {

	private int idlogerakcija;
	private String naziv;
	private Set<Loger> logers = new HashSet<Loger>(0);

	public LogerAkcija() {
	}

	public LogerAkcija(int idlogerakcija, String naziv) {
		this.idlogerakcija = idlogerakcija;
		this.naziv = naziv;
	}

	public LogerAkcija(int idlogerakcija, String naziv, Set<Loger> logers) {
		this.idlogerakcija = idlogerakcija;
		this.naziv = naziv;
		this.logers = logers;
	}

	public int getIdlogerakcija() {
		return this.idlogerakcija;
	}

	public void setIdlogerakcija(int idlogerakcija) {
		this.idlogerakcija = idlogerakcija;
	}

	public String getNaziv() {
		return this.naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public Set<Loger> getLogers() {
		return this.logers;
	}

	public void setLogers(Set<Loger> logers) {
		this.logers = logers;
	}
	
	public String toString() {
		return getNaziv();
	}
	
	public void addLoger(Loger l) {
		logers.add(l);
		l.setLogerAkcija(this);
	}

}
