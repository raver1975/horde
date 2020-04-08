package rnn.model;

import rnn.autodiff.Graph;
import rnn.matrix.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LstmLayer implements Model {
	
	private static final long serialVersionUID = 1L;
	int inputDimension;
	int outputDimension;
	
	Matrix Wix, Wih, bi;
	Matrix Wfx, Wfh, bf;
	Matrix Wox, Woh, bo;
	Matrix Wcx, Wch, bc;
	
	Matrix hiddenContext;
	Matrix cellContext;
	
	Nonlinearity fInputGate = new SigmoidUnit();
	Nonlinearity fForgetGate = new SigmoidUnit();
	Nonlinearity fOutputGate = new SigmoidUnit();
	Nonlinearity fCellInput = new TanhUnit();
	Nonlinearity fCellOutput = new TanhUnit();
	
	public LstmLayer(int inputDimension, int outputDimension, double initParamsStdDev, Random rng) {
		this.inputDimension = inputDimension;
		this.outputDimension = outputDimension;
		Wix = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		Wih = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		bi = new Matrix(outputDimension);
		Wfx = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		Wfh = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		//set forget bias to 1.0, as described here: http://jmlr.org/proceedings/papers/v37/jozefowicz15.pdf
		bf = Matrix.ones(outputDimension, 1);
		Wox = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		Woh = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		bo = new Matrix(outputDimension);
		Wcx = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		Wch = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		bc = new Matrix(outputDimension);
	}

	@Override
	public Matrix forward(Matrix input, Graph g) throws Exception {
		
		//input gate
		Matrix sum0 = g.mul(Wix, input);
		Matrix sum1 = g.mul(Wih, hiddenContext);
		Matrix inputGate = g.nonlin(fInputGate, g.add(g.add(sum0, sum1), bi));
		
		//forget gate
		Matrix sum2 = g.mul(Wfx, input);
		Matrix sum3 = g.mul(Wfh, hiddenContext);
		Matrix forgetGate = g.nonlin(fForgetGate, g.add(g.add(sum2, sum3), bf));
		
		//output gate
		Matrix sum4 = g.mul(Wox, input);
		Matrix sum5 = g.mul(Woh, hiddenContext);
		Matrix outputGate = g.nonlin(fOutputGate, g.add(g.add(sum4, sum5), bo));

		//write operation on cells
		Matrix sum6 = g.mul(Wcx, input);
		Matrix sum7 = g.mul(Wch, hiddenContext);
		Matrix cellInput = g.nonlin(fCellInput, g.add(g.add(sum6, sum7), bc));
		
		//compute new cell activation
		Matrix retainCell = g.elmul(forgetGate, cellContext);
		Matrix writeCell = g.elmul(inputGate,  cellInput);
		Matrix cellAct = g.add(retainCell,  writeCell);
		
		//compute hidden state as gated, saturated cell activations
		Matrix output = g.elmul(outputGate, g.nonlin(fCellOutput, cellAct));
		
		//rollover activations for next iteration
		hiddenContext = output;
		cellContext = cellAct;
		
		return output;
	}

	@Override
	public void resetState() {
		hiddenContext = new Matrix(outputDimension);
		cellContext = new Matrix(outputDimension);
	}

	@Override
	public List<Matrix> getParameters() {
		List<Matrix> result = new ArrayList<>();
		result.add(Wix);
		result.add(Wih);
		result.add(bi);
		result.add(Wfx);
		result.add(Wfh);
		result.add(bf);
		result.add(Wox);
		result.add(Woh);
		result.add(bo);
		result.add(Wcx);
		result.add(Wch);
		result.add(bc);
		return result;
	}
}
