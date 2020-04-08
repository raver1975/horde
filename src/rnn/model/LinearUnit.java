package rnn.model;


public class LinearUnit implements Nonlinearity {
	

	private static final long serialVersionUID = 1L;

	@Override
	public double forward(double x) {
		return x;
	}

	@Override
	public double backward(double x) {
		return 1.0;
	}
}
