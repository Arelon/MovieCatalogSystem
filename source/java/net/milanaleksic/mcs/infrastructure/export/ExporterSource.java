package net.milanaleksic.mcs.infrastructure.export;

/**
 * @author Milan Aleksic
 * 09.03.2008.
 */
public interface ExporterSource {
	
	/*
	 * Vrati ime fajla u koji ce biti smeseteni podaci za eksportovanje
	 */
	public String getTargetFile() ;
	
	/*
	 * Vrati broj zapisa u podacima
	 */
	public int getItemCount() ;
	
	/*
	 * Vrati broj kolona u podacima
	 */
	public int getColumnCount() ;
	
	/*
	 * Ako je indeks == -1 onda se trazi dovlacenje imena kolona, a ne podataka u tabeli
	 */
	public String getData(int row, int column) ;

}
