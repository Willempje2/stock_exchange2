package stock_exchange;

import java.util.Arrays;

import org.encog.engine.network.activation.ActivationElliott;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.layers.Layer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;


public class stock_prediction {
	public static void main(String[] args) throws Exception{
		
		int max_stock_price = 60;
		int length_trainingset = 15;
		String stock_key = "RAND.AS";
		
		get_stock_data stock_data = new get_stock_data(stock_key, "2015-11-10", "2016-11-14", length_trainingset, max_stock_price);
		
		//make training sets and ideal awnser sets
		double[][] input_list = new double[stock_data.get_num_trainingsets()][];
		double[][] ideal_list = new double[stock_data.get_num_trainingsets()][];
		
		for(int i = 0; i < stock_data.get_num_trainingsets(); i++){
		input_list[i] = stock_data.get_trainingset(i);
		ideal_list[i] = stock_data.get_ideal_trainingset(i);
		}
		
		
		MLDataSet trainingSet = new BasicMLDataSet(input_list, ideal_list);
		BasicNetwork network = new BasicNetwork();
		
		network.addLayer((Layer)new BasicLayer(new ActivationSigmoid(), true, (length_trainingset-1)*4	));
		network.addLayer((Layer)new BasicLayer(new ActivationSigmoid(), true, (length_trainingset-1)	));
		//network.addLayer((Layer)new BasicLayer(new ActivationSigmoid(), true, 8		));
		network.addLayer((Layer)new BasicLayer(new ActivationSigmoid(), true, 2		));
		network.getStructure().finalizeStructure();
		network.reset();
		
		// Strart training
		final Train train = new ResilientPropagation(network, trainingSet);
		int epoch = 1;
		for (int i=0;i<200;i++) {

		  train.iteration();
		  System.out.println("Epoch #" + epoch + 
		                     " Error:" + train.getError());
		  epoch++;

		}
		train.finishTraining();
		System.out.println("Finish training");
		
		// Calculate new results
		final MLData output = network.compute(new BasicMLData(stock_data.get_latest_stock_values()));
		System.out.println("Stock key:	" + stock_key);
		System.out.println("close value:	" + output.getData(0)*max_stock_price);
		System.out.println("Open value:	" + output.getData(1)*max_stock_price);
		
		
		
	}
	
}
