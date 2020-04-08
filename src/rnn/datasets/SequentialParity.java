package rnn.datasets;

import rnn.datastructs.DataSequence;
import rnn.datastructs.DataSet;
import rnn.datastructs.DataStep;
import rnn.loss.LossMultiDimensionalBinary;
import rnn.loss.LossSumOfSquares;
import rnn.matrix.Matrix;
import rnn.model.Model;
import rnn.model.Nonlinearity;
import rnn.model.SigmoidUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SequentialParity extends DataSet {

	public SequentialParity(Random r, int total_sequences, int max_sequence_length_train, int max_sequence_length_test) {
		inputDimension = 1;
		outputDimension = 1;
		lossTraining = new LossSumOfSquares();
		lossReporting = new LossMultiDimensionalBinary();
		training = generateSequences(r, total_sequences, max_sequence_length_train);
		
		//training.addAll(generateSequences(r, total_sequences, max_sequence_length_test));
		
		validation = generateSequences(r, total_sequences, max_sequence_length_train);
		testing = generateSequences(r, total_sequences, max_sequence_length_test);
	}
	
	private static List<DataSequence> generateSequences(Random r, int total_sequences, int max_sequence_length) {
		List<DataSequence> result = new ArrayList<>();
        for (int s = 0; s < total_sequences; s++) {
			DataSequence sequence = new DataSequence();
			int tot = 0;
			int tempSequenceLength = r.nextInt(max_sequence_length) + 1;
			for (int t = 0; t < tempSequenceLength; t++) {
				DataStep step = new DataStep();
				double[] input = {0.0};
				
				if (r.nextDouble() < 0.5) {
					input[0] = 1.0;
					tot++;
				}
				step.input = new Matrix(input);
				
				double[] targetOutput = null;
				if (t == tempSequenceLength - 1) {
					targetOutput = new double[1];
					targetOutput[0] = tot%2;
					step.targetOutput = new Matrix(targetOutput);
				}
				sequence.steps.add(step);
			}
			result.add(sequence);
		}
		return result;
	}

	@Override
	public void DisplayReport(Model model, Random rng) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public Nonlinearity getModelOutputUnitToUse() {
		return new SigmoidUnit();
	}
}
