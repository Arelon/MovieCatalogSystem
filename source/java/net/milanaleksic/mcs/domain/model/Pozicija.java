package net.milanaleksic.mcs.domain.model;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import java.util.Set;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings({"HardCodedStringLiteral"})
public class Pozicija implements java.io.Serializable {

    public static final String DEFAULT_POSITION_YES = "Y";
    public static final String DEFAULT_POSITION_NO = "N";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idpozicija;

    @Column(length = 100, nullable = false)
	private String pozicija;

	private boolean defaultPosition;

	@OneToMany(mappedBy = "pozicija", fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
    @org.hibernate.annotations.BatchSize(size=15)
    private Set<Medij> medijs = null;

    public Pozicija() {
	}

	public Pozicija(String naziv) {
        this.pozicija = naziv;
	}

    public Pozicija(String pozicija, boolean isDefault) {
        this.pozicija = pozicija;
        defaultPosition = isDefault;
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

    public void removeMedij(Medij medij) {
		medijs.remove(medij);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pozicija pozicija1 = (Pozicija) o;

        if (pozicija != null ? !pozicija.equals(pozicija1.pozicija) : pozicija1.pozicija != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pozicija != null ? pozicija.hashCode() : 0;
        return result;
    }

    @Basic
    @Column(nullable = false)
    @Access(AccessType.PROPERTY)
    public String getDefaultPosition() {
        return defaultPosition ? DEFAULT_POSITION_YES : DEFAULT_POSITION_NO;
    }

    public void setDefaultPosition(String defaultPosition) {
        this.defaultPosition = defaultPosition != null && DEFAULT_POSITION_YES.equals(defaultPosition);
    }

    public boolean isDefault() {
        return defaultPosition;
    }

    public void setDefault(boolean defaultPosition) {
        this.defaultPosition = defaultPosition;
    }

}
