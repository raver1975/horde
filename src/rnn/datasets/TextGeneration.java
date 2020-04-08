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


public class TextGeneration extends DataSet {


    public static boolean reportPerplexity = true;
    private static Map<String, Integer> charToIndex = new HashMap<>();
    private static Map<Integer, String> indexToChar = new HashMap<>();
    private static int dimension;
    private static double[] vecStartEnd;
    private static final int START_END_TOKEN_INDEX = 0;

    public static List<String> generateText(Model model, int steps, boolean argmax, double temperature, Random rng) throws Exception {
        List<String> lines = new ArrayList<>();
        Matrix start = new Matrix(dimension);
        start.w[START_END_TOKEN_INDEX] = 1.0;
        model.resetState();
        Graph g = new Graph(false);
        Matrix input = start.clone();
        String line = "";
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
            } else {
                indxChosen = Util.pickIndexFromRandomVector(probs, rng);
            }
            if (indxChosen == START_END_TOKEN_INDEX) {
                lines.add(line);
                line = "";
                input = start.clone();
                g = new Graph(false);
                model.resetState();
                input = start.clone();
            } else {
                String ch = indexToChar.get(indxChosen);
                line += ch;
                for (int i = 0; i < input.w.length; i++) {
                    input.w[i] = 0;
                }
                input.w[indxChosen] = 1.0;
            }
        }
        if (line.equals("") == false) {
            lines.add(line);
        }
        return lines;
    }


    public static String sequenceToSentence(DataSequence sequence) {
        String result = "\"";
        for (int s = 0; s < sequence.steps.size() - 1; s++) {
            DataStep step = sequence.steps.get(s);
            int index = -1;
            for (int i = 0; i < step.targetOutput.w.length; i++) {
                if (step.targetOutput.w[i] == 1) {
                    index = i;
                    break;
                }
            }
            String ch = indexToChar.get(index);
            result += ch;
        }
        result += "\"\n";
        return result;
    }

    public TextGeneration(String path) throws Exception {

        System.out.println("Text generation task");
        System.out.println("loading " + path + "...");

        File file = new File(path);
        List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
        Set<String> chars = new HashSet<>();
        int id = 0;

        charToIndex.put("[START/END]", id);
        indexToChar.put(id, "[START/END]");
        id++;

        System.out.println("Characters:");

        System.out.print("\t");

        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {

//				String[] parts = line.split(" ");
//				for (String part : parts) {
//					words.add(part.trim());
//				}

                String ch = line.charAt(i) + "";
                if (chars.contains(ch) == false) {
                    System.out.print(ch);
                    chars.add(ch);
                    charToIndex.put(ch, id);
                    indexToChar.put(id, ch);
                    id++;
                }
            }
        }

        dimension = chars.size() + 1;
        vecStartEnd = new double[dimension];
        vecStartEnd[START_END_TOKEN_INDEX] = 1.0;

        List<DataSequence> sequences = new ArrayList<>();
        int size = 0;
        for (String line : lines) {
            List<double[]> vecs = new ArrayList<>();
            vecs.add(vecStartEnd);
            for (int i = 0; i < line.length(); i++) {
                String ch = line.charAt(i) + "";
                int index = charToIndex.get(ch);
                double[] vec = new double[dimension];
                vec[index] = 1.0;
                vecs.add(vec);
            }
            vecs.add(vecStartEnd);

            DataSequence sequence = new DataSequence();
            for (int i = 0; i < vecs.size() - 1; i++) {
                sequence.steps.add(new DataStep(vecs.get(i), vecs.get(i + 1)));
                size++;
            }
            sequences.add(sequence);
        }
        System.out.println("Total unique chars = " + chars.size());
        System.out.println(size + " steps in training set.");

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
//        double[] temperatures = {1, 0.75, 0.5, 0.25, 0.1};
//        for (double temperature : temperatures) {
//            System.out.println("\nTemperature " + temperature + " prediction:");
//            List<String> guess = TextGeneration.generateText(model, reportSequenceLength, false, temperature, rng);
//            for (int i = 0; i < guess.size(); i++) {
//                if (i == guess.size() - 1) {
//                    System.out.println("\t\"" + guess.get(i) + "...\"");
//                } else {
//                    System.out.println("\t\"" + guess.get(i) + "\"");
//                }
//
//            }
//        }
//        System.out.println("\nArgmax prediction:");
//        List<String> guess = TextGeneration.generateText(model, reportSequenceLength, true, 1.0, rng);
//        for (int i = 0; i < guess.size(); i++) {
//            if (i == guess.size() - 1) {
//                System.out.println("\t\"" + guess.get(i) + "...\"");
//            } else {
//                System.out.println("\t\"" + guess.get(i) + "\"");
//            }
//
//        }
        System.out.println("========================================");
    }

    @Override
    public Nonlinearity getModelOutputUnitToUse() {
        return new LinearUnit();
    }
}
