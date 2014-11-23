package be.bagofwords.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NumUtilsTest {

    @Test
    public void testGetBorders() throws Exception {

        List<Double> counts = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            if (i < 990) {
                counts.add(1.0);
            } else {
                counts.add((double) i);
            }
        }

        double[] borders = NumUtils.getBorders(10, counts);

        Assert.assertEquals(9, borders.length);
        Assert.assertEquals(0, NumUtils.getBin(borders, 1));
        Assert.assertEquals(1, NumUtils.getBin(borders, 990));
        Assert.assertEquals(9, NumUtils.getBin(borders, 999));
    }

    @Test
    public void testSumLogProbs() throws Exception {
        double prob1 = Math.log(0.5);
        double prob2 = Math.log(0.5);
        Assert.assertEquals(1, Math.exp(NumUtils.sumLogProbs(prob1, prob2)), 0.01);

        prob1 = Math.log(1e-89);
        prob2 = Math.log(1e-50);
        Assert.assertEquals(prob2, NumUtils.sumLogProbs(prob1, prob2), 1e49);
    }

    @Test
    public void testSumLogProbsArray() throws Exception {
        double prob1 = Math.log(0.5);
        double prob2 = Math.log(0.25);
        double prob3 = Math.log(0.25);
        double[] probs = new double[]{prob1, prob2, prob3};
        Assert.assertEquals(1, Math.exp(NumUtils.sumLogProbs(probs)), 0.01);

        prob1 = Math.log(1e-89);
        prob2 = Math.log(1e-50);
        prob3 = Math.log(1e-73);
        probs = new double[]{prob1, prob2, prob3};
        Assert.assertEquals(prob2, NumUtils.sumLogProbs(probs), 1e49);
    }

}