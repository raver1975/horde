package rnn;

import rnn.datasets.bAbI;
import rnn.datastructs.DataSet;
import rnn.model.Model;
import rnn.trainer.Trainer;
import rnn.util.NeuralNetworkHelper;

import java.util.Random;

public class ExampleQuestionAnswering {
	public static void main(String[] args) throws Exception {
		
		/*
		EXAMPLE OF LSTM RESULTS:
			47.0% avg. accuracy on #1: Single Supporting Fact
			32.7% avg. accuracy on #2: Two Supporting Facts
			24.0% avg. accuracy on #3: Three Supporting Facts
			58.6% avg. accuracy on #4: Two Arg. Relations
			60.5% avg. accuracy on #5: Three Arg. Relations
			64.1% avg. accuracy on #6: Yes/No Questions
			76.3% avg. accuracy on #7: Counting
			69.9% avg. accuracy on #8: Lists/Sets
			61.2% avg. accuracy on #9: Simple Negation
			52.6% avg. accuracy on #10: Indefinite Knowledge
			67.8% avg. accuracy on #11: Basic Coreference
			64.4% avg. accuracy on #12: Conjunction
			89.6% avg. accuracy on #13: Compound Coreference
			24.2% avg. accuracy on #14: Time Reasoning
			29.5% avg. accuracy on #15: Basic Deduction
			46.2% avg. accuracy on #16: Basic Induction
			52.1% avg. accuracy on #17: Positional Reasoning
			91.2% avg. accuracy on #18: Size Reasoning
			8.0% avg. accuracy on #19: Path Finding
			94.0% avg. accuracy on #20: Agent's Motivations
			
		EXAMPLE OF GRU RESULTS:
			45.1% avg. accuracy on #1: Single Supporting Fact
			28.3% avg. accuracy on #2: Two Supporting Facts
			22.9% avg. accuracy on #3: Three Supporting Facts
			64.0% avg. accuracy on #4: Two Arg. Relations
			51.0% avg. accuracy on #5: Three Arg. Relations
			62.3% avg. accuracy on #6: Yes/No Questions
			72.1% avg. accuracy on #7: Counting
			72.9% avg. accuracy on #8: Lists/Sets
			64.2% avg. accuracy on #9: Simple Negation
			52.5% avg. accuracy on #10: Indefinite Knowledge
			64.1% avg. accuracy on #11: Basic Coreference
			63.2% avg. accuracy on #12: Conjunction
			92.7% avg. accuracy on #13: Compound Coreference
			23.8% avg. accuracy on #14: Time Reasoning
			29.3% avg. accuracy on #15: Basic Deduction
			43.9% avg. accuracy on #16: Basic Induction
			51.0% avg. accuracy on #17: Positional Reasoning
			90.6% avg. accuracy on #18: Size Reasoning
			9.2% avg. accuracy on #19: Path Finding
			93.7% avg. accuracy on #20: Agent's Motivations
		*/
		
		Random rng = new Random();
		int hiddenDimension = 10;
		
		int hiddenLayers = 1;
		double learningRate = 0.005;
		double initParamsStdDev = 0.08;
		int epochsPerTask = 50;
		int experiments = 1;
		
		boolean onlyShowSupportingFacts = false;
		
		double[] losses = new double[bAbI.TASK_NAMES.length];
		
		for (int experiment = 0; experiment < experiments; experiment++) {
			for (int task = 0; task < bAbI.TASK_NAMES.length; task++) {
				
				int setId = task + 1;
				System.out.println("\n==============================================================");
				System.out.println("bAbI experiment "+(experiment+1)+" of "+experiments);
				System.out.println("Task #" + setId + ": "+ bAbI.TASK_NAMES[task]+"\n");
			
				int totalExamples = 1000;
				
				DataSet data = new bAbI(setId, totalExamples, onlyShowSupportingFacts, rng);

				Model nn = NeuralNetworkHelper.makeLstm(
						data.inputDimension,
						hiddenDimension, hiddenLayers, 
						data.outputDimension, data.getModelOutputUnitToUse(), 
						initParamsStdDev, rng);

				/*
				Model nn = NeuralNetworkHelper.makeGru(
						data.inputDimension,
						hiddenDimension, hiddenLayers, 
						data.outputDimension, data.getModelOutputUnitToUse(), 
						initParamsStdDev, rng);
				//*/
				
				int reportEveryNthEpoch = 10;
				double loss = Trainer.train(epochsPerTask, learningRate, nn, data, reportEveryNthEpoch, rng);
				losses[task] += loss;
				System.out.println("\nFINAL: " + String.format("%.1f", (100*(1-loss))) + "% accuracy");
			}
		}
		
		System.out.println("\n\n==============================================================");
		System.out.println("SUMMED RESULTS:");
		for (int task = 0; task < bAbI.TASK_NAMES.length; task++) {
			System.out.println("\t" + String.format("%.1f", (100*(1-(losses[task]/(double)experiments)))) + "% avg. accuracy on #"+(task+1)+": " + bAbI.TASK_NAMES[task]);
		}
	}
}
