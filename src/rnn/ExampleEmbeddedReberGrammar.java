package rnn;

import rnn.datasets.EmbeddedReberGrammar;
import rnn.datastructs.DataSet;
import rnn.model.Model;
import rnn.trainer.Trainer;
import rnn.util.NeuralNetworkHelper;

import java.util.Random;

public class ExampleEmbeddedReberGrammar {
	public static void main(String[] args) throws Exception {

		Random rng = new Random();
		
		DataSet data = new EmbeddedReberGrammar(rng);
		
		int hiddenDimension = 12;
		int hiddenLayers = 1;
		double learningRate = 0.001;
		double initParamsStdDev = 0.08;

		Model nn = NeuralNetworkHelper.makeLstm( 
				data.inputDimension,
				hiddenDimension, hiddenLayers, 
				data.outputDimension, data.getModelOutputUnitToUse(), 
				initParamsStdDev, rng);
		
		int reportEveryNthEpoch = 10;
		int trainingEpochs = 1000;
		
		Trainer.train(trainingEpochs, learningRate, nn, data, reportEveryNthEpoch, rng);
		
		System.out.println("done.");
	}
}
