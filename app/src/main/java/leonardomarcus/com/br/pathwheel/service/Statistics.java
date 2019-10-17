package leonardomarcus.com.br.pathwheel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class Statistics {

    public static double mean(List<Double> values) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        double sum = 0;
        for(double value : values)
            sum+=value;
        return values.size() != 0 ? sum/(double)values.size() : 0;
    }

    public static double median(List<Double> values) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        List<Double> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues, new Comparator<Double>() {
            @Override
            public int compare(Double a, Double b) {
                if(a == b)
                    return 0;
                else if (a < b)
                    return -1;
                else
                    return 1;
            }
        });
        if(sortedValues.size() == 1)
            return sortedValues.get(0);
        if(sortedValues.size()%2 == 0)
            return (sortedValues.get((sortedValues.size()-1)/2)+sortedValues.get(((sortedValues.size()-1)/2)+1))/2d;
        else
            return sortedValues.get((sortedValues.size()-1)/2);

    }

    public static double medianAbsoluteDeviation(List<Double> values) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        double median = median(values);
        List<Double> seriesMedian = new ArrayList<>(values);
        for(int i=0; i<seriesMedian.size();i++)
            seriesMedian.set(i, Math.abs(seriesMedian.get(i)-median));
        return median(seriesMedian);

    }

    public static double meanRemovingOutliersUsingMedianAbsoluteDeviation(List<Double> values, double threshold) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        double mean = 0;
        double median = median(values);
        double medianAbsoluteDeviation = medianAbsoluteDeviation(values);
        int count = 0;
        for(Double a : values) {
            if(a > (median - threshold*medianAbsoluteDeviation) && a < (median + threshold*medianAbsoluteDeviation)) {
                mean+=a;
                count++;
            }
        }
        mean = mean/(double)count;
        //Log.d("testtt", "samples: "+values.size()+ " mean: " + mean(values) + " median: "+median+" medianAbsoluteDeviation: "+medianAbsoluteDeviation+" new mean:"+mean);
        return mean;
    }

    public static List<Double> getOutliersUsingMedianAbsoluteDeviation(List<Double> values, double threshold) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        List<Double> outliers = new ArrayList<Double>();
        double median = median(values);
        double medianAbsoluteDeviation = medianAbsoluteDeviation(values);
        for(Double a : values) {
            if(a <= (median - threshold*medianAbsoluteDeviation) || a >= (median + threshold*medianAbsoluteDeviation))
                outliers.add(a);
        }
        return outliers;
    }

    public static List<Double> getOutliersUsingMaxMedianAbsoluteDeviation(List<Double> values, double threshold) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        List<Double> outliers = new ArrayList<Double>();
        double median = median(values);
        double medianAbsoluteDeviation = medianAbsoluteDeviation(values);
        for(Double a : values) {
            if(a >= (median + threshold*medianAbsoluteDeviation))
                outliers.add(a);
        }
        return outliers;
    }

    public static double variance(List<Double> values) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        double average = mean(values);
        double variance = 0;
        for (double a : values)
            variance = variance + Math.pow((a - average), 2);
        return variance;
    }

    public static double sampleStandardDeviation(List<Double> values) {
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        return (values.size()-1) != 0 ? Math.sqrt(variance(values) / (double)(values.size()-1)) : 0;
    }

    //https://machinelearningmastery.com/how-to-use-statistics-to-identify-outliers-in-data/
    public static double meanRemovingOutliersUsingSampleStandardDeviation(List<Double> values, double zStandardDeviations) {
		/*
		 If a data distribution is approximately normal then about 68 percent of the data values are within one standard
		 deviation of the mean (mathematically, μ ± σ, where μ is the arithmetic mean), about 95 percent are within
		 two standard deviations (μ ± 2σ), and about 99.7 percent lie within three standard deviations (μ ± 3σ).
		 This is known as the 68-95-99.7 rule, or the empirical rule.
		 */
        if(values == null || values.isEmpty())
            throw new NoSuchElementException();
        double mean = mean(values);
        double standardDeviation = sampleStandardDeviation(values);
        double newMean = 0;
        int count = 0;
        for (double a : values) {
            if (a > (mean - (zStandardDeviations*standardDeviation)) && a < (mean + (zStandardDeviations*standardDeviation))) {
                newMean += a;
                count++;
            }
        }
        return newMean/(double)count;
    }

}