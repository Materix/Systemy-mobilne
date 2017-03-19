package pl.edu.agh.sm.magneto.desktop.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PositionHolder {
	private static PositionHolder INSTANCE = new PositionHolder();

	private final StringProperty positionProperty;

	private PositionHolder() {
		positionProperty = new SimpleStringProperty();
	}

	public StringProperty getPositionProperty() {
		return positionProperty;
	}

	public String getPosition() {
		return positionProperty.getValue();
	}

	public void setPosition(String position) {
		positionProperty.setValue(position);
	}

	public static PositionHolder getInstance() {
		return INSTANCE;
	}
}
