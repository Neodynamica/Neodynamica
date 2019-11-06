package com.neodynamica.userinterface.gui;

import com.neodynamica.backendinterface.GenerationBean;

import java.io.Serializable;

public class FormulaData implements Serializable {

    private boolean exception;
    private String name;
    private String dataset;
    private int generations;
    private int nodes;
    private int population;
    private int depth;
    private String skeleton;
    private String errorFunc;
    private double error;
    private double target;
    private GenerationBean bean;

    public FormulaData() {
        exception = false;
        error = 0;
        target = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }


    public int getGenerations() {
        return generations;
    }

    public void setGenerations(int generations) {
        this.generations = generations;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public String getSkeleton() {
        return skeleton;
    }

    public void setSkeleton(String skeleton) {
        this.skeleton = skeleton;
    }

    public String getErrorFunction() {
        return errorFunc;
    }

    public void setErrorFunction(String errorFunc) {
        this.errorFunc = errorFunc;
    }

    public boolean isException() {
        return exception;
    }

    public void setException(boolean exception) {
        this.exception = exception;
    }

    public GenerationBean getBean() {
        return bean;
    }

    public void setBean(GenerationBean bean) {
        this.bean = bean;
    }
}
