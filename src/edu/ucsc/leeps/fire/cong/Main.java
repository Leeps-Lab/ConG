package edu.ucsc.leeps.fire.cong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author jpettit
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Runtime runtime = Runtime.getRuntime();
        Process server = runtime.exec(new String[]{"java", "-ea", "-jar", "dist/Server.jar"});
        Process client1 = runtime.exec(new String[]{"java", "-ea", "-jar", "dist/Client.jar"});
        Process client2 = runtime.exec(new String[]{"java", "-ea", "-jar", "dist/Client.jar"});
        runtime.addShutdownHook(new ShutdownThread(server));
        runtime.addShutdownHook(new ShutdownThread(client1));
        runtime.addShutdownHook(new ShutdownThread(client2));
        new OutputRedirectThread(server.getErrorStream()).start();
        new OutputRedirectThread(client1.getErrorStream()).start();
        new OutputRedirectThread(client2.getErrorStream()).start();
        server.waitFor();
        client1.waitFor();
        client2.waitFor();
    }

    private static class OutputRedirectThread extends Thread {

        private InputStream is;

        public OutputRedirectThread(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {
                    System.err.println(line);
                    line = reader.readLine();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class ShutdownThread extends Thread {

        private Process process;

        public ShutdownThread(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            process.destroy();
        }
    }
}
