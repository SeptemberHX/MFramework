package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 */
public class MPopulation {

    private List<MChromosome> chromosomes = new ArrayList<>();
    private List<String> nodeIdList = new ArrayList<>();
    private List<String> serviceIdList = new ArrayList<>();

    public static Random CROSSOVER_POINT_RAND = new Random(1000000);
    public static Random MUTATION_POINT_RAND = new Random(2000000);
    public static Random CROSSOVER_RATE_RAND = new Random(3000000);
    public static Random MUTATION_RATE_RAND = new Random(4000000);
    public static Random MUTATION_SELECT_RAND = new Random(5000000);

    public MPopulation(MServerOperator snapshotOperator) {
        MSystemModel.getIns().getMSNManager().getAllValues().forEach(node -> nodeIdList.add(node.getId()));
        snapshotOperator.getAllServices().forEach(service -> serviceIdList.add(service.getId()));
    }
}
