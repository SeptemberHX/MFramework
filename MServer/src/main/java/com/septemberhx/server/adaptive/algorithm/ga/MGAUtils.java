package com.septemberhx.server.adaptive.algorithm.ga;

import java.util.Random;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/14
 */
public class MGAUtils {
    public static Random CROSSOVER_POINT_RAND = new Random(1000000);
    public static Random CROSSOVER_PROB_RAND = new Random(3000000);
    public static Random MUTATION_PROB_RAND = new Random(4000000);
    public static Random MUTATION_SELECT_RAND = new Random(5000000);
}
