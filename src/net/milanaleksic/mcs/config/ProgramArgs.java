package net.milanaleksic.mcs.config;

import org.kohsuke.args4j.Option;

/**
 * User: Milan Aleksic
 * Date: 7/31/11
 * Time: 7:49 PM
 */
public class ProgramArgs {

    private boolean noInitialMovieListLoading = false;
    private boolean collectStatistics = false;
    private boolean noRestorationProcessing = false;

    @Option(name="-r", aliases = "--noRestorationProcessing", usage="do not create/restore restore point")
    public void setNoRestorationProcessing(boolean noRestorationProcessing) {
        this.noRestorationProcessing = noRestorationProcessing;
    }

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

    public boolean isNoRestorationProcessing() {
        return noRestorationProcessing;
    }

    @Override
    public String toString() {
        return "ProgramArgs{" +
                "noInitialMovieListLoading=" + noInitialMovieListLoading +
                ", collectStatistics=" + collectStatistics +
                ", noRestorationProcessing=" + noRestorationProcessing +
                '}';
    }
}
