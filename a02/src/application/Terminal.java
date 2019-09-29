package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Terminal {

    public Terminal() {

    }

    /**
     * This method acts as the bash command line where the input command
     * runs in the bash terminal
     * @param command
     * @return
     */
    public static String command(String command) {

        try {

            String output = null;

            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);

            Process process = pb.start();

            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            int exitStatus = process.waitFor();

            if (exitStatus == 0) {

                String line;
                String text = "";
                while ((line = stdout.readLine()) != null) {

                    text = text + "\n" + line;
                }
                output = text;
            } else {

                String line;
                while ((line = stderr.readLine()) != null) {

                    output = line;
                }
            }

            if (output == null) {

                return "No output";
            }

            return output;
        } catch (Exception e) {

            e.printStackTrace();
        }
        return "Error";
    }
}

