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

    public void calcNSGAIIFitness() {
        for (MChromosome chromosome : populace) {
            chromosome.calcNSGAIIFitness();
        }
    }

    public void calcWSGAFitness() {
        for (MChromosome chromosome : populace) {
            chromosome.calcWSGAFitness();
        }
    }
}
