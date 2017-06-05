package pl.edu.agh.sm.magneto.desktop;

import com.google.common.collect.EvictingQueue;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import pl.edu.agh.sm.magneto.commons.PositionData;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

public class PositionCalculator {
	private static final double[] START_POSITION = new double[]{0,0,0};
	private static final int FINISH_CALIBRATING_STEP = 10;
	private static final double ZUPT_THRESHOLD = 0.00001;
	private static final int ZUPT_WINDOW_SIZE = 10;
	private static final float ALPHA = 0.25f;

	private INDArray p_0;
	private INDArray F;
	private INDArray P;
	private INDArray C;
	private double mi;

	private State state;
	private long lastReceiveTime;
	private List<float[]> magnetometerCalibratingData = new ArrayList<>();
	private List<float[]> accelerometerCalibratingData = new ArrayList<>();
	private List<float[]> gyroscopeCalibratingData = new ArrayList<>();

	private int k;
	private double dt;
	private INDArray Q;
	private INDArray R;
	private INDArray H_pos;
	private INDArray H_vel;
	private EvictingQueue<Double> zuptWindow;

	private INDArray previousX;
	private Function<double[], double[]> interpolant;

	private float[] magnetometerValues;
	private float[] accelerometerValues;
	private float[] gyroscopeValues;

	private float[] accelerometerZeroValues;
	private float[] gyroscopeZeroValues;


	public PositionCalculator() {
		p_0 = Nd4j.create(new double[]{0, 0}); // start position
		k = 0;
		state = State.CALIBRATING;
		zuptWindow = EvictingQueue.create(ZUPT_WINDOW_SIZE);
	}

	private static double calculateLength(double[] vector) {
		double sum = 0;
		for (double e : vector) {
			sum += e * e;
		}
		return Math.sqrt(sum);
	}

	private static double calculateLength(float[] vector) {
		double sum = 0;
		for (double e : vector) {
			sum += e * e;
		}
		return Math.sqrt(sum);
	}

