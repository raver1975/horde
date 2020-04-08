package rnn.model;



public class SigmoidUnit implements Nonlinearity {

	private static final long serialVersionUID = 1L;

	@Override
	public double forward(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	@Override
	public double backward(double x) {
		double act = forward(x);
		return act * (1 - act);
	}
}
