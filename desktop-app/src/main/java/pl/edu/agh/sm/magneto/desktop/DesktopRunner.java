package pl.edu.agh.sm.magneto.desktop;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import javafx.util.Duration;
import pl.edu.agh.sm.magneto.commons.PositionData;
import pl.edu.agh.sm.magneto.desktop.ui.Xform;

import java.awt.*;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

public class DesktopRunner extends Application {
	private static final double SCALE = 1;
	private static Logger logger = Logger.getLogger(DesktopRunner.class.getName());
	final Group root = new Group();
	final Group axisGroup = new Group();
	final Xform world = new Xform();
	final PerspectiveCamera camera = new PerspectiveCamera(true);
	final Xform cameraXform = new Xform();
	final Xform cameraXform2 = new Xform();
	final Xform cameraXform3 = new Xform();
	final double cameraDistance = 450;
	final Xform moleculeGroup = new Xform();
	final Xform phoneGroup = new Xform();
	boolean timelinePlaying = false;
	double ONE_FRAME = 1.0 / 24.0;
	double DELTA_MULTIPLIER = 200.0;
	double CONTROL_MULTIPLIER = 0.1;
	double SHIFT_MULTIPLIER = 0.1;
	double ALT_MULTIPLIER = 0.5;
	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;
	PhongMaterial redMaterial;
	Xform phoneSphereXform;
	private Thread receiveDataThread;
	private Timeline timeline;

