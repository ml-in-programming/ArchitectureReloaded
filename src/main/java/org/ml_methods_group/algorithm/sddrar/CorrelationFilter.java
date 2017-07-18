package org.ml_methods_group.algorithm.sddrar;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import java.util.ArrayList;
import java.util.List;

public class CorrelationFilter {
    public static void filterByFeatureCorrelationRate(DataSet dataSet) {

        double[] avg = getAvgAbsoluteCorrelation(dataSet.getMatrix());
        double mean = new Mean().evaluate(avg);
        double stddev = new StandardDeviation().evaluate(avg);

        List<Integer> toStay = new ArrayList<>();

        for (int i = 0; i < dataSet.getMatrix().getColumnDimension(); i++) {
            if (Math.abs(avg[i] - mean) <= stddev) {
                toStay.add(i);
            }
        }

        double[][] newData = new double[toStay.size()][dataSet.getMatrix().getRowDimension()];
        List<String> newFeatureNames = new ArrayList<>();

        int j = 0;
        for (Integer i : toStay) {
            newData[j++] = dataSet.getMatrix().getColumn(i);
            newFeatureNames.add(dataSet.getFeatureNames().get(i));
        }

        dataSet.setData(MatrixUtils.createRealMatrix(newData).transpose());
        dataSet.setFeatureNames(newFeatureNames);
    };

    private static double[] getAvgAbsoluteCorrelation(RealMatrix dataSet) {
        RealMatrix correlations = new PearsonsCorrelation().computeCorrelationMatrix(dataSet);
        correlations = filterNans(correlations);
        assert correlations.getColumnDimension() == correlations.getRowDimension();
        assert correlations.getRowDimension() == dataSet.getColumnDimension();


        double[] avg = new double[correlations.getRowDimension()];
        for (int i = 0; i < correlations.getRowDimension(); i++) {
            double[] row = correlations.getData()[i];
            double[] absRow = new double[row.length];
            for (int j = 0; j < row.length; j++) {
                absRow[j] = Math.abs(row[j]);
            }
            avg[i] = (new Sum().evaluate(absRow) - 1) / (correlations.getRowDimension() - 1);
        }
        return avg;
    }

    private static RealMatrix filterNans(RealMatrix matrix) {
        double[][] data = matrix.getData();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = Double.isNaN(data[i][j]) ? 0 : data[i][j];
            }
        }

        return MatrixUtils.createRealMatrix(data);
    }
}
