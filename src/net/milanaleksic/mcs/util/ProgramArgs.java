package net.milanaleksic.mcs.util;

import org.kohsuke.args4j.Option;

/**
 * User: Milan Aleksic
 * Date: 7/31/11
 * Time: 7:49 PM
 */
public class ProgramArgs {

    private boolean noInitialMovieListLoading = false;
    private boolean collectStatistics = false;

    @Option(name="-n", aliases = "--noInitialMovieListLoading", usage="do not load movie list immediately on startup")
    public void setNoInitialMovieListLoading(boolean noInitialMovieListLoading) {
        this.noInitialMovieListLoading = noInitialMovieListLoading;
    }

    @Option(name="-c", aliases = "--collectStatistics", usage="collect Hibernate statistics")
    public void setCollectStatistics(boolean collectStatistics) {
        this.collectStatistics = collectStatistics;
    }

    public boolean isNoInitialMovieListLoading() {
        return noInitialMovieListLoading;
    }

    public boolean isCollectStatistics() {
        return collectStatistics;
    }

    @Override
    public String toString() {
        return "ProgramArgs{" +
                "noInitialMovieListLoading=" + noInitialMovieListLoading +
                ", collectStatistics=" + collectStatistics +
                '}';
    }
}
