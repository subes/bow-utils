package be.bagofwords.util;

import be.bagofwords.counts.BinComputer;
import be.bagofwords.text.MappedText;
import org.junit.Assert;
import org.junit.Test;

public class TestStringUtils {
    @Test
    public void testGetPartition() {
        BinComputer bc = new BinComputer(1000);
        int numOfPartitions = 10;
        for (int i = 'a'; i <= 'z'; i++) {
            String word = Character.toString(Character.toChars(i)[0]);
            bc.addCount(StringUtils.getPartition(word, numOfPartitions));
        }
        double[] bins = bc.getEquiDenseBins(numOfPartitions);
        Assert.assertEquals(9, bins.length); //10 partitions
        int[] counts = bc.getBinCounts(bins);
        double averageCount = 0;
        for (int i = 1; i < numOfPartitions; i++) {
            averageCount += counts[i];
        }
        averageCount /= numOfPartitions - 1;
        for (int i = 1; i < numOfPartitions; i++) {
            Assert.assertTrue(averageCount - 1 < counts[i] && averageCount + 1 > counts[i]);
        }
    }

    @Test
    public void testEscapeFileName() {
        String orig1 = "file://home/koen/f/";
        String escape1 = "file:_+_+home_+koen_+f_+";
        Assert.assertEquals(escape1, StringUtils.escapeFileName(orig1));
        Assert.assertEquals(orig1, StringUtils.unescapeFileName(escape1));

        String orig2 = "\\home_home\\koen\\f\\";
        String escape2 = "_-home__home_-koen_-f_-";
        Assert.assertEquals(escape2, StringUtils.escapeFileName(orig2));
        Assert.assertEquals(orig2, StringUtils.unescapeFileName(escape2));
    }

    @Test
    public void testIsPossibleName() {
        Assert.assertTrue(StringUtils.isPossibleName("Koen Deschacht"));
        Assert.assertTrue(StringUtils.isPossibleName("121 Koen Deschacht"));
        Assert.assertTrue(StringUtils.isPossibleName("121 Koen 212"));
        Assert.assertTrue(StringUtils.isPossibleName("121 D Koen 212"));
        Assert.assertFalse(StringUtils.isPossibleName("121"));
        Assert.assertFalse(StringUtils.isPossibleName("een"));
        Assert.assertFalse(StringUtils.isPossibleName("zegt Koen"));
        Assert.assertFalse(StringUtils.isPossibleName("Koen zegt"));
    }

    @Test
    public void testReduceCharacterDiversity() {
        Assert.assertEquals("utoya", StringUtils.reduceCharacterDiversityLevel2("Utøya"));
    }

    @Test
    public void testReplaceAll() throws Exception {
        String startString = "hoihoidaag";
        MappedText mappedText = new MappedText(startString.toCharArray(), "test");
        StringUtils.replaceAll("daag", "bye", mappedText);
        Assert.assertEquals("hoihoibye", mappedText.getS());
    }

    @Test
    public void testReplaceAllRegex2() throws Exception {
        String startString = "hoihoidaag";
        MappedText mappedText = new MappedText(startString.toCharArray(), "test");
        StringUtils.replaceAll("^", "test", mappedText);
        Assert.assertEquals("testhoihoidaag", mappedText.getS());
    }

    @Test
    public void testReplaceAllRegex3() throws Exception {
        String origText = "Ulstrup is een plaats in de Deense regio Midden-Jutland, gemeente Favrskov, en telt 2013 inwoners (2007).\n" +
                "\n" +
                "Het Ulstrup Slot van Christiaan IV van Denemarken bevindt zich hier.";
        MappedText mappedText = new MappedText(origText.toCharArray(), "test");
        StringUtils.replaceAll("^", " ", mappedText);
        Assert.assertEquals(origText, mappedText.getText().trim());
    }

    @Test
    public void testReplaceAllRegex4() throws Exception {
        String startString = "hoihoidaag";
        MappedText mappedText = new MappedText(startString.toCharArray(), "test");
        StringUtils.replaceAll("^hoi", "test", mappedText);
        Assert.assertEquals("testhoidaag", mappedText.getS());
    }

    @Test
    public void testReplaceAllRegex5() throws Exception {
        String startString = "hoihoidaag";
        MappedText mappedText = new MappedText(startString.toCharArray(), "test");
        StringUtils.replaceAll("daag$", "test", mappedText);
        Assert.assertEquals("hoihoitest", mappedText.getS());
    }

    @Test
    public void testReplaceAllRegex6() throws Exception {
        String startString = "hoihoidaag";
        MappedText mappedText = new MappedText(startString.toCharArray(), "test");
        StringUtils.replaceAll("ih.{2}", "test", mappedText);
        Assert.assertEquals("hotestdaag", mappedText.getS());
    }

}