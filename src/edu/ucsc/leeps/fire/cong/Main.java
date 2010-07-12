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
        startClient();
        startClient();
        startClient();
        startClient();
        startServer().waitFor();
    }

    private static Process startServer() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process p = runtime.exec(new String[]{"java", "-ea", "-jar", "dist/Server.jar"});
        runtime.addShutdownHook(new ShutdownThread(p));
        new OutputRedirectThread(p.getErrorStream()).start();
        return p;
    }

    private static Process startClient() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process p = runtime.exec(new String[]{"java", "-ea", "-jar", "dist/Client.jar"});
        runtime.addShutdownHook(new ShutdownThread(p));
        new OutputRedirectThread(p.getErrorStream()).start();
        return p;
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
