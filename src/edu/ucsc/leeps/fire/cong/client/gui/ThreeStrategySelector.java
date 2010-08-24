package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

public class ThreeStrategySelector extends Sprite implements Configurable<Config>, MouseListener {

    private String rLabel = "";
    private String pLabel = "";
    private String sLabel = "";
    private final int CORNER_MARKER_R = 10;
    private final int BASE_MARKER_R = 7;
    private final int R = 0;
    private final int P = 1;
    private final int S = 2;
    private float sideLength;
    private Marker rock, paper, scissors;
    private float maxDist;
    private Marker current, target, ghost, opponent;
    private float[] axisDistance;
    private float[] ghostStrat;
    private float[] targetStrat;
    // current played strategies stored here (R, P, S)
    private float[] playedStrat;
    // average of opponents' strategies
    private float[] opponentStrat;
    private Slider[] stratSlider;
    private boolean showSliders;
    private boolean mouseInTriangle;
    private boolean enabled;
    private Config config;
    private HeatmapHelper heatmap;
    private Client applet;
    public float currentPercent;
    // Markers for droplines
    private Marker rDrop, pDrop, sDrop;
    private Marker ghostRDrop, ghostPDrop, ghostSDrop;
    private StrategyChanger strategyChanger;

    public ThreeStrategySelector(
            Sprite parent, float x, float y, int width, int height,
            Client applet, StrategyChanger strategyChanger) {
        super(parent, x, y, width, height);
        this.applet = applet;
        this.strategyChanger = strategyChanger;
        mouseInTriangle = false;
        axisDistance = new float[3];
        ghostStrat = new float[3];
        targetStrat = new float[3];
        playedStrat = new float[3];
        opponentStrat = new float[3];
        for (int i = R; i <= S; i++) {
            axisDistance[i] = 0f;
            ghostStrat[i] = 0f;
            targetStrat[i] = 0f;
            playedStrat[i] = 0f;
            opponentStrat[i] = 0f;
        }

        stratSlider = new Slider[3];

        sideLength = width - 10;
        maxDist = (Client.sqrt(3) / 2f) * sideLength;

        rock = new Marker(this, 5, height / 3, true, CORNER_MARKER_R);
        rock.setLabel("R");
        rock.setLabelMode(Marker.LabelMode.Bottom);
        rock.setDrawMode(Marker.DrawMode.FillOutline);
        paper = new Marker(this, rock.origin.x + sideLength, rock.origin.y, true, CORNER_MARKER_R);
        paper.setLabel("P");
        paper.setLabelMode(Marker.LabelMode.Bottom);
        paper.setDrawMode(Marker.DrawMode.FillOutline);
        scissors = new Marker(this, rock.origin.x + sideLength / 2,
                rock.origin.y - (int) maxDist, true, CORNER_MARKER_R);
        scissors.setLabel("S");
        scissors.setLabelMode(Marker.LabelMode.Top);
        scissors.setDrawMode(Marker.DrawMode.FillOutline);

        // set up strategy markers
        current = new Marker(this, 0, 0, false, BASE_MARKER_R + 2);
        current.setColor(50, 255, 50);
        current.setLabel("$$");
        current.setLabelMode(Marker.LabelMode.Bottom);
        current.setDrawMode(Marker.DrawMode.FillOutline);
        target = new Marker(this, 0, 0, false, BASE_MARKER_R + 2);
        target.setLabel("$$");
        target.setLabelMode(Marker.LabelMode.Bottom);
        target.setDrawMode(Marker.DrawMode.Target);
        ghost = new Marker(this, 0, 0, false, BASE_MARKER_R);
        ghost.setColor(25, 255, 25, 140);
        ghost.setDrawMode(Marker.DrawMode.FillOutline);
        opponent = new Marker(this, 0, 0, false, BASE_MARKER_R);
        opponent.setColor(200, 40, 40);
        opponent.setDrawMode(Marker.DrawMode.FillOutline);

        // set up Sliders
        stratSlider[R] = new Slider(applet, this, Slider.Alignment.Horizontal, 50, width - 50, height / 3 + 50,
                Color.BLACK, rLabel, 1f);
        stratSlider[P] = new Slider(applet, this, Slider.Alignment.Horizontal, 50, width - 50, height / 3 + 100,
                Color.BLACK, pLabel, 1f);
        stratSlider[S] = new Slider(applet, this, Slider.Alignment.Horizontal, 50, width - 50, height / 3 + 150,
                Color.BLACK, sLabel, 1f);


        // set up dropline Markers
        rDrop = new Marker(this, 0, 0, true, BASE_MARKER_R);
        rDrop.setLabel("R");
        rDrop.setLabelMode(Marker.LabelMode.Right);
        rDrop.setDrawMode(Marker.DrawMode.FillOutline);

        ghostRDrop = new Marker(this, 0, 0, true, BASE_MARKER_R);
        ghostRDrop.setAlpha(150);
        ghostRDrop.setLabel("R");
        ghostRDrop.setLabelMode(Marker.LabelMode.Right);
        ghostRDrop.setDrawMode(Marker.DrawMode.FillOutline);

        pDrop = new Marker(this, 0, 0, true, BASE_MARKER_R);
        pDrop.setLabel("P");
        pDrop.setLabelMode(Marker.LabelMode.Left);
        pDrop.setDrawMode(Marker.DrawMode.FillOutline);

        ghostPDrop = new Marker(this, 0, 0, true, BASE_MARKER_R);
        ghostPDrop.setAlpha(150);
        ghostPDrop.setLabel("P");
        ghostPDrop.setLabelMode(Marker.LabelMode.Left);
        ghostPDrop.setDrawMode(Marker.DrawMode.FillOutline);

        sDrop = new Marker(this, 0, rock.origin.y, true, BASE_MARKER_R);
        sDrop.setLabel("S");
        sDrop.setLabelMode(Marker.LabelMode.Bottom);
        sDrop.setDrawMode(Marker.DrawMode.FillOutline);

        ghostSDrop = new Marker(this, 0, rock.origin.y, true, BASE_MARKER_R);
        ghostSDrop.setAlpha(150);
        ghostSDrop.setLabel("S");
        ghostSDrop.setLabelMode(Marker.LabelMode.Bottom);
        ghostSDrop.setDrawMode(Marker.DrawMode.FillOutline);

        setEnabled(false);

        applet.addMouseListener(this);
        heatmap = new HeatmapHelper(
                this, (int) (origin.x + rock.origin.x), (int) (origin.y + scissors.origin.y),
                (int) (paper.origin.x - rock.origin.x), (int) (rock.origin.y - scissors.origin.y),
                true, applet);
        heatmap.setVisible(true);
        currentPercent = 0f;

        FIRE.client.addConfigListener(this);
    }

