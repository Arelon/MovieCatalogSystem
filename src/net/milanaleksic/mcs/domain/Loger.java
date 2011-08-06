package net.milanaleksic.mcs.domain;

import java.util.Date;

public class Loger implements java.io.Serializable {

	private int idloger;
	private LogerAkcija logerAkcija;
	private String kontekst;
	private Date vreme;

	public Loger() {
	}

	public Loger(int idloger, LogerAkcija logerAkcija, String kontekst, Date vreme) {
		this.idloger = idloger;
		this.logerAkcija = logerAkcija;
		this.kontekst = kontekst;
		this.vreme = vreme;
	}

	public int getIdloger() {
		return this.idloger;
	}

	public void setIdloger(int idloger) {
		this.idloger = idloger;
	}

	public LogerAkcija getLogerAkcija() {
		return this.logerAkcija;
	}

	public void setLogerAkcija(LogerAkcija logerAkcija) {
		this.logerAkcija = logerAkcija;
	}

	public String getKontekst() {
		return this.kontekst;
	}

	public void setKontekst(String kontekst) {
		this.kontekst = kontekst;
	}

	public Date getVreme() {
		return this.vreme;
	}

	public void setVreme(Date vreme) {
		this.vreme = vreme;
	}
	
	public String toString() {
		return getKontekst();
	}


}
