package org.blip.ms;

import org.jdeferred.impl.DeferredObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.awaitility.Awaitility.*;
import static org.fest.assertions.api.Assertions.*;

public class $Test {

    @Test
    public void coalesce() {
         assertThat($.coalesce("a", null)).isEqualTo("a");
        assertThat($.coalesce("a", "")).isEqualTo("a");
        assertThat($.coalesce("a", " ")).isEqualTo("a");
        assertThat($.coalesce("", "a")).isEqualTo("a");
        assertThat($.coalesce(" ", "a")).isEqualTo("a");
        assertThat($.coalesce("     ", "a")).isEqualTo("a");
        assertThat($.coalesce(null, "a")).isEqualTo("a");
    }

    @Test
    public void trySleep() {
        long x = new Date().getTime();
        $.trySleep(100);
        long y = new Date().getTime();
        assertThat(x).isLessThan(y);
        assertThat(x+100).isLessThanOrEqualTo(y);
    }

    @Test
    public void promise() {
        DeferredObject<String, String, String> dfd = $.promise();

        List<String> results = new ArrayList<>();
        dfd.promise().progress((p) -> {
            results.add(p);
        });

        dfd.notify("Hi");
        dfd.notify("Test!");

        assertThat(results.get(0)).isEqualTo("Hi");
        assertThat(results.get(1)).isEqualTo("Test!");
    }
}
