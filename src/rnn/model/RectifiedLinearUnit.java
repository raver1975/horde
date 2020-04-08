package rnn.model;


public class RectifiedLinearUnit implements Nonlinearity {

	private static final long serialVersionUID = 1L;
	private double slope;
	
	public RectifiedLinearUnit() {
		this.slope = 0;
	}
	
	public RectifiedLinearUnit(double slope) {
		this.slope = slope;
	}
	
	@Override
	public double forward(double x) {
		if (x >= 0) {
			return x;
		}
		else {
			return x * slope;
		}
	}

	@Override
	public double backward(double x) {
		if (x >= 0) {
			return 1.0;
		}
		else {
			return slope;
		}
	}
}
