package rnn.model;



public class TanhUnit implements Nonlinearity {

	private static final long serialVersionUID = 1L;

	@Override
	public double forward(double x) {
		return Math.tanh(x);
	}

	@Override
	public double backward(double x) {
		double coshx = Math.cosh(x);
		double denom = (Math.cosh(2*x) + 1);
		return 4 * coshx * coshx / (denom * denom);
	}
}
