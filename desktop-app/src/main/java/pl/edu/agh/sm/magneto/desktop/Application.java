package pl.edu.agh.sm.magneto.desktop;


import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {
        DataReceiver dataReceiver = new DataReceiver();

        while (true) {
            dataReceiver.receiveData();
        }
    }
}
