package rnn.loss;

import rnn.matrix.Matrix;

public class LossArgMax implements Loss {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void backward(Matrix actualOutput, Matrix targetOutput) throws Exception {
		throw new Exception("not implemented");
		
	}

	@Override
	public double measure(Matrix actualOutput, Matrix targetOutput) throws Exception {
		if (actualOutput.w.length != targetOutput.w.length) {
			throw new Exception("mismatch");
		}
		double maxActual = Double.NEGATIVE_INFINITY;
		double maxTarget = Double.NEGATIVE_INFINITY;
		int indxMaxActual = -1;
		int indxMaxTarget = -1;
		for (int i = 0; i < actualOutput.w.length; i++) {
			if (actualOutput.w[i] > maxActual) {
				maxActual = actualOutput.w[i];
				indxMaxActual = i;
			}
			if (targetOutput.w[i] > maxTarget) {
				maxTarget = targetOutput.w[i];
				indxMaxTarget = i;
			}
		}
		if (indxMaxActual == indxMaxTarget) {
			return 0;
		}
		else {
			return 1;
		}
	}
	
}
