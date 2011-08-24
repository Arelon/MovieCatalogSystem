package net.milanaleksic.mcs.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="TIPMEDIJA", schema="DB2ADMIN")
@Cacheable
@org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class TipMedija implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="IDTIP")
	private int idtip;

    @Column(name="NAZIV", length = 100, nullable = false)
	private String naziv;

    @OneToMany(mappedBy = "tipMedija")
    @org.hibernate.annotations.BatchSize(size=15)
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
