package org.ml_methods_group.algorithm.sddrar;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataSet implements Serializable {
    private RealMatrix data;
    private List<String> entityNames;
    private List<String> featureNames;

    public DataSet(RealMatrix data, List<String> entityNames, List<String> featureNames) {
        this.data = data.copy();
        normalizeColumns(this.data);
        this.entityNames = entityNames;
        this.featureNames = featureNames;
    }

    // rows - entities, columns - features
    public static DataSet createaFromData(double[][] data) {
        List<String> entityNames = new ArrayList<>();
        List<String> featureNames = new ArrayList<>();
        final int featuresNum = data[0].length;
        final int entitiesNum = data.length;

        for (int i = 0; i < featuresNum; i++) {
            featureNames.add("feature" + i);
        }

        for (int j = 0; j < entitiesNum; j++) {
            entityNames.add("class" + j);
        }

        RealMatrix matrix = MatrixUtils.createRealMatrix(data);

        return new DataSet(matrix, entityNames, featureNames);
    }

    public RealMatrix getMatrix() {
        return data;
    }

    public void setData(RealMatrix data) {
        this.data = data;
    }

    public List<String> getEntityNames() {
        return entityNames;
    }

    public void setEntityNames(List<String> entityNames) {
        this.entityNames = entityNames;
    }

    public List<String> getFeatureNames() {
        return featureNames;
    }

    public void setFeatureNames(List<String> featureNames) {
        this.featureNames = featureNames;
    }

    private void normalizeColumns(RealMatrix matrix) {
        for (int i = 0; i < matrix.getColumnDimension(); i++) {
            double[] column = matrix.getColumn(i);
            matrix.setColumn(i, normalizeMax(column));
        }
    }

    private double[] normalizeMax(double[] data) {
        double max = new Max().evaluate(data);
        double min = new Min().evaluate(data);
        double[] res = new double[data.length];
        if (max > min) {
            for (int i = 0; i < data.length; i++) {
                res[i] = (data[i] - min) / (max - min);
            }
        } else if (max != 0) {
            for (int i = 0; i < data.length; i++) {
                res[i] = data[i] / max;
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                res[i] = 0;
            }
        }

        return res;
    }
}
