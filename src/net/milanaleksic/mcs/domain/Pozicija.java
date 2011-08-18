package net.milanaleksic.mcs.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="POZICIJA", schema="DB2ADMIN")
@org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Pozicija implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="IDPOZICIJA")
	private int idpozicija;

    @Column(name="POZICIJA", length = 100, nullable = false)
	private String pozicija;

	@OneToMany(mappedBy = "pozicija")
    private Set<Medij> medijs = new HashSet<Medij>(0);

	public Pozicija() {
	}

	public int getIdpozicija() {
		return this.idpozicija;
	}

	private void setIdpozicija(int idpozicija) {
		this.idpozicija = idpozicija;
	}

	public String getPozicija() {
		return this.pozicija;
	}

	public void setPozicija(String pozicija) {
		this.pozicija = pozicija;
	}

	public Set<Medij> getMedijs() {
		return this.medijs;
	}

	public void setMedijs(Set<Medij> medijs) {
		this.medijs = medijs;
	}
	
	public String toString() {
		return getPozicija();
	}
	
	public void addMedij(Medij m) {
		medijs.add(m);
		m.setPozicija(this);
	}


}
