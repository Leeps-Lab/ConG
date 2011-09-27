package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.logging.Dialogs;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author jpettit
 */
public class ScriptedPayoffFunction implements PayoffFunction, Serializable {

    public String script;
    private String scriptText, scriptExtension;
    private transient PayoffFunction scriptedPayoffFunction;
    private transient boolean error = false;
    private transient boolean throwError = true;

    public float getMin() {
        if (scriptedPayoffFunction == null) {
            configure();
        }
        return scriptedPayoffFunction.getMin();
    }

    public float getMax() {
        if (scriptedPayoffFunction == null) {
            configure();
        }
        return scriptedPayoffFunction.getMax();
    }

    public int getNumStrategies() {
        if (scriptedPayoffFunction == null) {
            configure();
        }
        return scriptedPayoffFunction.getNumStrategies();
    }

    public float getSubperiodBonus(int subperiod, Config config) {
        if (scriptedPayoffFunction == null) {
            configure();
        }
        return scriptedPayoffFunction.getSubperiodBonus(subperiod, config);
    }

    public float getPayoff(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies, Config config) {
        if (scriptedPayoffFunction == null) {
            configure();
        }
        if (!throwError && error) {
            return 0;
        }
        try {
            return scriptedPayoffFunction.getPayoff(id, percent, popStrategies, matchPopStrategies, config);
        } catch (Exception ex) {
            if (throwError) {
                throw new RuntimeException(ex);
            } else {
                ex.printStackTrace();
                error = true;
                return 0;
            }
        }
    }

    public float[] getPopStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        if (scriptedPayoffFunction == null) {
            configure();
        }
        return scriptedPayoffFunction.getPopStrategySummary(id, percent, popStrategies, matchPopStrategies);
    }

    public float[] getMatchStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        if (scriptedPayoffFunction == null) {
            configure();
        }
        return scriptedPayoffFunction.getMatchStrategySummary(id, percent, popStrategies, matchPopStrategies);
    }

    public void configure() {
        if (scriptText == null) {
            String source = FIRE.server.getConfigSource();
            File baseDir = new File(source).getParentFile();
            File scriptFile = new File(baseDir, script);
            if (!scriptFile.exists()) {
                Dialogs.popUpErr("Error: Payoff script referenced in config does not exist.\n" + scriptFile.getAbsolutePath());
                return;
            }
            ScriptEngineManager manager = new ScriptEngineManager();
            String[] components = scriptFile.getName().split("\\.");
            scriptExtension = components[components.length - 1];
            ScriptEngine engine = manager.getEngineByExtension(scriptExtension);
            try {
                engine.eval(new FileReader(scriptFile));
            } catch (ScriptException ex) {
                Dialogs.popUpErr("Error: Payoff script failed to run.", ex);
            } catch (FileNotFoundException ex) {
                Dialogs.popUpErr("Error: Payoff script file not found.", ex);
            }
            scriptedPayoffFunction = ((Invocable) engine).getInterface(PayoffFunction.class);
            scriptText = "";
            try {
                FileReader reader = new FileReader(scriptFile);
                int c = reader.read();
                while (c != -1) {
                    scriptText += (char) c;
                    c = reader.read();
                }
            } catch (IOException ex) {
                Dialogs.popUpErr("Error reading payoff script.", ex);
            }
        } else {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByExtension(scriptExtension);
            try {
                engine.eval(scriptText);
            } catch (ScriptException ex) {
                Dialogs.popUpErr("Error: Payoff script failed to run.", ex);
            }
            scriptedPayoffFunction = ((Invocable) engine).getInterface(PayoffFunction.class);
        }
    }

    public void setScript(String scriptText, String scriptExtension) {
        this.scriptText = scriptText;
        this.scriptExtension = scriptExtension;
        throwError = true;
        configure();
    }
}