	public double[] calculatePosition(PositionData data) {


		magnetometerValues = lowPassFilter(data.getMagnetometer(), magnetometerValues);
		accelerometerValues = lowPassFilter(data.getAccelerometer(), accelerometerValues);
		gyroscopeValues = lowPassFilter(data.getGyroscope(), gyroscopeValues);

		k += 1;
		zuptWindow.add(calculateLength(gyroscopeValues));
		if (state == State.CALIBRATING) {
			if (k == 1) {
				lastReceiveTime = System.currentTimeMillis();
			} else if (k == FINISH_CALIBRATING_STEP) {
				long time = System.currentTimeMillis();
				finishCalibration(time - lastReceiveTime);
				state = State.RUNNING;
			}
			magnetometerCalibratingData.add(magnetometerValues.clone());
			accelerometerCalibratingData.add(accelerometerValues.clone());
			gyroscopeCalibratingData.add(gyroscopeValues.clone());
			return START_POSITION;
		}


		float[] a = subArray(accelerometerValues, accelerometerZeroValues);
		float[] g = subArray(gyroscopeValues, gyroscopeZeroValues);

		INDArray a_k = Nd4j.create(accelerometerValues).transpose();
		INDArray g_k = Nd4j.create(gyroscopeValues).transpose();

		INDArray Omega = Nd4j.create(new double[][]{{0.0, -g_k.getDouble(2), g_k.getDouble(1)},
				{g_k.getDouble(2), 0.0, -g_k.getDouble(0)},
				{-g_k.getDouble(1), g_k.getDouble(0), 0.0}});


		C = C.mmul((Nd4j.eye(3).mul(2.0).add(Omega.mul(dt))).mmul(inverse(Nd4j.eye(3).mul(2.0).sub(Omega.mul(dt)))));
		INDArray a_nav = C.mmul(a_k);
		double ax = a_nav.getDouble(0);
		double ay = a_nav.getDouble(1);
		double az = a_nav.getDouble(2);


		INDArray x = F.mmul(previousX).addColumnVector(Nd4j.create(new double[]{dt * ax, dt * ay, 0, 0}));
//		x = previousX;

//		System.out.println(ax + ";" + ay + ";" + az + ";" + x.getDouble(0) + ";" + x.getDouble(1) + ";" + x.getDouble(2) + ";" + x.getDouble(3));

//		INDArray v = Nd4j.create(new double[]{ax, ay}).mul(dt).add(x.getRow(0).getColumns(0, 1));
//		INDArray p = v.mul(dt).add(x.getRow(0).getColumns(2, 3));

		P = F.mmul(P).mmul(F.transpose()).add(Q);
//		if ((k % 2) == 0) {
//		if (false) {
		if (calculateLength(magnetometerValues) > 0) {
			double xPos = x.getDouble(2, 0);
			double yPos = x.getDouble(3, 0);
			INDArray zk = Nd4j.create(correct_pos(new double[]{xPos, yPos, 0}, magnetometerValues, interpolant)).transpose(); //<----mag correction (interpolant)

			INDArray K = P.mmul(H_pos.transpose()).mmul(inverse(H_pos.mmul(P).mmul(H_pos.transpose()).add(R)));
			P = Nd4j.eye(4).sub(K.mmul(H_pos)).mmul(P);
			P = P.add(P.transpose()).div(2.0);
			INDArray yk = H_pos.mmul(x).subRowVector(zk).mul(-1);
			INDArray dx = K.mmul(yk);

			x = x.addColumnVector(dx);


//			x.put(2, 0, zk.getDouble(0, 0));
//			x.put(3, 0, zk.getDouble(1, 0));

		}
		if (isZupt()) {
			INDArray K = P.mmul(H_vel.transpose()).mmul(inverse(H_vel.mmul(P).mmul(H_vel.transpose()).add(R)));
			P = (Nd4j.eye(4).sub(K.mmul(H_vel))).mmul(P);
			P = P.add(P.transpose()).div(2.0);

			INDArray yk = H_vel.mmul(x).mul(-1);
			x = x.add(K.mmul(yk));
		}
		previousX = x;
		System.out.println(accelerometerValues[0] + ";" + accelerometerValues[1] + ";" +accelerometerValues[2] + ";" +x.getDouble(0) + ";" + x.getDouble(1) + ";" + x.getDouble(2) + ";" + x.getDouble(3));

		return new double[]{previousX.getDouble(2, 0), previousX.getDouble(3, 0), 0.0};

	}

	private INDArray inverse(INDArray input) {
		return Nd4j.create(MatrixUtils.inverse(new BlockRealMatrix(toArray(input))).getData());
	}

	private double[][] toArray(INDArray input) {
		double[][] result = new double[input.rows()][input.columns()];
		for (int row = 0; row < input.rows(); row++) {
			for (int column = 0; column < input.columns(); column++) {
				result[row][column] = input.getDouble(row, column);
			}
		}
		return result;
	}

