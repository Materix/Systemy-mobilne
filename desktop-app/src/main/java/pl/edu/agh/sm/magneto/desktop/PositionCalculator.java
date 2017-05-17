package pl.edu.agh.sm.magneto.desktop;

import com.google.common.collect.EvictingQueue;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.inverse.InvertMatrix;
import pl.edu.agh.sm.magneto.commons.PositionData;

import java.io.Serializable;

public class PositionCalculator {
	private static final double[] START_POSITION = new double[]{0.17, -0.17, 0};
	private static final int FINISH_CALIBRATING_STEP = 10;
	private static final double ZUPT_THRESHOLD = 10;
	private static final int ZUPT_WINDOW_SIZE = 10;

	private INDArray p_0;
	private INDArray F;
	private INDArray P;
	private INDArray C;

	private State state;
	private long lastReceiveTime;

	private int k;
	private double dt;
	private INDArray Q;
	private INDArray R;
	private INDArray H_pos;
	private INDArray H_vel;
	private EvictingQueue<Double> zuptWindow;

	private INDArray previousX;


	public PositionCalculator() {
		p_0 = Nd4j.create(new double[]{0.17, -0.17}); // start position
		k = 0;
		state = State.CALIBRATING;
		zuptWindow = EvictingQueue.create(ZUPT_WINDOW_SIZE);
	}

	public void calibrate() {
//			 x = [vx_k, vy_k, px_k, py_k]
//		x = np.zeros(shape = (len(a)+1, 2 + 2))
//		x[0][2:4] = p_0
	}

	public double[] calculatePosition(PositionData data) {
		k += 1;
		zuptWindow.add(calculateLenght(data.getMagnetometer()));
		if (state == State.CALIBRATING) {
			if (k == 1) {
				lastReceiveTime = System.currentTimeMillis();
			} else if (k == FINISH_CALIBRATING_STEP) {
				long time = System.currentTimeMillis();
				finishCalibration(time - lastReceiveTime);
				state = State.RUNNING;
			}
			return START_POSITION;
		}
		INDArray a_k = Nd4j.create(data.getAccelerometer());
		INDArray g_k = Nd4j.create(data.getGyroscope());

		INDArray Omega = Nd4j.create(new double[][]{{0.0, -g_k.getDouble(2), g_k.getDouble(1)},
				{g_k.getDouble(2), 0.0, -g_k.getDouble(0)},
				{-g_k.getDouble(1), g_k.getDouble(0), 0.0}});

		C = C.mmul((Nd4j.eye(3).mul(2.0).add(Omega.mul(dt))).mmul(
				InvertMatrix.invert(Nd4j.eye(3).mul(2.0).sub(Omega.mul(dt)), false)));
		INDArray a_nav = C.mmul(a_k);
		double ax = a_nav.getDouble(0);
		double ay = a_nav.getDouble(1);
		double az = a_nav.getDouble(2);

		INDArray x = F.mmul(previousX).addColumnVector(Nd4j.create(new double[]{dt * ax, dt * ay, 0, 0}));



		INDArray v = Nd4j.create(new double[]{ax, ay}).mul(dt).add(x.getRow(0).getColumns(0,1));
		INDArray p = v.mul(dt).add(x.getRow(0).getColumns(2,3));

		P = F.mmul(P).mmul(F.transpose()).add(Q);
		if ((k % 15) == 0) {
			INDArray m_c = Nd4j.create(data.getMagnetometer());
			INDArray p_tmp = x.getRow(0).getColumns(2,3);
			INDArray zk = correct_pos(p_tmp, m_c, null); //<----mag correction (interpolant)

			INDArray K = P.mmul(H_pos.transpose()).mmul(InvertMatrix.invert(H_pos.mmul(P).mmul(H_pos.transpose()).add(R), false));
			P = Nd4j.eye(4).sub(K.mmul(H_pos)).mmul(P);
			P = P.add(P.transpose()).div(2.0);
			INDArray yk = H_pos.mmul(x).subRowVector(zk).mul(-1);
			INDArray dx = (K.mmul(yk));
			INDArray xk_ = x.getRow(0).getColumns(2,3).add(dx.getRow(0).getColumns(2,3));
			INDArray vk_ = x.getRow(0).getColumns(0,1).add(dx.getRow(0).getColumns(0,1));

			x.put(0,0, vk_.getDouble(0));
			x.put(0,1, vk_.getDouble(1));
			x.put(0,2, xk_.getDouble(0));
			x.put(0,3, xk_.getDouble(1));
		}
		if (isZupt()) {
			INDArray K = P.mmul(H_vel.transpose()).mmul(InvertMatrix.invert(H_vel.mmul(P).mmul(H_vel.transpose()).add(R), false));
			P = (Nd4j.eye(4).sub(K.mmul(H_vel))).mmul(P);
			P = P.add(P.transpose()).div(2.0);

			INDArray yk = H_vel.mmul(x).mul(-1);
			x = x.add(K.mmul(yk));
		}

		previousX = x;

		return new double[]{previousX.getDouble(2),previousX.getDouble(3), 0.0};
	}

	private INDArray correct_pos(INDArray p_tmp, INDArray m_c, Object o) {
		return Nd4j.create(new double[][]{{0},{0}});
	}

	private boolean isZupt() {
		double sum = 0.0;
		for (double a : zuptWindow)
			sum += a;
		double mean = sum / zuptWindow.size();

		sum = 0;
		for (double a :zuptWindow)
			sum += (a - mean) * (a - mean);
		return sum / zuptWindow.size() < ZUPT_THRESHOLD;
	}

	private void finishCalibration(double t) {
		dt = (FINISH_CALIBRATING_STEP - 1) / t;
		F = Nd4j.vstack(Nd4j.hstack(Nd4j.eye(2), Nd4j.zeros(2, 2)), Nd4j.hstack(Nd4j.eye(2).mul(dt), Nd4j.eye(2)));
		P = Nd4j.zeros(4,4);
		double kq_v = 0.00001;
		double kq_p = 0.001;
		double kr = 0.00000001;
		Q = Nd4j.diag(Nd4j.create(new double[]{kq_v, kq_v, kq_p, kq_p})).mul(dt);
		R = Nd4j.diag(Nd4j.create(new double[]{1.0, 1.0})).mul(kr);
		previousX = Nd4j.create(new double[]{0, 0, START_POSITION[0], START_POSITION[1]});
		C = Nd4j.create(new double[][]{{0, 0,0}, {0, 0,0},{0, 0,0}});

		H_pos = Nd4j.create(new double[][]{{0,0,1,0}, {0,0,0,1}});
		H_vel = Nd4j.create(new double[][]{{1,0,0,0}, {0,1,0,0}});

		k = 0;
	}

	private double calculateLenght(float[] vector) {
		double sum = 0;
		for (float e: vector) {
			sum += e*e;
		}
		return Math.sqrt(sum);
	}

	private enum State {
		CALIBRATING, RUNNING;
	}
}
