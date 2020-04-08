package rnn.datasets;

import rnn.autodiff.Graph;
import rnn.datastructs.DataSequence;
import rnn.datastructs.DataSet;
import rnn.datastructs.DataStep;
import rnn.loss.LossSoftmax;
import rnn.matrix.Matrix;
import rnn.model.LinearUnit;
import rnn.model.Model;
import rnn.model.Nonlinearity;
import rnn.util.Util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;


public class TextGenerationUnbroken extends DataSet {

	private static final long serialVersionUID = 1L;
	public static int reportSequenceLength = 100;
	public static boolean reportPerplexity = true;
	private static Map<String, Integer> charToIndex = new HashMap<>();
	private static Map<Integer, String> indexToChar = new HashMap<>();
	private static int dimension;
	
	public static String generateText(Model model, int steps, boolean argmax, double temperature, Random rng) throws Exception {
		Matrix start = new Matrix(dimension);
		model.resetState();
		Graph g = new Graph(false);
		Matrix input = start.clone();
		String result = "";
		for (int s = 0; s < steps; s++) {
			Matrix logprobs = model.forward(input, g);
			Matrix probs = LossSoftmax.getSoftmaxProbs(logprobs, temperature);
			
			int indxChosen = -1;
			if (argmax) {
				double high = Double.NEGATIVE_INFINITY;
				for (int i = 0; i < probs.w.length; i++) {
					if (probs.w[i] > high) {
						high = probs.w[i];
						indxChosen = i;
					}
				}
			}
			else {
				indxChosen = Util.pickIndexFromRandomVector(probs, rng);
			}
			String ch = indexToChar.get(indxChosen);
			result += ch;
			for (int i = 0; i < input.w.length; i++) {
				input.w[i] = 0;
			}
			input.w[indxChosen] = 1.0;
		}
		result = result.replace("\n", "\"\n\t\"");
		return result;
	}
	
	public TextGenerationUnbroken(String path, int totalSequences, int sequenceMinLength, int sequenceMaxLength, Random rng) throws Exception {
		
		System.out.println("Text generation task");
		System.out.println("loading " + path + "...");
		
		File file = new File(path);
		List<String> lines_ = Files.readAllLines(file.toPath(), Charset.defaultCharset());
		
		String text = "";
		for (String line : lines_) {
			text += line + "\n";
		}
		
		Set<String> chars = new HashSet<>();
		int id = 0;
		
		System.out.println("Characters:");
		
		System.out.print("\t");
		
		for (int i = 0; i < text.length(); i++) {
			String ch = text.charAt(i) + "";
			if (chars.contains(ch) == false) {
				if (ch.equals("\n")) {
					System.out.print("\\n");
				}
				else {
					System.out.print(ch);
				}
				chars.add(ch);
				charToIndex.put(ch, id);
				indexToChar.put(id, ch);
				id++;
			}
		}
		System.out.println("");
		
		dimension = chars.size();
		
		List<DataSequence> sequences = new ArrayList<>();
		
		for (int s = 0; s < totalSequences; s++) {
			List<double[]> vecs = new ArrayList<>();
			int len = rng.nextInt(sequenceMaxLength - sequenceMinLength + 1) + sequenceMinLength;
			int start = rng.nextInt(text.length() - len);
			for (int i = 0; i < len; i++) {
				String ch = text.charAt(i+start) + "";
				int index = charToIndex.get(ch);
				double[] vec = new double[dimension];
				vec[index] = 1.0;
				vecs.add(vec);
			}
			DataSequence sequence = new DataSequence();
			for (int i = 0; i < vecs.size() - 1; i++) {
				sequence.steps.add(new DataStep(vecs.get(i), vecs.get(i+1)));
			}
			sequences.add(sequence);
		}

		System.out.println("Total unique chars = " + chars.size());
		
		training = sequences;
		lossTraining = new LossSoftmax();
		lossReporting = new LossSoftmax();
		inputDimension = sequences.get(0).steps.get(0).input.w.length;
		int loc = 0;
		while (sequences.get(0).steps.get(loc).targetOutput == null) {
			loc++;
		}
		outputDimension = sequences.get(0).steps.get(loc).targetOutput.w.length;
	}

	@Override
	public void DisplayReport(Model model, Random rng) throws Exception {
		System.out.println("========================================");
		System.out.println("REPORT:");
		if (reportPerplexity) {
			System.out.println("\ncalculating perplexity over entire data set...");
			double perplexity = LossSoftmax.calculateMedianPerplexity(model, training);
			System.out.println("\nMedian Perplexity = " + String.format("%.4f", perplexity));
		}
		double[] temperatures = {1, 0.75, 0.5, 0.25, 0.1};
		for (double temperature : temperatures) {
			System.out.println("\nTemperature "+temperature+" prediction:");
			String guess = TextGenerationUnbroken.generateText(model, reportSequenceLength, false, temperature, rng);
			System.out.println("\t\"..." + guess + "...\"");
		}
		System.out.println("\nArgmax prediction:");
		String guess = TextGenerationUnbroken.generateText(model, reportSequenceLength, true, 1.0, rng);
		System.out.println("\t\"..." + guess + "...\"");
		System.out.println("========================================");
	}

	@Override
	public Nonlinearity getModelOutputUnitToUse() {
		return new LinearUnit();
	}
}
