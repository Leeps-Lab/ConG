package edu.ucsc.leeps.fire.cong.server;

import compiler.CharSequenceCompiler;
import compiler.CharSequenceCompilerException;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.logging.Dialogs;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 *
 * @author jpettit
 */
public class ScriptedPayoffFunction implements PayoffFunction, Serializable {

    public String source;
    public float min, max;
    public int strategies;
    private String scriptText;
    private transient PayoffScriptInterface function;
    private transient DiagnosticCollector<JavaFileObject> errs;

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public int getNumStrategies() {
        return strategies;
    }

    public float getSubperiodBonus(int subperiod, Config config) {
        return 0;
    }

    public float getPayoff(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies, Config config) {
        if (function == null) {
            configure();
        }
        return function.getPayoff(id, percent, popStrategies, matchPopStrategies, config);
    }

    public float[] getPopStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        return null;
    }

    public float[] getMatchStrategySummary(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies) {
        return null;
    }

    public void configure() {
        if (scriptText == null) {
            File baseDir = new File(FIRE.server.getConfigSource()).getParentFile();
            File scriptFile = new File(baseDir, source);
            if (!scriptFile.exists()) {
                Dialogs.popUpErr("Error: Payoff script referenced in config does not exist.\n" + scriptFile.getAbsolutePath());
                return;
            }
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
        }
        CharSequenceCompiler<PayoffScriptInterface> compiler = new CharSequenceCompiler<PayoffScriptInterface>(
                getClass().getClassLoader(), Arrays.asList(new String[]{"-target", "1.5"}));
        errs = new DiagnosticCollector<JavaFileObject>();
        String qualifiedName = "edu.ucsc.leeps.fire.cong.server.PayoffScript";
        String code = scriptTemplate.replace("$function", scriptText);
        Class<PayoffScriptInterface> clazz = null;
        try {
            clazz = compiler.compile(qualifiedName, code, errs, new Class<?>[]{PayoffScriptInterface.class});
        } catch (ClassCastException ex1) {
            Dialogs.popUpErr(ex1);
        } catch (CharSequenceCompilerException ex2) {
            Dialogs.popUpErr(ex2);
        }
        if (clazz != null) {
            try {
                function = clazz.newInstance();
            } catch (InstantiationException ex1) {
                Dialogs.popUpErr(ex1);
            } catch (IllegalAccessException ex2) {
                Dialogs.popUpErr(ex2);
            }
        }
    }

    public List<Diagnostic<? extends JavaFileObject>> setScript(String scriptText) {
        this.scriptText = scriptText;
        configure();
        return errs.getDiagnostics();
    }
    private static String scriptTemplate = ""
            + "package edu.ucsc.leeps.fire.cong.server;\n"
            + "import edu.ucsc.leeps.fire.cong.server.ScriptedPayoffFunction.PayoffScriptInterface;\n"
            + "import edu.ucsc.leeps.fire.cong.config.Config;\n"
            + "import java.util.*;\n"
            + "public class PayoffScript implements PayoffScriptInterface {\n"
            + "$function\n"
            + "}";

    public static interface PayoffScriptInterface {

        public float getPayoff(int id, float percent, Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies, Config config);
    }
}
