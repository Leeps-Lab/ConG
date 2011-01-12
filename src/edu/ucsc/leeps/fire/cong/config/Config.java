package edu.ucsc.leeps.fire.cong.config;

import edu.ucsc.leeps.fire.cong.client.gui.Line;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.config.BaseConfig;
import edu.ucsc.leeps.fire.cong.server.CournotPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.PricingPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.PublicGoodsPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.QWERTYPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ThresholdPayoffFunction;
import java.awt.Color;

/**
 *
 * @author jpettit
 */
public class Config extends BaseConfig {

    public boolean pointsPerSecond;
    public float percentChangePerSecond;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;
    public int numTuples;
    public int tupleSize;
    public boolean assignedTuples;
    public boolean excludeSelf;
    public boolean mixedStrategySelection;
    public boolean stripStrategySelection;
    public int subperiods;
    public StrategySelectionDisplayType strategySelectionDisplayType;
    public Line yourPayoff, matchPayoff;
    public Line yourStrategy, matchStrategy;
    public Line thresholdLine;
    public String rLabel, pLabel, sLabel, shortRLabel, shortPLabel, shortSLabel;
    public Color rColor, pColor, sColor;
    public boolean showRPSSliders;
    public ShockZone shock;
    public DecisionDelay initialDelay, delay;
    public float impulse;
    public float changeCost;
    public boolean showHeatmapLegend;
    public boolean chatroom;
    public boolean negativePayoffs;
    public boolean sigmoidHeatmap;
    public float sigmoidAlpha;
    public float sigmoidBeta;
    public boolean showMatrix;
    public boolean showPayoffTimeAxisLabels;
    public float updatesPerSecond;
    public int strategyUpdateMillis;
    public boolean subperiodRematch;
    public boolean probPayoffs;
    public boolean showHeatmap;
    public static final Class bimatrix = TwoStrategyPayoffFunction.class;
    public static final Class rps = ThreeStrategyPayoffFunction.class;
    public static final Class qwerty = QWERTYPayoffFunction.class;
    public static final Class pricing = PricingPayoffFunction.class;
    public static final Class cournot = CournotPayoffFunction.class;
    public static final Class publicGoods = PublicGoodsPayoffFunction.class;
    public static final Class line = Line.class;
    public static final Class threshold = ThresholdPayoffFunction.class;
    public static final Class shockZone = ShockZone.class;
    public static final Class decisionDelay = DecisionDelay.class;
    // per-client
    public float[] initialStrategy;
    public int matchID;
    public boolean isCounterpart;
    public int playersInTuple;
    public int population, match;
    public int marginalCost;

    public Config() {
        timeConstrained = true;
        paid = true;
        length = 120;
        pointsPerSecond = false;
        percentChangePerSecond = 0.1f;
        changeCost = 0;
        subperiods = 0;
        preLength = 0;
        strategySelectionDisplayType = StrategySelectionDisplayType.Matrix;
        mixedStrategySelection = true;
        stripStrategySelection = false;
        yourPayoff = new Line();
        yourPayoff.r = 50;
        yourPayoff.g = 50;
        yourPayoff.b = 50;
        yourPayoff.alpha = 100;
        yourPayoff.weight = 2f;
        yourPayoff.visible = true;
        yourPayoff.mode = Line.Mode.Shaded;
        matchPayoff = new Line();
        matchPayoff.r = 0;
        matchPayoff.g = 0;
        matchPayoff.b = 0;
        matchPayoff.alpha = 255;
        matchPayoff.weight = 2f;
        matchPayoff.visible = true;
        matchPayoff.mode = Line.Mode.Solid;
        yourStrategy = new Line();
        yourStrategy.visible = true;
        yourStrategy.mode = Line.Mode.Solid;
        yourStrategy.r = yourPayoff.r;
        yourStrategy.g = yourPayoff.g;
        yourStrategy.b = yourPayoff.b;
        yourStrategy.alpha = 100;
        yourStrategy.weight = 2f;
        matchStrategy = new Line();
        matchStrategy.visible = true;
        matchStrategy.mode = Line.Mode.Solid;
        matchStrategy.r = matchPayoff.r;
        matchStrategy.g = matchPayoff.g;
        matchStrategy.b = matchPayoff.b;
        matchStrategy.alpha = 255;
        matchStrategy.weight = 2f;
        thresholdLine = new Line();
        thresholdLine.mode = Line.Mode.Dashed;
        thresholdLine.r = 255;
        thresholdLine.g = 170;
        thresholdLine.b = 0;
        thresholdLine.alpha = 255;
        thresholdLine.weight = 2f;
        rLabel = "A";
        pLabel = "B";
        sLabel = "C";
        shortRLabel = "A";
        shortPLabel = "B";
        shortSLabel = "C";
        rColor = new Color(255, 255, 255);
        pColor = new Color(0, 0, 0);
        sColor = new Color(150, 150, 150);
        showRPSSliders = false;
        shock = new ShockZone();
        shock.start = 0f;
        shock.end = 0f;
        shock.backfill = false;
        impulse = 0f;
        showHeatmapLegend = true;
        chatroom = false;
        negativePayoffs = false;
        sigmoidHeatmap = false;
        showHeatmap = true;
        sigmoidAlpha = 0.5f;
        showPayoffTimeAxisLabels = false;
        showMatrix = true;
        excludeSelf = false;
        tupleSize = -1;
        numTuples = -1;
        population = -1;
        match = -1;
        marginalCost = 0;
        updatesPerSecond = 1;
        strategyUpdateMillis = 100;
        probPayoffs = false;
    }
    public static final Color[] colors = new Color[]{
        new Color(7, 226, 0),
        new Color(52, 95, 255),
        new Color(247, 64, 24),
        new Color(255, 121, 0),};
}