    public void update() {
        if (visible) {
            heatmap.updateThreeStrategyHeatmap(
                    currentPercent,
                    opponentStrat[0], opponentStrat[1], opponentStrat[2],
                    this);
        }
    }

    @Override
    public synchronized void draw(Client applet) {
        if (!visible) {
            return;
        }

        heatmap.draw(applet);

        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        float mouseX = applet.mouseX - origin.x;
        float mouseY = applet.mouseY - origin.y;

        applet.stroke(0);
        applet.strokeWeight(2);
        applet.line(rock.origin.x, rock.origin.y, paper.origin.x, paper.origin.y);
        applet.line(rock.origin.x, rock.origin.y, scissors.origin.x, scissors.origin.y);
        applet.line(scissors.origin.x, scissors.origin.y, paper.origin.x, paper.origin.y);
        rock.draw(applet);
        paper.draw(applet);
        scissors.draw(applet);

        if (enabled) {
            rock.shrink();
            paper.shrink();
            scissors.shrink();
            
            if (rock.circularIsHit(applet.mouseX, applet.mouseY)) {
                rock.enlarge();
            } else if (paper.circularIsHit(applet.mouseX, applet.mouseY)) {
                paper.enlarge();
            } else if (scissors.circularIsHit(applet.mouseX, applet.mouseY)) {
                scissors.enlarge();
            }

            calculateAxisDistance(mouseX - rock.origin.x, rock.origin.y - mouseY);

            if (axisDistance[R] <= maxDist && axisDistance[R] >= 0 && axisDistance[P] <= maxDist && axisDistance[P] >= 0 && axisDistance[S] <= maxDist && axisDistance[S] >= 0) {
                mouseInTriangle = true;
            } else {
                mouseInTriangle = false;
            }

            if (mouseInTriangle) {
                applet.noCursor();
                if (current.isGrabbed()) {
                    calculateTargetStrats();
                    stratSlider[R].setGhostValue(targetStrat[R]);
                    stratSlider[P].setGhostValue(targetStrat[P]);
                    stratSlider[S].setGhostValue(targetStrat[S]);
                }
                
                calculateGhostStrats();
                ghost.setVisible(true);
                ghost.update(mouseX, mouseY);
            } else {
                if (current.isGrabbed()) {
                    current.release();
                }
                ghost.setVisible(false);
                applet.cursor();
            }

            if (!mouseInTriangle && applet.mousePressed) {
                for (int i = R; i <= S; i++) {
                    if (stratSlider[i].isGhostGrabbed()) {
                        ghost.setVisible(true);
                        if (applet.keyPressed && applet.key == Client.CODED && applet.keyCode == Client.CONTROL) {

                            float currentPos = stratSlider[i].getGhostPos();
                            if (applet.mouseX > applet.pmouseX) {
                                stratSlider[i].moveGhost(currentPos + stratSlider[i].getLength() / 300f);
                            } else if (applet.mouseX < applet.pmouseX) {
                                stratSlider[i].moveGhost(currentPos - stratSlider[i].getLength() / 300f);
                            }
                        } else {
                            stratSlider[i].moveGhost(mouseX);
                        }

                        balanceStrats(ghostStrat, i, stratSlider[i].getGhostValue());
                        switch (i) {
                            case R:
                                stratSlider[P].setGhostValue(ghostStrat[P]);
                                stratSlider[S].setGhostValue(ghostStrat[S]);
                                break;
                            case P:
                                stratSlider[R].setGhostValue(ghostStrat[R]);
                                stratSlider[S].setGhostValue(ghostStrat[S]);
                                break;
                            case S:
                                stratSlider[R].setGhostValue(ghostStrat[R]);
                                stratSlider[P].setGhostValue(ghostStrat[P]);
                                break;
                        }
                        float[] coords = calculateStratCoords(ghostStrat[R], ghostStrat[P], ghostStrat[S]);
                        ghost.update(coords[0], coords[1]);

                        break;
                    }
                }
            }
        }

        if (ghost.visible) {
            updateDropLines(ghost, ghostStrat, ghostRDrop, ghostPDrop, ghostSDrop);
            applet.strokeWeight(1);
            applet.stroke(0, 255, 255, 150);
            applet.line(ghost.origin.x, ghost.origin.y, ghostRDrop.origin.x, ghostRDrop.origin.y);
            applet.line(ghost.origin.x, ghost.origin.y, ghostPDrop.origin.x, ghostPDrop.origin.y);
            applet.line(ghost.origin.x, ghost.origin.y, ghostSDrop.origin.x, ghostSDrop.origin.y);

            ghost.setLabel(config.payoffFunction.getPayoff(currentPercent, ghostStrat, opponentStrat));

            ghostRDrop.setLabel(ghostStrat[R]);
            ghostPDrop.setLabel(ghostStrat[P]);
            ghostSDrop.setLabel(ghostStrat[S]);

            adjustLabels(ghostStrat, ghost, ghostPDrop, ghostRDrop);

            ghostRDrop.draw(applet);
            ghostPDrop.draw(applet);
            ghostSDrop.draw(applet);
        }

        ghost.draw(applet);

        if (target.visible && targetStrat != null) {
            target.setLabel(config.payoffFunction.getPayoff(currentPercent, targetStrat, opponentStrat));
            adjustLabels(targetStrat, target, null, null);
        }

        if (current.visible) {
            updateDropLines(current, playedStrat, rDrop, pDrop, sDrop);
            applet.strokeWeight(1);
            applet.stroke(0, 255, 255);
            applet.line(current.origin.x, current.origin.y, rDrop.origin.x, rDrop.origin.y);
            applet.line(current.origin.x, current.origin.y, pDrop.origin.x, pDrop.origin.y);
            applet.line(current.origin.x, current.origin.y, sDrop.origin.x, sDrop.origin.y);
        }

        current.setLabel(config.payoffFunction.getPayoff(currentPercent, playedStrat, opponentStrat));

        adjustLabels(playedStrat, current, pDrop, rDrop);

        rDrop.setLabel(playedStrat[R]);
        pDrop.setLabel(playedStrat[P]);
        sDrop.setLabel(playedStrat[S]);


        rDrop.draw(applet);
        pDrop.draw(applet);
        sDrop.draw(applet);
        target.draw(applet);
        current.draw(applet);
        opponent.draw(applet);
        for (int i = R; i <= S; i++) {
            stratSlider[i].draw(applet);
        }

        applet.popMatrix();
    }