	public static void main(String[] args) throws IOException, AWTException {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {


		buildScene();
		buildCamera();
//        buildAxes();
		buildModel();


		startDataReceiver();
//        Scene scene = new Scene(FXMLLoader.load(DesktopRunner.class.getResource("main.fxml")),
//                1024, 768, true);
		Scene scene = new Scene(root, 1024, 768, true);
		scene.setFill(Color.GREY);

		handleKeyboard(scene, world);
		handleMouse(scene, world);

		primaryStage.setTitle("Magneto");
		primaryStage.setScene(scene);
		primaryStage.show();

		scene.setCamera(camera);

		primaryStage.setOnCloseRequest(t -> {
			receiveDataThread.stop();
			Platform.exit();
			System.exit(0);
		});
	}

	private void startDataReceiver() {

		receiveDataThread = new Thread(() -> {

			PositionCalculator positionCalculator = new PositionCalculator();
			DataReceiver dataReceiver = null;
			try {
				dataReceiver = new DataReceiver();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
			while (true) {
				try {
					PositionData data = dataReceiver.receiveData();
//					logger.log(Level.INFO, data.toString());
					double[] position = positionCalculator.calculatePosition(data);

					// XYZ -> XZY
					phoneSphereXform.setTranslateX(position[0] * SCALE);
					phoneSphereXform.setTranslateY(position[2] * SCALE);
					phoneSphereXform.setTranslateZ(position[1] * SCALE);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		});
		receiveDataThread.start();
	}

	private void buildScene() {
		root.getChildren().add(world);
	}

	private void buildCamera() {
		root.getChildren().add(cameraXform);
		cameraXform.getChildren().add(cameraXform2);
		cameraXform2.getChildren().add(cameraXform3);
		cameraXform3.getChildren().add(camera);
		cameraXform3.setRotateZ(180.0);

		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setTranslateZ(-cameraDistance);
		cameraXform.ry.setAngle(320.0);
		cameraXform.rx.setAngle(40);
	}

	private void buildAxes() {

		redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);

		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);

		final Box xAxis = new Box(240.0, 1, 1);
		final Box yAxis = new Box(1, 240.0, 1);
		final Box zAxis = new Box(1, 1, 240.0);

		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		world.getChildren().addAll(axisGroup);
	}

	private void buildModel() {

		final PhongMaterial silverMaterial = new PhongMaterial();
		silverMaterial.setDiffuseColor(Color.SILVER);
		silverMaterial.setSpecularColor(Color.SILVER);

		final PhongMaterial redMaterial = new PhongMaterial();
		silverMaterial.setDiffuseColor(Color.RED);
		silverMaterial.setSpecularColor(Color.RED);

		Xform magnetBoxXform = new Xform();

		Box magnetBox = new Box(10, 5, 20);
		magnetBox.setMaterial(silverMaterial);
		magnetBoxXform.getChildren().add(magnetBox);
		moleculeGroup.getChildren().add(magnetBoxXform);


		phoneSphereXform = new Xform();
		Sphere phoneSphere = new Sphere(2);
		phoneSphere.setMaterial(redMaterial);
		phoneSphereXform.getChildren().add(phoneSphere);

		phoneGroup.setTranslateX(0);
		phoneGroup.setTranslateY(0);
		phoneGroup.setTranslateZ(0);

		phoneGroup.getChildren().add(phoneSphereXform);

		world.getChildren().addAll(moleculeGroup);
		world.getChildren().addAll(phoneGroup);
	}

	private void handleMouse(Scene scene, final Node root) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);

				double modifier = 1.0;
				double modifierFactor = 0.1;

				if (me.isControlDown()) {
					modifier = 0.1;
				}
				if (me.isShiftDown()) {
					modifier = 10.0;
				}
				if (me.isPrimaryButtonDown()) {
					cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
					cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -
				} else if (me.isSecondaryButtonDown()) {
					double z = camera.getTranslateZ();
					double newZ = z + mouseDeltaX * modifierFactor * modifier;
					camera.setTranslateZ(newZ);
				} else if (me.isMiddleButtonDown()) {
					cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
					cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
				}
			}
		});
	}

	private void handleKeyboard(Scene scene, final Node root) {
		final boolean moveCamera = true;
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				Duration currentTime;
				switch (event.getCode()) {
					case Z:
						if (event.isShiftDown()) {
							cameraXform.ry.setAngle(0.0);
							cameraXform.rx.setAngle(0.0);
							camera.setTranslateZ(-300.0);
						}
						cameraXform2.t.setX(0.0);
						cameraXform2.t.setY(0.0);
						break;
					case X:
						if (event.isControlDown()) {
							if (axisGroup.isVisible()) {
								axisGroup.setVisible(false);
							} else {
								axisGroup.setVisible(true);
							}
						}
						break;
					case S:
						if (event.isControlDown()) {
							if (moleculeGroup.isVisible()) {
								moleculeGroup.setVisible(false);
							} else {
								moleculeGroup.setVisible(true);
							}
						}
						break;
					case UP:
						if (event.isControlDown() && event.isShiftDown()) {
							cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown() && event.isShiftDown()) {
							cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
						} else if (event.isControlDown()) {
							cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown()) {
							cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
						} else if (event.isShiftDown()) {
							double z = camera.getTranslateZ();
							double newZ = z + 5.0 * SHIFT_MULTIPLIER;
							camera.setTranslateZ(newZ);
						}
						break;
					case DOWN:
						if (event.isControlDown() && event.isShiftDown()) {
							cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown() && event.isShiftDown()) {
							cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
						} else if (event.isControlDown()) {
							cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown()) {
							cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
						} else if (event.isShiftDown()) {
							double z = camera.getTranslateZ();
							double newZ = z - 5.0 * SHIFT_MULTIPLIER;
							camera.setTranslateZ(newZ);
						}
						break;
					case RIGHT:
						if (event.isControlDown() && event.isShiftDown()) {
							cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown() && event.isShiftDown()) {
							cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
						} else if (event.isControlDown()) {
							cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown()) {
							cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
						}
						break;
					case LEFT:
						if (event.isControlDown() && event.isShiftDown()) {
							cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown() && event.isShiftDown()) {
							cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
						} else if (event.isControlDown()) {
							cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
						} else if (event.isAltDown()) {
							cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
						}
						break;
				}
			}
		});
	}
}
