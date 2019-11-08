package com.septemberhx.server.adaptive.algorithm.ga;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Vector;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/14
 */
public class MPopulation {
    @Getter
    @Setter
    List<MChromosome> populace;

    public MPopulation(final List<MChromosome> populace) {
        this.populace = populace;
    }

    public MPopulation() {
        this(new Vector<>());  // thread-safe
    }

    // normalize the cost and avgTime to [0, 1]
    public void normalizeObjectValues() {
        normalizeObjectValues(this.populace);
    }

    public static void normalizeObjectValues(List<MChromosome> chromosomeList) {
        double maxValue1 = MBaseGA.maxValue1;
        double minValue1 = MBaseGA.minValue1;
        double maxValue2 = MBaseGA.maxValue2;
        double minValue2 = MBaseGA.minValue2;

        for (MChromosome chromosome : chromosomeList) {
            List<Double> objValues = chromosome.getObjectiveValues();
            double normValue1 = (objValues.get(0) - minValue1) / (maxValue1 - minValue1);
            double normValue2 = (objValues.get(1) - minValue2) / (maxValue2 - minValue2);
            chromosome.setNormObjectiveValues(normValue1, normValue2);
            if (Configuration.DEBUG_MODE) {
                System.out.println(String.format("%f, %f -> %f, %f | %f", objValues.get(0), objValues.get(1), normValue1, normValue2, chromosome.getNormWSGAFitness()));
            }
        }
    }
}
