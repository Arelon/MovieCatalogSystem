package net.milanaleksic.mcs.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="ZANR", schema="DB2ADMIN")
@Cacheable
@org.hibernate.annotations.Cache(region="mcs",
        usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Zanr implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="IDZANR")
	private int idzanr;

    @Column(name="ZANR", length = 100, nullable = false)
	private String zanr;

    @OneToMany(mappedBy = "zanr")
	private Set<Film> films = new HashSet<Film>(0);

	public Zanr() {
	}

	public int getIdzanr() {
		return this.idzanr;
	}

	private void setIdzanr(int idzanr) {
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
