package br.com.catolicapb.facerecoptm.connection;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionToRaspberry {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final boolean isRaspOn = true; //se for executar o código com o rasp altere para true

    public static void sendHttpRequest(String command) {
        if (isRaspOn) {
            executor.submit(() -> {
                try {
                    URL url = new URL("http://192.168.2.111:5000/led");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(("action=" + command).getBytes());
                        os.flush();
                    }
                    int responseCode = connection.getResponseCode();
                    //System.out.println("Response Code: " + responseCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void sendLedCommand(String command) {
        sendHttpRequest(command);
    }
}
