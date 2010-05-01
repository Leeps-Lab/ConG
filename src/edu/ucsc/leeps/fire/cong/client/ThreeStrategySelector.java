package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import edu.ucsc.leeps.fire.server.PeriodConfigurable;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

public class ThreeStrategySelector extends Sprite implements PeriodConfigurable, MouseListener {

    public String rLabel = "Rock";
    public String pLabel = "Paper";
    public String sLabel = "Scissors";
    private final int MARKER_RADIUS = 7;
    private final int R = 0;
    private final int P = 1;
    private final int S = 2;
    private float sideLength;
    private Marker rock, paper, scissors;
    private float maxDist;
    private Marker current, planned, opponent;
    private float[] axisDistance;
    private float[] plannedStrat;
    private float[] targetStrat;
    // current played strategies stored here (R, P, S)
    private float[] playedStrat;
    // average of opponents' strategies
    private float[] opponentStrat;
    private Slider[] stratSlider;
    private boolean mouseInTriangle;
    private Color rColor, pColor, sColor;
    private boolean enabled;
    private PeriodConfig periodConfig;
    private HeatmapHelper heatmap;
    private PEmbed applet;
    public float currentPercent;
    // Markers for droplines
    private Marker rDrop, pDrop, sDrop;
    private Marker pRDrop, pPDrop, pSDrop;
    private StrategyChanger strategyChanger;

