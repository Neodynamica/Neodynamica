package com.neodynamica.backendinterface;

import io.jenetics.Phenotype;
import io.jenetics.ext.moea.ParetoFront;
import io.jenetics.ext.moea.Vec;
import io.jenetics.ext.util.TreeFormatter;
import io.jenetics.prog.ProgramGene;
import io.jenetics.prog.op.MathExpr;
import io.jenetics.util.ISeq;

import java.io.Serializable;

public class GenerationBean<T> implements Serializable {

    private long index;
    private ISeq<Phenotype<ProgramGene<Double>, Vec<double[]>>> population;
    private T bestFitness;
    private T medianFitness;
    private T averageFitness;
    private T worstFitness;
    private ProgramGene<Double> bestGene;
    private T fitnessStandardDeviation;
    private boolean newBest = false; //true if there's a new best solution this generation
    private String[] inputVariableLabels;

    public GenerationBean() {
    }

    public String getBestSolutionParenthesesString() {
        return new MathExpr(bestGene).simplify().toString();
    }

    /**
     * Returns the bestSolution of this generation in Jenetics Tree format
     *
     * @return A multiline String of the Tree representation of this generation's best solution
     */
    public String getBestSolutionTree() {
        return TreeFormatter.TREE.format(bestGene);
    }

    /**
     * Returns the simplified bestSolution of this generation in Jenetics Tree format
     * @return A multiline String of the Tree representation of this generation's best solution
     */
    public String getBestSimplifiedSolutionTree() {
        return TreeFormatter.TREE.format(new MathExpr(bestGene).simplify().toTree());
    }

    /**
     * Takes in a row of input variable values and feeds them through the best formula of this
     * generation to return the resulting prediction of the output value for this row. Must specify
     * values for ALL inputs variables considered in the search, even if they don't appear in this
     * particular formula.
     * <p>
     * e.g. In a search for 'e' with input variables a,b,c,d: best formula for this generation is
     * <p><code>predictWithBestFormula(1, 2, 3, 4) = MUL(1, ADD(2, 4)) = 8.0</code>MUL(a, ADD(b, d)) e.g.
     * predictWithBestFormula
     *
     * @param inputRow - a varargs or array of Doubles which correspond to a complete set of inputs
     * to the solution
     */
    public Double predictWithBestSolution(Double... inputRow) {
        return getBestGene().eval(inputRow);
    }

    /* Generic getters and setters */
    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public T getBestFitness() {
        return bestFitness;
    }

    public void setBestFitness(T bestFitness) {
        this.bestFitness = bestFitness;
    }

    public T getMedianFitness() {
        return medianFitness;
    }

    public void setMedianFitness(T medianFitness) {
        this.medianFitness = medianFitness;
    }

    public T getAverageFitness() {
        return averageFitness;
    }

    public void setAverageFitness(T averageFitness) {
        this.averageFitness = averageFitness;
    }

    public T getWorstFitness() {
        return worstFitness;
    }

    public void setWorstFitness(T worstFitness) {
        this.worstFitness = worstFitness;
    }

    public T getFitnessStandardDeviation() {
        return fitnessStandardDeviation;
    }

    public void setFitnessStandardDeviation(T fitnessStandardDeviation) {
        this.fitnessStandardDeviation = fitnessStandardDeviation;
    }

    public ISeq getPopulation() {
        return population;
    }

    public void setPopulation(ISeq population) {
        this.population = population;
    }

    public boolean isNewBest() {
        return newBest;
    }

    public void setNewBest(boolean newBest) {
        this.newBest = newBest;
    }

    public ProgramGene<Double> getBestGene() {
        return bestGene;
    }

    public void setBestGene(ProgramGene<Double> bestGene) {
        this.bestGene = bestGene;
    }

    public String[] getInputVariableLabels() {
        return inputVariableLabels;
    }

    public void setInputVariableLabels(String[] inputVariableLabels) {
        this.inputVariableLabels = inputVariableLabels;
    }

    public ParetoFront<Vec<double[]>> getParetoFront() {
        // @TODO: handle error if population type is different

        final ParetoFront<Vec<double[]>> paretoFront = new ParetoFront<>(Vec::dominance);
        this.population.forEach(p -> paretoFront.add(p.getFitness()));

        return paretoFront;
    }

    //TODO - Maybe add in the parameters if they're ever changed by backend stuff
}
