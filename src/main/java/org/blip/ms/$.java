package org.blip.ms;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;

/**
 * Copy of basically underscore type libraries and convience functions.
 *
 */
public class $ {

    /**
     * Takes the first non null value.
     *
     * @param args
     *            variable number of string values to coalesce
     * @return The first non-null value.
     */
    public static String coalesce(String... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && StringUtils.isNotBlank(args[i])) {
                return args[i];
            }
        }
        return null;
    }

    /**
     * Attempts a thread sleep, useful for tests and to avoid
     * capturing the exception.
     *
     * @param ms The time to sleep in milliseconds
     */
    public static void trySleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Returns promise/deferred object for cross thread notification and async processing.
     */
    public static <D, F, P> DeferredObject<D, F, P> promise() {
        return new Async<>();
    }

    /**
     * Fixes issue with deferred library where notify was being performed on a synchronized(this) call
     * slowing performance needlessly.
     */
    private static class Async<D, F, P> extends DeferredObject<D, F, P> {
        /**
         * {@inheritDoc}
         */
        @Override
        public Deferred<D, F, P> notify(final P progress) {
            triggerProgress(progress);
            return this;
        }
    }

}