    public ThreeStrategySelector(
            float x, float y, int width, int height,
            PEmbed applet, StrategyChanger strategyChanger) {
        super(x, y, width, height);
        this.applet = applet;
        this.strategyChanger = strategyChanger;
        mouseInTriangle = false;
        axisDistance = new float[3];
        plannedStrat = new float[3];
        targetStrat = new float[3];
        playedStrat = new float[3];
        opponentStrat = new float[3];
        for (int i = R; i <= S; i++) {
            axisDistance[i] = 0f;
            plannedStrat[i] = 0f;
            targetStrat[i] = 0f;
            playedStrat[i] = 0f;
            opponentStrat[i] = 0f;
        }

        stratSlider = new Slider[3];

        rColor = new Color(255, 25, 25);
        pColor = new Color(25, 25, 255);
        sColor = new Color(255, 0, 255);

        sideLength = width - 10;
        maxDist = (PEmbed.sqrt(3) / 2f) * sideLength;

        rock = new Marker(this, 5, height / 3, true, 10);
        rock.setColor(rColor);
        rock.setLabel("R");
        rock.setLabelMode(Marker.LabelMode.Bottom);
        paper = new Marker(this, rock.origin.x + sideLength, rock.origin.y, true, 10);
        paper.setColor(pColor);
        paper.setLabel("P");
        paper.setLabelMode(Marker.LabelMode.Bottom);
        scissors = new Marker(this, rock.origin.x + sideLength / 2,
                rock.origin.y - (int) maxDist, true, 10);
        scissors.setColor(sColor);
        scissors.setLabel("S");
        scissors.setLabelMode(Marker.LabelMode.Top);

        // set up strategy markers
        current = new Marker(this, 0, 0, false, MARKER_RADIUS + 2);
        current.setColor(50, 255, 50);
        current.setLabel("$$");
        current.setLabelMode(Marker.LabelMode.Bottom);
        planned = new Marker(this, 0, 0, false, MARKER_RADIUS);
        planned.setColor(25, 255, 25, 140);
        opponent = new Marker(this, 0, 0, false, MARKER_RADIUS);
        opponent.setColor(200, 40, 40);

        // set up Sliders
        stratSlider[R] = new Slider(this, 50, width - 50, height / 3 + 50,
                rColor, rLabel, 1f);
        stratSlider[P] = new Slider(this, 50, width - 50, height / 3 + 100,
                pColor, pLabel, 1f);
        stratSlider[S] = new Slider(this, 50, width - 50, height / 3 + 150,
                sColor, sLabel, 1f);


        // set up dropline Markers
        rDrop = new Marker(this, 0, 0, true, MARKER_RADIUS);
        rDrop.setColor(rColor);
        rDrop.setLabel("R");
        rDrop.setLabelMode(Marker.LabelMode.Right);

        pRDrop = new Marker(this, 0, 0, true, MARKER_RADIUS);
        pRDrop.setColor(rColor);
        pRDrop.setAlpha(150);
        pRDrop.setLabel("R");
        pRDrop.setLabelMode(Marker.LabelMode.Right);

        pDrop = new Marker(this, 0, 0, true, MARKER_RADIUS);
        pDrop.setColor(pColor);
        pDrop.setLabel("P");
        pDrop.setLabelMode(Marker.LabelMode.Left);

        pPDrop = new Marker(this, 0, 0, true, MARKER_RADIUS);
        pPDrop.setColor(pColor);
        pPDrop.setAlpha(150);
        pPDrop.setLabel("P");
        pPDrop.setLabelMode(Marker.LabelMode.Left);

        sDrop = new Marker(this, 0, rock.origin.y, true, MARKER_RADIUS);
        sDrop.setColor(sColor);
        sDrop.setLabel("S");
        sDrop.setLabelMode(Marker.LabelMode.Bottom);

        pSDrop = new Marker(this, 0, rock.origin.y, true, MARKER_RADIUS);
        pSDrop.setColor(sColor);
        pSDrop.setAlpha(150);
        pSDrop.setLabel("S");
        pSDrop.setLabelMode(Marker.LabelMode.Bottom);

        setEnabled(false);

        applet.addMouseListener(this);
        heatmap = new HeatmapHelper(
                (int) (origin.x + rock.origin.x), (int) (origin.y + scissors.origin.y),
                (int) (paper.origin.x - rock.origin.x), (int) (rock.origin.y - scissors.origin.y),
                true, applet);
        heatmap.setVisible(true);
        currentPercent = 0f;
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
    public synchronized void draw(PEmbed applet) {
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
            calculateAxisDistance(mouseX - rock.origin.x, rock.origin.y - mouseY);

            if (axisDistance[R] <= maxDist && axisDistance[R] >= 0 && axisDistance[P] <= maxDist && axisDistance[P] >= 0 && axisDistance[S] <= maxDist && axisDistance[S] >= 0) {
                mouseInTriangle = true;
            } else {
                mouseInTriangle = false;
            }

            if (mouseInTriangle) {
                applet.noCursor();
                if (current.isGrabbed()) {
                    current.update(mouseX, mouseY);
                    calculateTargetStrats();
                    stratSlider[R].setGhostValue(targetStrat[R]);
                    stratSlider[P].setGhostValue(targetStrat[P]);
                    stratSlider[S].setGhostValue(targetStrat[S]);
                } else {
                    calculatePlannedStrats();
                    planned.setVisible(true);
                    planned.update(mouseX, mouseY);
                }
            } else {
                if (current.isGrabbed()) {
                    current.release();
                }
                planned.setVisible(false);
                applet.cursor();
            }

            if (!mouseInTriangle && applet.mousePressed) {
                for (int i = R; i <= S; i++) {
                    if (stratSlider[i].isGhostGrabbed()) {
                        planned.setVisible(true);
                        if (applet.keyPressed && applet.key == PEmbed.CODED && applet.keyCode == PEmbed.CONTROL) {

                            float currentPos = stratSlider[i].getGhostPos();
                            if (applet.mouseX > applet.pmouseX) {
                                stratSlider[i].moveGhost(currentPos + stratSlider[i].getLength() / 300f);
                            } else if (applet.mouseX < applet.pmouseX) {
                                stratSlider[i].moveGhost(currentPos - stratSlider[i].getLength() / 300f);
                            }
                        } else {
                            stratSlider[i].moveGhost(mouseX);
                        }

                        balancePlannedStrats(i, stratSlider[i].getGhostValue());
                        float[] coords = calculateStratCoords(plannedStrat[R], plannedStrat[P], plannedStrat[S]);
                        planned.update(coords[0], coords[1]);

                        adjustLabels();

                        break;
                    }
                }
            }
        }

        if (planned.visible) {
            updatePlannedDropLines();
            applet.strokeWeight(1);
            applet.stroke(0, 255, 255, 150);
            applet.line(planned.origin.x, planned.origin.y, pRDrop.origin.x, pRDrop.origin.y);
            applet.line(planned.origin.x, planned.origin.y, pPDrop.origin.x, pPDrop.origin.y);
            applet.line(planned.origin.x, planned.origin.y, pSDrop.origin.x, pSDrop.origin.y);

            planned.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, plannedStrat, opponentStrat));

