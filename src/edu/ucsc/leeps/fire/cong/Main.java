package edu.ucsc.leeps.fire.cong;

import edu.ucsc.leeps.fire.testing.NumSubjectsInput;
import edu.ucsc.leeps.fire.testing.Startup;

/**
 *
 * @author jpettit
 */
public class Main {

    public static void main(String[] args) {
        NumSubjectsInput dialog = new NumSubjectsInput(null, true);
        dialog.setVisible(true);
        try {
            int numSubjects = Integer.parseInt(dialog.numSubjectsInput.getValue().toString());
            if (numSubjects > 0) {
                Startup.main(args, numSubjects);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            System.err.println("Illegal number of subjects");
            System.exit(1);
        }
    }
}
