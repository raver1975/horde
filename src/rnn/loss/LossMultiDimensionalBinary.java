package rnn.loss;

import rnn.matrix.Matrix;

public class LossMultiDimensionalBinary implements Loss {

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
		
		for (int i = 0; i < targetOutput.w.length; i++) {
			if (targetOutput.w[i] >= 0.5 && actualOutput.w[i] < 0.5) {
				return 1;
			}
			if (targetOutput.w[i] < 0.5 && actualOutput.w[i] >= 0.5) {
				return 1;
			}
		}
		return 0;
	}

}
