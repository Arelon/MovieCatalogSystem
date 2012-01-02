package net.milanaleksic.mcs.infrastructure.util;

public class AnyThrow {

    public static void throwUncheked(Throwable e) {
        AnyThrow.<RuntimeException> throwAny(e);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAny(Throwable e) throws E {
        throw (E) e;
    }
}

