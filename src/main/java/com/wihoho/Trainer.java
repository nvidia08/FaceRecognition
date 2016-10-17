package com.wihoho;

import com.google.common.base.Preconditions;
import com.wihoho.constant.FeatureType;
import com.wihoho.jama.Matrix;
import com.wihoho.training.*;
import lombok.experimental.Builder;

import java.util.ArrayList;
import java.util.Objects;

@Builder
public class Trainer {
    Metric metric;
    FeatureType featureType;
    FeatureExtraction featureExtraction;
    int numberOfComponents;
    int k; // k specifies the number of neighbour to consider

    ArrayList<Matrix> trainingSet;
    ArrayList<String> trainingLabels;

    ArrayList<ProjectedTrainingMatrix> model;

    public void add(Matrix matrix, String label) {
        if (Objects.isNull(trainingSet)) {
            trainingSet = new ArrayList<>();
            trainingLabels = new ArrayList<>();
        }

        trainingSet.add(matrix);
        trainingLabels.add(label);
    }

    public void train() throws Exception {
        Preconditions.checkNotNull(metric);
        Preconditions.checkNotNull(featureType);
        Preconditions.checkNotNull(numberOfComponents);
        Preconditions.checkNotNull(trainingSet);
        Preconditions.checkNotNull(trainingLabels);

        switch (featureType) {
            case PCA:
                featureExtraction = new PCA(trainingSet, trainingLabels, numberOfComponents);
                break;
            case LDA:
                featureExtraction = new LDA(trainingSet, trainingLabels, numberOfComponents);
                break;
            case LPP:
                featureExtraction = new LPP(trainingSet, trainingLabels, numberOfComponents);
                break;
        }

        model = featureExtraction.getProjectedTrainingSet();
    }

    public String recognize(Matrix matrix) {
        Matrix testCase = featureExtraction.getW().transpose().times(matrix.minus(featureExtraction.getMeanMatrix()));
        String result = KNN.assignLabel(model.toArray(new ProjectedTrainingMatrix[0]), testCase, k, metric);
        return result;
    }
}
