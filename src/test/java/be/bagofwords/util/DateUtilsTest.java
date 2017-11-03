package be.bagofwords.util;

import org.junit.Test;

public class DateUtilsTest {

    @Test
    public void createDate() throws Exception {
        DateUtils.createDate(2018, 2, 1);//This threw a rather strange exception possibly cause by incorrect handling of timezones in java.util.Calendar?
    }

}