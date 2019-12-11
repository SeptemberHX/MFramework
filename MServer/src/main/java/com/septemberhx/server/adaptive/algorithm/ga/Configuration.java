package com.septemberhx.server.adaptive.algorithm.ga;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/14
 */
public class Configuration {

    public static boolean DEBUG_MODE = false;
    public static boolean VERIFY_EVERY_CHILD = false;
    public static boolean COMPOSITION_ALL_ENABLED = true;

    public static int POPULATION_SIZE = 100;
    public static int MOEAD_NEIGHBOR_SIZE = 20;

    public static int WSGA_MAX_ROUND = 300;
    public static int NSGAII_MAX_ROUND = 300;
    public static int MOEAD_MAX_ROUND = 300;

    public static double WSGA_MUTATION_RATE = 0.2;
    public static double NSGAII_MUTATION_RATE = 0.2;
    public static double MOEAD_MUTATION_RATE = 0.2;
}
