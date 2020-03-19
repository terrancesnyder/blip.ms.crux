package org.blip.ms;

import org.jdeferred.impl.DeferredObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.awaitility.Awaitility.*;
import static org.fest.assertions.api.Assertions.*;

public class DollarTest {

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
    public void tryLong() {
        assertThat($.tryLong("1")).isEqualTo(1L);
        assertThat($.tryLong("123")).isEqualTo(123L);
        assertThat($.tryLong("0.1")).isNull();

        assertThat($.tryLong(" ")).isNull();
        assertThat($.tryLong("x")).isNull();
        assertThat($.tryLong("      ")).isNull();
        assertThat($.tryLong(null)).isNull();
    }

    @Test
    public void hostname() {
        assertThat($.getHostname()).isNotEmpty();
    }

    @Test
    public void random() {
        assertThat($.randomNumber(0,100)).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(100);
    }

    @Test
    public void we_can_encode_uri() {
        String japanese = "ありがとございます！";
        assertThat($.encodeURIComponent(japanese)).isNotEqualTo("ありがとございます！");
        assertThat($.decodeURIComponent($.encodeURIComponent(japanese))).isEqualTo("ありがとございます！");
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

    @Test
    public void gzip() throws IOException {
        String expected = "test á ありがとございます！ 1234";
        byte[] gzip = $.gzip(expected);
        String actual = $.gunzip(gzip);

        assertThat(actual).isEqualTo(expected);

        expected = null;
        assertThat($.gzip(expected)).isNull();
        gzip = null;
        assertThat($.gzip(gzip)).isNull();
    }
}
