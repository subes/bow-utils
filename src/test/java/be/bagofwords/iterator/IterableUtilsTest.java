package be.bagofwords.iterator;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

/**
 * Created by koen on 18/09/15.
 */
public class IterableUtilsTest {

    @Test
    public void testStream() throws Exception {
        List<String> sourceList = Arrays.asList("two", "one", "three");
        DataIterable<String> iterable = IterableUtils.createIterable(sourceList);
        List<String> resultList = IterableUtils.stream(iterable).collect(toList());
        assertEquals(sourceList, resultList);
    }

    @Test
    public void testStream1() throws Exception {
        List<String> sourceList = Arrays.asList("one", "two", "three");
        DataIterable<String> iterable = IterableUtils.createIterable(sourceList);
        List<String> resultList = IterableUtils.stream(iterable, true).collect(toList());
        assertEquals(sourceList, resultList);
    }
}