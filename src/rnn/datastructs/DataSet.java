package rnn.datastructs;

import rnn.loss.Loss;
import rnn.model.Model;
import rnn.model.Nonlinearity;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public abstract class DataSet implements Serializable {
	public int inputDimension;
	public int outputDimension;
	public Loss lossTraining;
	public Loss lossReporting;
	public List<DataSequence> training;
	public List<DataSequence> validation;
	public List<DataSequence> testing;
	public abstract void DisplayReport(Model model, Random rng) throws Exception;
	public abstract Nonlinearity getModelOutputUnitToUse();
}
