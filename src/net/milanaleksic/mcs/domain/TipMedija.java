package net.milanaleksic.mcs.domain;

import java.util.HashSet;
import java.util.Set;

public class TipMedija implements java.io.Serializable {

	private int idtip;
	private String naziv;
	private Set<Medij> medijs = new HashSet<Medij>(0);

	public TipMedija() {
	}

	public int getIdtip() {
		return this.idtip;
	}

	private void setIdtip(int idtip) {
		this.idtip = idtip;
	}

	public String getNaziv() {
		return this.naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public Set<Medij> getMedijs() {
		return this.medijs;
	}

	public void setMedijs(Set<Medij> medijs) {
		this.medijs = medijs;
	}
	
	public String toString() {
		return getNaziv();
	}
	
	public void addMedij(Medij m) {
		medijs.add(m);
		m.setTipMedija(this);
	}


}