    //@Override
    public void mouseClicked(MouseEvent e) {
    }

    //@Override
    public void mousePressed(MouseEvent e) {
        if (enabled) {
            float mouseX = e.getX() - origin.x;
            float mouseY = e.getY() - origin.y;
            if (mouseInTriangle) {
                adjustLabels(playedStrat, current, pDrop, rDrop);
                current.grab();
            } else if (rock.isEnlarged()) {
                setTargetRPS(1, 0, 0);
            } else if (paper.isEnlarged()) {
                setTargetRPS(0, 1, 0);
            } else if (scissors.isEnlarged()) {
                setTargetRPS(0, 0, 1);
            } else {
                for (int i = 0; i < stratSlider.length; i++) {
                    if (stratSlider[i].mouseOnGhost(mouseX, mouseY)) {
                        stratSlider[i].grabGhost();
                        ghost.setVisible(true);
                        ghostStrat[R] = stratSlider[R].getGhostValue();
                        ghostStrat[P] = stratSlider[P].getGhostValue();
                        ghostStrat[S] = stratSlider[S].getGhostValue();
                        break;
                    }
                }
            }
        }
    }

    //@Override
    public void mouseReleased(MouseEvent e) {
        if (mouseInTriangle) {
            if (current.isGrabbed()) {
                current.release();
            }
        } else {
            for (int i = 0; i < stratSlider.length; i++) {
                if (stratSlider[i].isGhostGrabbed()) {
                    stratSlider[i].releaseGhost();
                    balanceStrats(ghostStrat, i, stratSlider[i].getGhostValue());
                    setTargetRPS(ghostStrat[R], ghostStrat[P], ghostStrat[S]);
                    ghost.setVisible(false);
                    break;
                }
            }
        }
    }

