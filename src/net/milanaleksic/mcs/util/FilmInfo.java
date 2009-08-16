package net.milanaleksic.mcs.util;

/**
 * @author Milan
 * 23 Sep 2007
 */
public class FilmInfo implements Comparable<FilmInfo> {
	
	private String medij = null;
	private String nazivFilma = null;
	private String prevodFilma = null;
	private String zanr = null;
	private String pozicija = null;
	private String komentar = null;
	private int id = -1;
	
	public FilmInfo(Integer id, String medij, String nazivFilma, String prevodFilma, String zanr, String pozicija, String komentar) {
		super();
		this.id = id;
		this.medij = medij;
		this.nazivFilma = nazivFilma;
		this.prevodFilma = prevodFilma;
		this.zanr = zanr;
		this.pozicija = pozicija;
		this.komentar = komentar;
	}
	
	public String getMedij() {
		return medij;
	}
	public void setMedij(String medij) {
		this.medij = medij;
	}
	public String getNazivFilma() {
		return nazivFilma;
	}
	public void setNazivFilma(String nazivFilma) {
		this.nazivFilma = nazivFilma;
	}
	public String getPrevodFilma() {
		return prevodFilma;
	}
	public void setPrevodFilma(String prevodFilma) {
		this.prevodFilma = prevodFilma;
	}
	public String getZanr() {
		return zanr;
	}
	public void setZanr(String zanr) {
		this.zanr = zanr;
	}
	public String getPozicija() {
		return pozicija;
	}
	public void setPozicija(String pozicija) {
		this.pozicija = pozicija;
	}
	public String getKomentar() {
		return komentar;
	}
	public void setKomentar(String komentar) {
		this.komentar = komentar;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int compareTo(FilmInfo o) {
		return medij.compareTo(o.medij);
	}
	
}
