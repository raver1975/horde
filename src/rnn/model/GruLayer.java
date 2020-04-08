package rnn.model;

import rnn.autodiff.Graph;
import rnn.matrix.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * As described in:
 * 	"Learning Phrase Representations using RNNDemo Encoder-Decoder for Statistical Machine Translation"
 * 	http://arxiv.org/abs/1406.1078
*/

public class GruLayer implements Model {

	private static final long serialVersionUID = 1L;
	int inputDimension;
	int outputDimension;
	
	Matrix IHmix, HHmix, Bmix;
	Matrix IHnew, HHnew, Bnew;
	Matrix IHreset, HHreset, Breset;
	
	Matrix context;
	
	Nonlinearity fMix = new SigmoidUnit();
	Nonlinearity fReset = new SigmoidUnit();
	Nonlinearity fNew = new TanhUnit();
	
	public GruLayer(int inputDimension, int outputDimension, double initParamsStdDev, Random rng) {
		this.inputDimension = inputDimension;
		this.outputDimension = outputDimension;
		IHmix = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		HHmix = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		Bmix = new Matrix(outputDimension);
		IHnew = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		HHnew = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		Bnew = new Matrix(outputDimension);
		IHreset = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		HHreset = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		Breset= new Matrix(outputDimension);
	}
	
	@Override
	public Matrix forward(Matrix input, Graph g) throws Exception {
		
		Matrix sum0 = g.mul(IHmix, input);
		Matrix sum1 = g.mul(HHmix, context);
		Matrix actMix = g.nonlin(fMix, g.add(g.add(sum0, sum1), Bmix));

		Matrix sum2 = g.mul(IHreset, input);
		Matrix sum3 = g.mul(HHreset, context);
		Matrix actReset = g.nonlin(fReset, g.add(g.add(sum2, sum3), Breset));
		
		Matrix sum4 = g.mul(IHnew, input);
		Matrix gatedContext = g.elmul(actReset, context);
		Matrix sum5 = g.mul(HHnew, gatedContext);
		Matrix actNewPlusGatedContext = g.nonlin(fNew, g.add(g.add(sum4, sum5), Bnew));
		
		Matrix memvals = g.elmul(actMix, context);
		Matrix newvals = g.elmul(g.oneMinus(actMix), actNewPlusGatedContext);
		Matrix output = g.add(memvals, newvals);
		
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
		result.add(IHmix);
		result.add(HHmix);
		result.add(Bmix);
		result.add(IHnew);
		result.add(HHnew);
		result.add(Bnew);
		result.add(IHreset);
		result.add(HHreset);
		result.add(Breset);
		return result;
	}

}