    //@Override
    public void mouseEntered(MouseEvent e) {
    }

    //@Override
    public void mouseExited(MouseEvent e) {
    }

    public void setAllStrategies(float[] newStrats) {
        playedStrat[R] = newStrats[R];
        playedStrat[P] = newStrats[P];
        playedStrat[S] = newStrats[S];

        setTargetRPS(newStrats[R], newStrats[P], newStrats[S]);

        ghostStrat[R] = newStrats[R];
        ghostStrat[P] = newStrats[P];
        ghostStrat[S] = newStrats[S];

        for (int i = R; i <= S; i++) {
            stratSlider[i].setStratValue(playedStrat[i]);
            stratSlider[i].setGhostValue(ghostStrat[i]);
        }

        float[] coords = calculateStratCoords(newStrats[R],
                                              newStrats[P],
                                              newStrats[S]);
        current.update(coords[0], coords[1]);
    }

    public void setCurrentStrategies(float[] currentStrat) {
        playedStrat[R] = currentStrat[R];
        playedStrat[P] = currentStrat[P];
        playedStrat[S] = currentStrat[S];
        for (int i = R; i <= S; ++i) {
            stratSlider[i].setStratValue(currentStrat[i]);
        }
        float[] coords = calculateStratCoords(currentStrat[R],
                                              currentStrat[P],
                                              currentStrat[S]);
        current.update(coords[0], coords[1]);
    }