            pRDrop.setLabel(plannedStrat[R]);
            pPDrop.setLabel(plannedStrat[P]);
            pSDrop.setLabel(plannedStrat[S]);

            adjustPlannedLabels();

            pRDrop.draw(applet);
            pPDrop.draw(applet);
            pSDrop.draw(applet);
        }

        planned.draw(applet);

        if (current.visible) {
            updateCurrentDropLines();
            applet.strokeWeight(1);
            applet.stroke(0, 255, 255);
            applet.line(current.origin.x, current.origin.y, rDrop.origin.x, rDrop.origin.y);
            applet.line(current.origin.x, current.origin.y, pDrop.origin.x, pDrop.origin.y);
            applet.line(current.origin.x, current.origin.y, sDrop.origin.x, sDrop.origin.y);
        }

        current.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, playedStrat, opponentStrat));

        if (enabled) {
            calculatePlayedStrats(current.origin.x - rock.origin.x, rock.origin.y - current.origin.y);
        }
        adjustLabels();

        rDrop.setLabel(playedStrat[R]);
        pDrop.setLabel(playedStrat[P]);
        sDrop.setLabel(playedStrat[S]);


        rDrop.draw(applet);
        pDrop.draw(applet);
        sDrop.draw(applet);
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
                adjustLabels();
                current.grab();
                planned.setVisible(false);
            } else {
                for (int i = 0; i < stratSlider.length; i++) {
                    if (stratSlider[i].mouseOnGhost(mouseX, mouseY)) {
                        stratSlider[i].grabGhost();
                        planned.setVisible(true);
                        balancePlannedStrats(i, stratSlider[i].getGhostValue());
                        float[] coords = calculateStratCoords(plannedStrat[R], plannedStrat[P], plannedStrat[S]);
                        planned.update(coords[0], coords[1]);
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
                    balanceTargetStrats(i, stratSlider[i].getGhostValue());
                    float[] coords = calculateStratCoords(targetStrat[R], targetStrat[P], targetStrat[S]);
                    current.update(coords[0], coords[1]);
                    planned.setVisible(false);
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

    public void setPlayerRPS(float newR, float newP, float newS) {
        playedStrat[R] = newR;
        playedStrat[P] = newP;
        playedStrat[S] = newS;

        setTargetRPS(newR, newP, newS);

        for (int i = R; i <= S; i++) {
            stratSlider[i].setStratValue(playedStrat[i]);
        }

        float[] coords = calculateStratCoords(newR, newP, newS);
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
        return playedStrat;
    }

    public float[] getOpponentRPS() {
        return opponentStrat;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            current.setVisible(true);
            opponent.setVisible(true);
            rDrop.setVisible(true);
            pDrop.setVisible(true);
            sDrop.setVisible(true);
            for (int i = R; i <= S; ++i) {
                stratSlider[i].showGhost();
            }
        } else {
            current.setVisible(false);
            opponent.setVisible(false);
            rDrop.setVisible(false);
            pDrop.setVisible(false);
            sDrop.setVisible(false);
            for (int i = R; i <= S; ++i) {
                stratSlider[i].hideGhost();
            }
        }
    }

    public void pause() {
        setEnabled(false);
    }

    public void reset() {
        setEnabled(false);
        for (int i = R; i <= S; i++) {
            axisDistance[i] = 0f;
            plannedStrat[i] = 0f;
            playedStrat[i] = 0f;
            opponentStrat[i] = 0f;
            stratSlider[i].setStratValue(0f);
            stratSlider[i].hideGhost();
        }

        current.setVisible(false);
        planned.setVisible(false);
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

    // adjust the labeling modes of the Markers depending on the
    // current strategies being played.
    private void adjustLabels() {
        if (playedStrat[S] < 0.2f) {
            if (playedStrat[R] > playedStrat[P]) {
                current.setLabelMode(Marker.LabelMode.Right);
                pDrop.setLabelMode(Marker.LabelMode.Top);
                rDrop.setLabelMode(Marker.LabelMode.Right);
            } else {
                current.setLabelMode(Marker.LabelMode.Left);
                pDrop.setLabelMode(Marker.LabelMode.Left);
                rDrop.setLabelMode(Marker.LabelMode.Top);
            }
        } else {
            current.setLabelMode(Marker.LabelMode.Bottom);
            pDrop.setLabelMode(Marker.LabelMode.Left);
            rDrop.setLabelMode(Marker.LabelMode.Right);
        }
    }

    private void adjustPlannedLabels() {
        if (plannedStrat[S] < 0.2f) {
            if (plannedStrat[R] > plannedStrat[P]) {
                planned.setLabelMode(Marker.LabelMode.Right);
                pPDrop.setLabelMode(Marker.LabelMode.Top);
                pRDrop.setLabelMode(Marker.LabelMode.Right);
            } else {
                planned.setLabelMode(Marker.LabelMode.Left);
                pPDrop.setLabelMode(Marker.LabelMode.Left);
                pRDrop.setLabelMode(Marker.LabelMode.Top);
            }
        } else {
            planned.setLabelMode(Marker.LabelMode.Bottom);
            pPDrop.setLabelMode(Marker.LabelMode.Left);
            pRDrop.setLabelMode(Marker.LabelMode.Right);
        }
    }

    // calculate playedR, playedP, playedS from x, y
    private void calculatePlayedStrats(float x, float y) {
        float newS = y / maxDist;

        // constant factor for determining distance
        float epsilon = y + (1 / PEmbed.sqrt(3)) * x;

        // calculate distance from paper 3D axis
        float deltaX = x - (PEmbed.sqrt(3) / 4) * epsilon;
        float deltaY = y - .75f * epsilon;
        float distP = PEmbed.sqrt(PEmbed.sq(deltaX) + PEmbed.sq(deltaY));

        float newP = distP / maxDist;

        float newR = 1 - newS - newP;

        playedStrat[R] = newR;
        playedStrat[P] = newP;
        playedStrat[S] = newS;

        for (int i = R; i <= S; i++) {
            stratSlider[i].setStratValue(playedStrat[i]);
        }

        //server.strategyChanged(client.getID());
    }

    public float[] translate(float x, float y) {
        float newS = y / maxDist;

        // constant factors for determining distances
        float epsilon1 = y + (1 / PEmbed.sqrt(3)) * x;
        float epsilon2 = y - (1 / PEmbed.sqrt(3)) * x;

        // calculate distance from paper 3D axis
        float deltaX = x - (PEmbed.sqrt(3) / 4) * epsilon1;
        float deltaY = y - .75f * epsilon1;
        float newP;
        if (deltaX < 0 && deltaY > 0) {
            newP = -1;
        } else {
            float distP = PEmbed.sqrt(PEmbed.sq(deltaX) + PEmbed.sq(deltaY));
            newP = distP / maxDist;
        }

        // calculate distance from rock 3D axis
        deltaX = x - .75f * sideLength + (PEmbed.sqrt(3) / 4) * epsilon2;
        deltaY = y - (PEmbed.sqrt(3) / 4) * sideLength - .75f * epsilon2;
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
        float epsilon1 = y + (1 / PEmbed.sqrt(3)) * x;
        float epsilon2 = y - (1 / PEmbed.sqrt(3)) * x;

        float deltaX, deltaY;

        deltaX = x - (PEmbed.sqrt(3) / 4) * epsilon1;
        deltaY = y - .75f * epsilon1;
        if (deltaX < 0 && deltaY > 0) {
            axisDistance[P] = -1;
        } else {
            axisDistance[P] = PEmbed.sqrt(PEmbed.sq(deltaX) + PEmbed.sq(deltaY));
        }

        deltaX = x - .75f * sideLength + (PEmbed.sqrt(3) / 4) * epsilon2;
        deltaY = y - (PEmbed.sqrt(3) / 4) * sideLength - .75f * epsilon2;
        if (deltaX > 0 && deltaY > 0) {
            axisDistance[R] = -1;
        } else {
            axisDistance[R] = PEmbed.sqrt(PEmbed.sq(deltaX) + PEmbed.sq(deltaY));
        }
    }

    // calculate plannedStrat entries
    private void calculatePlannedStrats() {
        plannedStrat[S] = axisDistance[S] / maxDist;
        plannedStrat[P] = axisDistance[P] / maxDist;
        plannedStrat[R] = 1 - plannedStrat[S] - plannedStrat[P];
    }

    // calculate targetStrat entries
    private void calculateTargetStrats() {
        targetStrat[S] = axisDistance[S] / maxDist;
        targetStrat[P] = axisDistance[P] / maxDist;
        targetStrat[R] = 1 - targetStrat[S] - targetStrat[P];
        strategyChanger.setTargetStrategy(targetStrat);
    }

    // calculate x, y coordinates given r, p, s values
    private float[] calculateStratCoords(float r, float p, float s) {
        float[] coords = new float[2];

        coords[0] = rock.origin.x + (maxDist * p) / PEmbed.sin(PEmbed.PI / 3) + maxDist * s * PEmbed.tan(PEmbed.PI / 6);
        coords[1] = rock.origin.y - maxDist * s;

        return coords;
    }

    // balance other strat values when using sliders
    private void balanceTargetStrats(int strat, float value) {
        float pValue, deltaV, percentR, percentP, percentS;
        float newR, newP, newS;
        switch (strat) {
            case R:
                pValue = targetStrat[R];
                deltaV = value - pValue;

                float PStotal = targetStrat[P] + targetStrat[S];
                if (PStotal > 0) {
                    percentP = targetStrat[P] / PStotal;
                    percentS = targetStrat[S] / PStotal;
                } else {
                    PStotal = 1 - value;
                    percentP = .50f;
                    percentS = .50f;
                }

                newR = value;
                newP = (PStotal - deltaV) * percentP;
                newS = 1 - newR - newP;
                setTargetRPS(newR, newP, newS);
                break;

            case P:
                pValue = targetStrat[P];
                deltaV = value - pValue;

                float RStotal = targetStrat[R] + targetStrat[S];
                if (RStotal > 0) {
                    percentR = targetStrat[R] / RStotal;
                    percentS = targetStrat[S] / RStotal;
                } else {
                    RStotal = 1 - value;
                    percentR = .50f;
                    percentS = .50f;
                }

                newP = value;
                newR = (RStotal - deltaV) * percentR;
                newS = 1 - newR - newP;
                setTargetRPS(newR, newP, newS);
                break;

            case S:
                pValue = targetStrat[S];
                deltaV = value - pValue;

                float RPtotal = targetStrat[R] + targetStrat[P];
                if (RPtotal > 0) {
                    percentR = targetStrat[R] / RPtotal;
                    percentP = targetStrat[P] / RPtotal;
                } else {
                    RPtotal = 1 - value;
                    percentR = .50f;
                    percentP = .50f;
                }

                newS = value;
                newR = (RPtotal - deltaV) * percentR;
                newP = 1 - newR - newS;
                setTargetRPS(newR, newP, newS);
                break;

            default:
                throw new RuntimeException("RPS Error: strat value " + "out of bounds in balanceStratValues()");
        }
    }

    private void balancePlannedStrats(int strat, float value) {
        float pValue, deltaV, percentR, percentP, percentS;
        float newR, newP, newS;
        switch (strat) {
            case R:
                pValue = plannedStrat[R];
                deltaV = value - pValue;

                float PStotal = plannedStrat[P] + plannedStrat[S];
                if (PStotal > 0) {
                    percentP = plannedStrat[P] / PStotal;
                    percentS = plannedStrat[S] / PStotal;
                } else {
                    PStotal = 1 - value;
                    percentP = .50f;
                    percentS = .50f;
                }

                newR = value;
                newP = (PStotal - deltaV) * percentP;
                newS = 1 - newR - newP;
                setPlannedRPS(newR, newP, newS);
                break;

            case P:
                pValue = plannedStrat[P];
                deltaV = value - pValue;

                float RStotal = plannedStrat[R] + plannedStrat[S];
                if (RStotal > 0) {
                    percentR = plannedStrat[R] / RStotal;
                    percentS = plannedStrat[S] / RStotal;
                } else {
                    RStotal = 1 - value;
                    percentR = .50f;
                    percentS = .50f;
                }

                newP = value;
                newR = (RStotal - deltaV) * percentR;
                newS = 1 - newR - newP;
                setPlannedRPS(newR, newP, newS);
                break;

            case S:
                pValue = plannedStrat[S];
                deltaV = value - pValue;

                float RPtotal = plannedStrat[R] + plannedStrat[P];
                if (RPtotal > 0) {
                    percentR = plannedStrat[R] / RPtotal;
                    percentP = plannedStrat[P] / RPtotal;
                } else {
                    RPtotal = 1 - value;
                    percentR = .50f;
                    percentP = .50f;
                }

                newS = value;
                newR = (RPtotal - deltaV) * percentR;
                newP = 1 - newR - newS;
                setPlannedRPS(newR, newP, newS);
                break;

            default:
                throw new RuntimeException("RPS Error: strat value " + "out of bounds in balanceStratValues()");
        }
    }

    private void updateCurrentDropLines() {
        sDrop.update(current.origin.x, rock.origin.y);

        float x, y;
        x = current.origin.x - maxDist * playedStrat[P] * PEmbed.cos(PEmbed.PI / 6);
        y = current.origin.y - maxDist * playedStrat[P] * PEmbed.sin(PEmbed.PI / 6);
        pDrop.update(x, y);

        x = current.origin.x + maxDist * playedStrat[R] * PEmbed.cos(PEmbed.PI / 6);
        y = current.origin.y - maxDist * playedStrat[R] * PEmbed.sin(PEmbed.PI / 6);
        rDrop.update(x, y);
    }

    private void updatePlannedDropLines() {
        pSDrop.update(planned.origin.x, rock.origin.y);

        float x, y;
        x = planned.origin.x - maxDist * plannedStrat[P] * PEmbed.cos(PEmbed.PI / 6);
        y = planned.origin.y - maxDist * plannedStrat[P] * PEmbed.sin(PEmbed.PI / 6);
        pPDrop.update(x, y);

        x = planned.origin.x + maxDist * plannedStrat[R] * PEmbed.cos(PEmbed.PI / 6);
        y = planned.origin.y - maxDist * plannedStrat[R] * PEmbed.sin(PEmbed.PI / 6);
        pRDrop.update(x, y);
    }

    private void setTargetRPS(float targetR, float targetP, float targetS) {
        targetStrat[R] = targetR;
        targetStrat[P] = targetP;
        targetStrat[S] = targetS;
        for (int i = R; i <= S; ++i) {
            stratSlider[i].setGhostValue(targetStrat[i]);
        }
    }

    private void setPlannedRPS(float plannedR, float plannedP, float plannedS) {
        plannedStrat[R] = plannedR;
        plannedStrat[P] = plannedP;
        plannedStrat[S] = plannedS;
    }

    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        periodConfig = (PeriodConfig) basePeriodConfig;
        if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            heatmap.setPeriodConfig(periodConfig);
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
