package rnn.datasets;

import rnn.datastructs.DataSequence;
import rnn.datastructs.DataSet;
import rnn.datastructs.DataStep;
import rnn.loss.LossMultiDimensionalBinary;
import rnn.loss.LossSumOfSquares;
import rnn.model.Model;
import rnn.model.Nonlinearity;
import rnn.model.SigmoidUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmbeddedReberGrammar extends DataSet {
	
	static public class State {
		public State(Transition[] transitions) {
			this.transitions = transitions;
		}
		public Transition[] transitions;
	}
	
	static public class Transition {
		public Transition(int next_state_id, int token) {
			this.next_state_id = next_state_id;
			this.token = token;
		}
		public int next_state_id;
		public int token;
	}
	
	public EmbeddedReberGrammar(Random r) throws Exception {
		int total_sequences = 1000;
		inputDimension = 7;
		outputDimension = 7;
		lossTraining = new LossSumOfSquares();
		lossReporting = new LossMultiDimensionalBinary();
		training = generateSequences(r, total_sequences);
		validation = generateSequences(r, total_sequences);
		testing = generateSequences(r, total_sequences);
	}
	
	public static List<DataSequence> generateSequences(Random r, int sequences) {
		
		List<DataSequence> result = new ArrayList<>();
		
		final int B = 0;
		final int T = 1;
		final int P = 2;
		final int S = 3;
		final int X = 4;
		final int V = 5;
		final int E = 6;
		
		State[] states = new State[19];
		states[0] = new State(new Transition[] {new Transition(1,B)});
		states[1] = new State(new Transition[] {new Transition(2,T), new Transition(11,P)});
		states[2] = new State(new Transition[] {new Transition(3,B)});
		states[3] = new State(new Transition[] {new Transition(4,T), new Transition(9,P)});
		states[4] = new State(new Transition[] {new Transition(4,S), new Transition(5,X)});
		states[5] = new State(new Transition[] {new Transition(6,S), new Transition(9,X)});
		states[6] = new State(new Transition[] {new Transition(7,E)});
		states[7] = new State(new Transition[] {new Transition(8,T)});
		states[8] = new State(new Transition[] {new Transition(0,E)});
		states[9] = new State(new Transition[] {new Transition(9,T), new Transition(10,V)});
		states[10] = new State(new Transition[] {new Transition(5,P), new Transition(6,V)});
		states[11] = new State(new Transition[] {new Transition(12,B)});
		states[12] = new State(new Transition[] {new Transition(13,T), new Transition(17,P)});
		states[13] = new State(new Transition[] {new Transition(13,S), new Transition(14,X)});
		states[14] = new State(new Transition[] {new Transition(15,S), new Transition(17,X)});
		states[15] = new State(new Transition[] {new Transition(16,E)});
		states[16] = new State(new Transition[] {new Transition(8,P)});
		states[17] = new State(new Transition[] {new Transition(17,T), new Transition(18,V)});
		states[18] = new State(new Transition[] {new Transition(14,P), new Transition(15,V)});
		
		for (int sequence = 0; sequence < sequences; sequence++) {
			List<DataStep> steps = new ArrayList<>();
            int state_id = 0;
			while (true) {
				int transition = -1;
				if (states[state_id].transitions.length == 1) {
					transition = 0;
				}
				else if (states[state_id].transitions.length == 2) {
					transition = r.nextInt(2);
				}
				double[] observation = null;
				
				observation = new double[7];
				observation[states[state_id].transitions[transition].token] = 1.0;
				
				state_id = states[state_id].transitions[transition].next_state_id;
				if (state_id == 0) { //exit at end of sequence
					break;
				}
				double[] target_output = new double[7];
				for (int i = 0; i < states[state_id].transitions.length; i++) {
					target_output[states[state_id].transitions[i].token] = 1.0;
				}
				steps.add(new DataStep(observation, target_output));
			}
			result.add(new DataSequence(steps));
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