    public void setCounterpartRPS(float r, float p, float s) {
        opponentStrat[R] = r;
        opponentStrat[P] = p;
        opponentStrat[S] = s;
        float[] coords = calculateStratCoords(r, p, s);
        opponent.update(coords[0], coords[1]);
    }

    public float[] getPlayerRPS() {
        return new float[] {playedStrat[R], playedStrat[P], playedStrat[S]};
    }

    public float[] getOpponentRPS() {
        return new float[] {opponentStrat[R], opponentStrat[P], opponentStrat[S]};
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        applet.cursor();
        if (enabled) {
            current.setVisible(true);
            target.setVisible(true);
            opponent.setVisible(true);
            rDrop.setVisible(true);
            pDrop.setVisible(true);
            sDrop.setVisible(true);
            for (int i = R; i <= S; ++i) {
                stratSlider[i].showGhost();
            }
        } else {
            current.setVisible(false);
            target.setVisible(false);
            opponent.setVisible(false);
            rDrop.setVisible(false);
            pDrop.setVisible(false);
            sDrop.setVisible(false);
            for (int i = R; i <= S; ++i) {
                stratSlider[i].hideGhost();
            }
        }
    }

    public void reset() {
        setEnabled(false);
        for (int i = R; i <= S; i++) {
            axisDistance[i] = 0f;
            ghostStrat[i] = 0f;
            playedStrat[i] = 0f;
            targetStrat[i] = 0f;
            opponentStrat[i] = 0f;
            stratSlider[i].setStratValue(0f);
            stratSlider[i].hideGhost();
        }

        current.setVisible(false);
        target.setVisible(false);
        ghost.setVisible(false);
        opponent.setVisible(false);
        rDrop.setVisible(false);
        pDrop.setVisible(false);
        sDrop.setVisible(false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            applet.removeMouseListener(this);
        } else {
            applet.addMouseListener(this);
        }
    }

    // adjust the labeling mode of given marker based on strats in given
    // array. also adjust labeling mode of given droplines
    private void adjustLabels(float[] strats, Marker stratMarker,
            Marker pDropMarker, Marker rDropMarker) {
        if (strats[S] < 0.2f) {
            if (strats[R] > strats[P]) {
                stratMarker.setLabelMode(Marker.LabelMode.Right);
                if (pDropMarker != null) {
                    pDropMarker.setLabelMode(Marker.LabelMode.Top);
                }
                if (rDropMarker != null) {
                    rDropMarker.setLabelMode(Marker.LabelMode.Right);
                }
            } else {
                stratMarker.setLabelMode(Marker.LabelMode.Left);
                if (pDropMarker != null) {
                    pDropMarker.setLabelMode(Marker.LabelMode.Left);
                }
                if (rDropMarker != null) {
                    rDropMarker.setLabelMode(Marker.LabelMode.Top);
                }
            }
        } else {
            stratMarker.setLabelMode(Marker.LabelMode.Bottom);
            if (pDropMarker != null) {
                pDropMarker.setLabelMode(Marker.LabelMode.Left);
            }
            if (rDropMarker != null) {
                rDropMarker.setLabelMode(Marker.LabelMode.Right);
            }
        }
    }

