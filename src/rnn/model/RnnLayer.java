package rnn.model;

import rnn.autodiff.Graph;
import rnn.matrix.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RnnLayer implements Model {

	private static final long serialVersionUID = 1L;
	int inputDimension;
	int outputDimension;
	
	Matrix W, b;
	
	Matrix context;
	
	Nonlinearity f;
	
	public RnnLayer(int inputDimension, int outputDimension, Nonlinearity hiddenUnit, double initParamsStdDev, Random rng) {
		this.inputDimension = inputDimension;
		this.outputDimension = outputDimension;
		this.f = hiddenUnit;
		W = Matrix.rand(outputDimension, inputDimension+outputDimension, initParamsStdDev, rng);
		b = new Matrix(outputDimension);
	}
	
	@Override
	public Matrix forward(Matrix input, Graph g) throws Exception {
		
		Matrix concat = g.concatVectors(input, context);
		
		Matrix sum = g.mul(W, concat);
		sum = g.add(sum, b);
		Matrix output = g.nonlin(f, sum);
		
		//rollover activations for next iteration
		context = output;
		
		return output;
	}

	@Override
	public void resetState() {
		context = new Matrix(outputDimension);
	}

	@Override
	public List<Matrix> getParameters() {
		List<Matrix> result = new ArrayList<>();
		result.add(W);
		result.add(b);
		return result;
	}

}
