package rnn.model;

import rnn.autodiff.Graph;
import rnn.matrix.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class FeedForwardLayer implements Model {

	private static final long serialVersionUID = 1L;
	Matrix W;
	Matrix b;
	Nonlinearity f;
	
	public FeedForwardLayer(int inputDimension, int outputDimension, Nonlinearity f, double initParamsStdDev, Random rng) {
		W = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		b = new Matrix(outputDimension);
		this.f = f;
	}
	
	@Override
	public Matrix forward(Matrix input, Graph g) throws Exception {
		Matrix sum = g.add(g.mul(W, input), b);
		Matrix out = g.nonlin(f, sum);
		return out;
	}

	@Override
	public void resetState() {

	}

	@Override
	public List<Matrix> getParameters() {
		List<Matrix> result = new ArrayList<>();
		result.add(W);
		result.add(b);
		return result;
	}
}