    public float[] translate(float x, float y) {
        float newS = y / maxDist;

        // constant factors for determining distances
        float epsilon1 = y + (1 / Client.sqrt(3)) * x;
        float epsilon2 = y - (1 / Client.sqrt(3)) * x;

        // calculate distance from paper 3D axis
        float deltaX = x - (Client.sqrt(3) / 4) * epsilon1;
        float deltaY = y - .75f * epsilon1;
        float newP;
        if (deltaX < 0 && deltaY > 0) {
            newP = -1;
        } else {
            float distP = Client.sqrt(Client.sq(deltaX) + Client.sq(deltaY));
            newP = distP / maxDist;
        }

        // calculate distance from rock 3D axis
        deltaX = x - .75f * sideLength + (Client.sqrt(3) / 4) * epsilon2;
        deltaY = y - (Client.sqrt(3) / 4) * sideLength - .75f * epsilon2;
        float newR;
        if (deltaX > 0 && deltaY > 0) {
            newR = -1;
        } else {
            newR = 1 - newS - newP;
        }

        return new float[]{newR, newP, newS};
    }

    // calculate axisDistance entries
    /*
     * Modifies axisDistance array. Array is invalid if any of the entries
     * are -1.
     */
    private void calculateAxisDistance(float x, float y) {
        axisDistance[S] = y;

        // constant factors for determining distances
        float epsilon1 = y + (1 / Client.sqrt(3)) * x;
        float epsilon2 = y - (1 / Client.sqrt(3)) * x;

        float deltaX, deltaY;

        deltaX = x - (Client.sqrt(3) / 4) * epsilon1;
        deltaY = y - .75f * epsilon1;
        if (deltaX < 0 && deltaY > 0) {
            axisDistance[P] = -1;
        } else {
            axisDistance[P] = Client.sqrt(Client.sq(deltaX) + Client.sq(deltaY));
        }

        deltaX = x - .75f * sideLength + (Client.sqrt(3) / 4) * epsilon2;
        deltaY = y - (Client.sqrt(3) / 4) * sideLength - .75f * epsilon2;
        if (deltaX > 0 && deltaY > 0) {
            axisDistance[R] = -1;
        } else {
            axisDistance[R] = Client.sqrt(Client.sq(deltaX) + Client.sq(deltaY));
        }
    }

    // calculate ghostStrat entries
    private void calculateGhostStrats() {
        ghostStrat[S] = axisDistance[S] / maxDist;
        ghostStrat[P] = axisDistance[P] / maxDist;
        ghostStrat[R] = 1 - ghostStrat[S] - ghostStrat[P];
    }

    // calculate targetStrat entries
    private void calculateTargetStrats() {
        targetStrat[S] = axisDistance[S] / maxDist;
        targetStrat[P] = axisDistance[P] / maxDist;
        targetStrat[R] = 1 - targetStrat[S] - targetStrat[P];
        strategyChanger.setTargetStrategy(targetStrat);
        float[] coords = calculateStratCoords(targetStrat[R], targetStrat[P], targetStrat[S]);
        target.update(coords[0], coords[1]);
    }

    // calculate x, y coordinates given r, p, s values
    private float[] calculateStratCoords(float r, float p, float s) {
        float[] coords = new float[2];

        coords[0] = rock.origin.x + (maxDist * p) / Client.sin(Client.PI / 3) + maxDist * s * Client.tan(Client.PI / 6);
        coords[1] = rock.origin.y - maxDist * s;

        return coords;
    }