	private double[][] correct_pos(double[] p_tmp, float[] m_c, Function<double[], double[]> interpolant) {
		try {
			SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-5, 1e-10, 10000));
			PointValuePair a = optimizer.optimize(new MaxEval(1000000),
					new ObjectiveFunction(new MagneticFieldModel(interpolant, m_c)),
					GoalType.MINIMIZE,
					new InitialGuess(p_tmp),
					new NelderMeadSimplex(new double[]{0.01, 0.01, 0.01}));
			return new double[][]{{a.getPoint()[0]}, {a.getPoint()[1]}};
		} catch (TooManyEvaluationsException e) {
			e.printStackTrace();
			return new double[][]{{p_tmp[0]}, {p_tmp[1]}};
		}
	}

	private boolean isZupt() {
		double sum = 0.0;
		for (double a : zuptWindow)
			sum += a;
		double mean = sum / zuptWindow.size();

		sum = 0;
		for (double a : zuptWindow)
			sum += (a - mean) * (a - mean);
//		System.out.println((sum / zuptWindow.size()) + " " + (sum / zuptWindow.size() < ZUPT_THRESHOLD));
		return sum / zuptWindow.size() < ZUPT_THRESHOLD;
	}

	private void finishCalibration(double t) {
		dt = (FINISH_CALIBRATING_STEP - 1) / t;
		F = Nd4j.vstack(Nd4j.hstack(Nd4j.eye(2), Nd4j.zeros(2, 2)), Nd4j.hstack(Nd4j.eye(2).mul(dt), Nd4j.eye(2)));
		P = Nd4j.zeros(4, 4);
		double kq_v = 0.00001;
		double kq_p = 0.001;
		double kr = 0.00000001;
		Q = Nd4j.diag(Nd4j.create(new double[]{kq_v, kq_v, kq_p, kq_p})).mul(dt);
		R = Nd4j.diag(Nd4j.create(new double[]{1.0, 1.0})).mul(kr);
		previousX = Nd4j.create(new double[]{0, 0, START_POSITION[0], START_POSITION[1]}).transpose();
		C = Nd4j.eye(3);

		H_pos = Nd4j.create(new double[][]{{0, 0, 1, 0}, {0, 0, 0, 1}});
		H_vel = Nd4j.create(new double[][]{{1, 0, 0, 0}, {0, 1, 0, 0}});
		interpolant = createInterpolant(calculateMi());
		accelerometerZeroValues = avgArray(accelerometerCalibratingData);
		gyroscopeZeroValues = avgArray(gyroscopeCalibratingData);

		k = 0;
	}

	private float[] avgArray(List<float[]> arrays) {
		float avgX = 0;
		float avgY = 0;
		float avgZ = 0;
		for (float[] vector : arrays) {
			avgX += vector[0];
			avgY += vector[1];
			avgZ += vector[2];
		}
		return new float[]{avgX / arrays.size(),
				avgY / arrays.size(),
				avgZ / arrays.size()};
	}

	private float[] subArray(float[] first, float[] second) {
		if (first.length != second.length) {
			throw new IllegalArgumentException("Different length of arrays");
		}
		float[] result = new float[first.length];
		for (int i = 0; i < first.length; i++) {
			result[i] = first[i] - second[i];
		}
		return result;
	}

	private double[] calculateMi() {
		float[] avg = avgArray(magnetometerCalibratingData);

		double x = START_POSITION[0];
		double y = START_POSITION[1];
		double z = START_POSITION[2];
		double denominator = Math.pow(x * x + y * y + z * z, 5 / 2);


		return new double[]{
				avg[0] * denominator / (3 * z * y),
				avg[1] * denominator / (3 * z * x),
				avg[2] * denominator / (2 * (x * x - y * y) - z * z)
		};
	}

	private Function<double[], double[]> createInterpolant(double[] mi) {
		double miLength = calculateLength(mi);
		return vector -> {
			double x = vector[0];
			double y = vector[1];
			double z = vector[2];
			double denominator = Math.pow(x * x + y * y + z * z, 5 / 2);

			return new double[]{
					3 * miLength * z * y / denominator,
					3 * miLength * z * x / denominator,
					miLength * (2 * (x * x - y * y) - z * z) / denominator
			};
		};

	}

	private float[] lowPassFilter(float[] input, float[] output) {
		if (output == null) return input;
		for (int i = 0; i < input.length; i++) {
			output[i] = output[i] + ALPHA * (input[i] - output[i]);
		}
//		return output;
		return input;
	}

	private enum State {
		CALIBRATING, RUNNING
	}

	private static class MagneticFieldModel implements MultivariateFunction {
		private Function<double[], double[]> interpolant;
		private float[] measuredValue;

		MagneticFieldModel(Function<double[], double[]> interpolant, float[] measuredValue) {
			this.interpolant = interpolant;
			this.measuredValue = measuredValue;
		}

		@Override
		public double value(double[] point) {
			double[] modeledValue = interpolant.apply(point);
			double first = calculateLength(new double[]{modeledValue[0] - measuredValue[0], modeledValue[1] - measuredValue[1], modeledValue[2] - measuredValue[2]});
			double second = calculateLength(modeledValue) - calculateLength(measuredValue);
//			System.out.println(first * first + second * second);
			return first * first + second * second;
		}
	}

}
