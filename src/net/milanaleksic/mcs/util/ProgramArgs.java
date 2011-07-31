package net.milanaleksic.mcs.util;

import org.kohsuke.args4j.Option;

/**
 * User: Milan Aleksic
 * Date: 7/31/11
 * Time: 7:49 PM
 */
public class ProgramArgs {

    private boolean noInitialMovieListLoading;

    @Option(name="-n", aliases = "--noInitialMovieListLoading", usage="do not load movie list immediatelly on startup")
    public void setNoInitialMovieListLoading(boolean noInitialMovieListLoading) {
        this.noInitialMovieListLoading = noInitialMovieListLoading;
    }

    public boolean isNoInitialMovieListLoading() {
        return noInitialMovieListLoading;
    }

    @Override
    public String toString() {
        return "ProgramArgs{" +
                "noInitialMovieListLoading=" + noInitialMovieListLoading +
                '}';
    }
}