    // balance other strat values when using sliders
    private void balanceStrats(float[] strats, int strat, float value) {
        float pValue, deltaV, percentR, percentP, percentS;
        float newR, newP, newS;
        switch (strat) {
            case R:
                pValue = strats[R];
                deltaV = value - pValue;

                float PStotal = strats[P] + strats[S];
                if (PStotal > 0) {
                    percentP = strats[P] / PStotal;
                    percentS = strats[S] / PStotal;
                } else {
                    PStotal = 1 - value;
                    percentP = .50f;
                    percentS = .50f;
                }

                newR = value;
                newP = (PStotal - deltaV) * percentP;
                newS = 1 - newR - newP;
                break;

            case P:
                pValue = strats[P];
                deltaV = value - pValue;

                float RStotal = strats[R] + strats[S];
                if (RStotal > 0) {
                    percentR = strats[R] / RStotal;
                    percentS = strats[S] / RStotal;
                } else {
                    RStotal = 1 - value;
                    percentR = .50f;
                    percentS = .50f;
                }

                newP = value;
                newR = (RStotal - deltaV) * percentR;
                newS = 1 - newR - newP;
                break;

            case S:
                pValue = strats[S];
                deltaV = value - pValue;

                float RPtotal = strats[R] + strats[P];
                if (RPtotal > 0) {
                    percentR = strats[R] / RPtotal;
                    percentP = strats[P] / RPtotal;
                } else {
                    RPtotal = 1 - value;
                    percentR = .50f;
                    percentP = .50f;
                }

                newS = value;
                newR = (RPtotal - deltaV) * percentR;
                newP = 1 - newR - newS;
                break;

            default:
                throw new RuntimeException("RPS Error: strat value " + "out of bounds in balanceStratValues()");
        }
        strats[R] = newR;
        strats[P] = newP;
        strats[S] = newS;
    }

    private void updateDropLines(Marker stratMarker, float[] strats,
            Marker rDropMarker, Marker pDropMarker, Marker sDropMarker) {
        sDropMarker.update(stratMarker.origin.x, rock.origin.y);

        float x, y;
        x = stratMarker.origin.x - maxDist * strats[P] * Client.cos(Client.PI / 6);
        y = stratMarker.origin.y - maxDist * strats[P] * Client.sin(Client.PI / 6);
        pDropMarker.update(x, y);

        x = stratMarker.origin.x + maxDist * strats[R] * Client.cos(Client.PI / 6);
        y = stratMarker.origin.y - maxDist * strats[R] * Client.sin(Client.PI / 6);
        rDropMarker.update(x, y);
    }

    private void setTargetRPS(float targetR, float targetP, float targetS) {
        targetStrat[R] = targetR;
        targetStrat[P] = targetP;
        targetStrat[S] = targetS;
        for (int i = R; i <= S; ++i) {
            stratSlider[i].setGhostValue(targetStrat[i]);
        }
        float[] coords = calculateStratCoords(targetStrat[R], targetStrat[P], targetStrat[S]);
        target.update(coords[0], coords[1]);
        strategyChanger.setTargetStrategy(targetStrat);
    }

    public void configChanged(Config config) {
        this.config = config;
        if (config.mixedStrategySelection &&
                config.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            rLabel = config.rLabel;
            stratSlider[R].setLabel(rLabel);
            stratSlider[R].setColor(config.rColor);
            pLabel = config.pLabel;
            stratSlider[P].setLabel(pLabel);
            stratSlider[P].setColor(config.pColor);
            sLabel = config.sLabel;
            stratSlider[S].setLabel(sLabel);
            stratSlider[S].setColor(config.sColor);
            rock.setLabel(config.shortRLabel);
            rock.setColor(config.rColor);
            paper.setLabel(config.shortPLabel);
            paper.setColor(config.pColor);
            scissors.setLabel(config.shortSLabel);
            scissors.setColor(config.sColor);
            rDrop.setColor(config.rColor);
            ghostRDrop.setColor(config.rColor);
            pDrop.setColor(config.pColor);
            ghostPDrop.setColor(config.pColor);
            sDrop.setColor(config.sColor);
            ghostSDrop.setColor(config.sColor);

            for (int i = R; i <= S; ++i) {
                stratSlider[i].setVisible(config.showRPSSliders);
            }

            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
