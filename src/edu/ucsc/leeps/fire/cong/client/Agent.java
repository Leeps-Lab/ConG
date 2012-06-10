package edu.ucsc.leeps.fire.cong.client;

import compiler.CharSequenceCompiler;
import compiler.CharSequenceCompilerException;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffUtils;
import edu.ucsc.leeps.fire.cong.server.PricingPayoffFunction;
import edu.ucsc.leeps.fire.logging.Dialogs;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 *
 * @author jpettit
 */
public class Agent extends Thread implements Serializable {

    public transient volatile boolean running;
    public transient volatile boolean paused;
    public String agentText;
    private transient AgentScriptInterface function;

    public Agent() {
        paused = false;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            Config config = FIRE.client.getConfig();
            if (!paused && FIRE.client.isRunningPeriod() && Client.state != null && Client.state.target != null && config != null) {
                if (function != null) {
                    //Client.state.setTarget(function.act(config), config);
                } else if (config.agentSource != null) {
                    configure(config);
                } else {
                    test();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void test() {
        // send random chat messages
        if (FIRE.client.getConfig().chatroom && FIRE.client.getConfig().freeChat && FIRE.client.getRandom().nextFloat() < 0.05) {
            int n = FIRE.client.getRandom().nextInt(3);
            String s;
            switch (n) {
                case 0:
                    s = "foo";
                    break;
                case 1:
                    s = "bar";
                    break;
                case 2:
                    s = "baz";
                    break;
                default:
                    s = "def";
                    break;
            }
            FIRE.client.getServer().newMessage(s, Client.state.id);
        }
        if (Client.state.strategyChanger.isLocked()) {
            return;
        }
        // change to a random strategy
        Config config = FIRE.client.getConfig();
        if (config.payoffFunction.getNumStrategies() <= 2) {
            float newTarget;
            if (PayoffUtils.getPayoff() <= 3) {
                newTarget = FIRE.client.getRandom().nextFloat();
            } else {
                float maxPayoff = Float.NEGATIVE_INFINITY;
                float maxStrategy = 0;
                float[] strategy = new float[]{maxStrategy, 0};
                for (float s = 0; s <= 1; s += 0.01) {
                    strategy[0] = s;
                    float payoff = PayoffUtils.getPayoff(strategy);
                    if (payoff > maxPayoff) {
                        maxPayoff = payoff;
                        maxStrategy = s;
                    }
                }
                newTarget = maxStrategy + (float) (0.1 * FIRE.client.getRandom().nextGaussian());
            }
            if (config.payoffFunction instanceof PricingPayoffFunction) { //payoff function dependent
                float max = config.payoffFunction.getMax();
                float min = config.payoffFunction.getMin();
                float newPrice = min + (newTarget * (max - min));
                if (newPrice < config.marginalCost) {
                    float marginalCostTarget = config.marginalCost / (max - min);
                    newTarget = marginalCostTarget;
                }
            }
            if (newTarget >= 0 && newTarget <= 1) {
                //Client.state.setTarget(new float[]{newTarget}, config);
            }
        } else if (config.payoffFunction.getNumStrategies() == 3) {
            float maxPayoff = Float.NEGATIVE_INFINITY;
            float[] maxStrategy = new float[3];
            float[] strategy = new float[3];
            for (float s0 = 0; s0 <= 1; s0 += 0.01) {
                for (float s1 = 0; s0 + s1 <= 1; s1 += 0.01) {
                    strategy[0] = s0;
                    strategy[1] = s1;
                    strategy[2] = 1 - s0 - s1;
                    float payoff = PayoffUtils.getPayoff(strategy);
                    if (payoff > maxPayoff) {
                        maxPayoff = payoff;
                        maxStrategy[0] = strategy[0];
                        maxStrategy[1] = strategy[1];
                        maxStrategy[2] = strategy[2];
                    }
                }
            }
            float[] newTarget = new float[3];
            newTarget[0] = maxStrategy[0] + (float) (0.01 * FIRE.client.getRandom().nextGaussian());
            newTarget[1] = maxStrategy[1] + (float) (0.01 * FIRE.client.getRandom().nextGaussian());
            newTarget[2] = 1 - newTarget[0] - newTarget[1];
            if (newTarget[0] >= 0 && newTarget[0] <= 1
                    && newTarget[1] >= 0 && newTarget[1] <= 1
                    && newTarget[2] >= 0 && newTarget[2] <= 1
                    && Math.abs(1 - (newTarget[0] + newTarget[1] + newTarget[2])) <= Float.MIN_NORMAL) {
                //Client.state.setTarget(newTarget, config);
            }
        }
    }

    public DiagnosticCollector<JavaFileObject> configure(Config config) {
        DiagnosticCollector<JavaFileObject> errs = null;
        if (config.agentSource == null) {
            return errs;
        }
        if (agentText == null) {
            File baseDir = new File(FIRE.server.getConfigSource()).getParentFile();
            File scriptFile = new File(baseDir, config.agentSource);
            if (!scriptFile.exists()) {
                Dialogs.popUpErr("Error: Payoff script referenced in config does not exist.\n" + scriptFile.getAbsolutePath());
                return errs;
            }
            agentText = "";
            try {
                FileReader reader = new FileReader(scriptFile);
                int c = reader.read();
                while (c != -1) {
                    agentText += (char) c;
                    c = reader.read();
                }
            } catch (IOException ex) {
                Dialogs.popUpErr("Error reading payoff script.", ex);
            }
        }

        CharSequenceCompiler<AgentScriptInterface> compiler = new CharSequenceCompiler<AgentScriptInterface>(
                System.class.getClassLoader(), Arrays.asList(new String[]{"-target", "1.5"}));
        errs = new DiagnosticCollector<JavaFileObject>();
        Class<AgentScriptInterface> clazz = null;
        Pattern classNamePattern = Pattern.compile("public class (.*?) implements AgentScriptInterface");
        Matcher m = classNamePattern.matcher(agentText);
        if (!m.find() || m.groupCount() != 1) {
            Dialogs.popUpErr("Failed to find class name");
            return errs;
        }
        try {
            clazz = compiler.compile(m.group(1), agentText, errs, new Class<?>[]{AgentScriptInterface.class});
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
        return errs;
    }

    public static interface AgentScriptInterface {

        public float[] act(Config config);
    }
}
