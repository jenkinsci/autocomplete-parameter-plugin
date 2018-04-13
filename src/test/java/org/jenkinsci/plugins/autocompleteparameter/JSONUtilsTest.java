package org.jenkinsci.plugins.autocompleteparameter;

import org.junit.Assert;
import org.junit.Test;

public class JSONUtilsTest {

    private String sample = "{" +
            "'page': 1, " +
            "'entries': [ " +
            "   {'name':'Eddard','house':'Stark'}," +
            "   {'name':'Robert','house':'Baratheon'}" +
            " ], " +
            "'inner': [" +
            "   {'data': [{'a': 1}]}, " +
            "   {'data': [{'a': 2}]}" +
            " ] " +
            "}";

    @Test
    public void traverseJson_empty() {
        String array = "[{\"name\":\"Eddard\",\"house\":\"Stark\"},{\"name\":\"Robert\",\"house\":\"Baratheon\"}]";
        String result = JSONUtils.traverseJson(array, "");
        Assert.assertEquals(array, result);
    }

    @Test
    public void traverseJson_root() {
        String result = JSONUtils.traverseJson(sample, "entries");
        Assert.assertEquals("[{\"name\":\"Eddard\",\"house\":\"Stark\"},{\"name\":\"Robert\",\"house\":\"Baratheon\"}]", result);
    }

    @Test
    public void traverseJson_inner_array() {
        String result = JSONUtils.traverseJson(sample, "inner/1/data");
        Assert.assertEquals("[{\"a\":2}]", result);
    }

    @Test
    public void traverseJson_null() {
        String result = JSONUtils.traverseJson(sample, "inner/1/data/0/b");
        Assert.assertEquals("null", result);
    }

}