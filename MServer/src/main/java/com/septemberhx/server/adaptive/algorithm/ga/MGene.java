package com.septemberhx.server.adaptive.algorithm.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 */
public class MGene {

    private Integer[] geneIntArr;

    private MGene(Integer[] geneIntArr) {
        this.geneIntArr = geneIntArr;
    }

    public MGene(int serviceSize) {
        this.geneIntArr = new Integer[serviceSize];
        Arrays.fill(this.geneIntArr, 0);
    }

    public Integer[] getGeneIntArr() {
        return geneIntArr;
    }

    public ArrayList<MGene> crossoverTwoPoint(MGene other, int startIndex, int endIndex) {
        Integer[] newArr1 = Arrays.copyOf(this.geneIntArr, this.geneIntArr.length);
        Integer[] newArr2 = Arrays.copyOf(other.geneIntArr, other.geneIntArr.length);

        for (int i = startIndex; i <= endIndex; ++i) {
            newArr1[i] = other.geneIntArr[i];
            newArr2[i] = this.geneIntArr[i];
        }

        ArrayList<MGene> resultList = new ArrayList<>(2);
        resultList.add(new MGene(newArr1));
        resultList.add(new MGene(newArr2));
        return resultList;
    }

    public void addInstance(int serviceIndex) {
        this.geneIntArr[serviceIndex] += 1;
    }

    public boolean deleteInstance(int serviceIndex) {
        if (this.geneIntArr[serviceIndex] == 0) {
            return false;
        }
        this.geneIntArr[serviceIndex] -= 1;
        return true;
    }
}
