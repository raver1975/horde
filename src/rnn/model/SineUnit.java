package rnn.model;



public class SineUnit implements Nonlinearity {

	private static final long serialVersionUID = 1L;

	@Override
	public double forward(double x) {
		return Math.sin(x);
	}

	@Override
	public double backward(double x) {
		return Math.cos(x);
	}
}
