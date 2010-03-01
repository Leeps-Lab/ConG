package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import processing.core.PApplet;

public class ThreeStrategySelector extends Sprite implements MouseListener {

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
    private MovingMarker current;
    private Marker planned, opponent;
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
    private ServerInterface server;
    private ClientInterface client;
    private PApplet applet;
    private PayoffFunction payoffFunction;
    private HeatmapHelper heatmap;
    private boolean visible = false;
    public float currentPercent;
    // Markers for droplines
    private Marker rDrop, pDrop, sDrop;
    private Marker pRDrop, pPDrop, pSDrop;

    public ThreeStrategySelector(
            float x, float y, int width, int height,
            PApplet applet,
            ServerInterface server,
            ClientInterface client) {
        super(x, y, width, height);

        this.server = server;
        this.client = client;

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
        maxDist = (PApplet.sqrt(3) / 2f) * sideLength;

        rock = new Marker(5, height / 3, true, 10);
        rock.setColor(rColor);
        rock.setLabel("R");
        rock.setLabelMode(Marker.BOTTOM);
        paper = new Marker(rock.x + sideLength, rock.y, true, 10);
        paper.setColor(pColor);
        paper.setLabel("P");
        paper.setLabelMode(Marker.BOTTOM);
        scissors = new Marker(rock.x + sideLength / 2,
                rock.y - (int) maxDist, true, 10);
        scissors.setColor(sColor);
        scissors.setLabel("S");
        scissors.setLabelMode(Marker.TOP);

        // set up strategy markers
        current = new MovingMarker(0, 0, false, MARKER_RADIUS + 2, 1f);
        current.setColor(50, 255, 50);
        current.setLabel("$$");
        current.setLabelMode(Marker.BOTTOM);
        planned = new Marker(0, 0, false, MARKER_RADIUS);
        planned.setAlpha(140);
        planned.setColor(25, 255, 25);
        opponent = new Marker(0, 0, false, MARKER_RADIUS);
        opponent.setColor(200, 40, 40);

        // set up Sliders
        stratSlider[R] = new Slider(50, width - 50, height / 3 + 50,
                rColor, rLabel, 1f);
        stratSlider[P] = new Slider(50, width - 50, height / 3 + 100,
                pColor, pLabel, 1f);
        stratSlider[S] = new Slider(50, width - 50, height / 3 + 150,
                sColor, sLabel, 1f);


        // set up dropline Markers
        rDrop = new Marker(0, 0, true, MARKER_RADIUS);
        rDrop.setColor(rColor);
        rDrop.setLabel("R");
        rDrop.setLabelMode(Marker.RIGHT);

        pRDrop = new Marker(0, 0, true, MARKER_RADIUS);
        pRDrop.setColor(rColor);
        pRDrop.setAlpha(150);
        pRDrop.setLabel("R");
        pRDrop.setLabelMode(Marker.RIGHT);

        pDrop = new Marker(0, 0, true, MARKER_RADIUS);
        pDrop.setColor(pColor);
        pDrop.setLabel("P");
        pDrop.setLabelMode(Marker.LEFT);

        pPDrop = new Marker(0, 0, true, MARKER_RADIUS);
        pPDrop.setColor(pColor);
        pPDrop.setAlpha(150);
        pPDrop.setLabel("P");
        pPDrop.setLabelMode(Marker.LEFT);

        sDrop = new Marker(0, rock.y, true, MARKER_RADIUS);
        sDrop.setColor(sColor);
        sDrop.setLabel("S");
        sDrop.setLabelMode(Marker.BOTTOM);

        pSDrop = new Marker(0, rock.y, true, MARKER_RADIUS);
        pSDrop.setColor(sColor);
        pSDrop.setAlpha(150);
        pSDrop.setLabel("S");
        pSDrop.setLabelMode(Marker.BOTTOM);

        setEnabled(false);

        applet.addMouseListener(this);
        heatmap = new HeatmapHelper(applet,
                (int) (paper.x - rock.x), (int) (rock.y - scissors.y),
                0xFF0000FF, 0xFFFFFF00, 0xFF00FF00);
        heatmap.setThreeStrategySelector(this);
        currentPercent = 0f;
        this.applet = applet;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPayoffFunction(PayoffFunction payoffFunction) {
        this.payoffFunction = payoffFunction;
        heatmap.setPayoffFunction(payoffFunction);
    }

    public void update() {
        if (visible) {
            heatmap.updateThreeStrategyHeatmap(
                    currentPercent,
                    opponentStrat[0], opponentStrat[1], opponentStrat[2]);
        }
    }

    @Override
    public void draw(PApplet applet) {
        if (!visible) {
            return;
        }

        if (heatmap.getHeatmap() != null) {
            applet.image(heatmap.getHeatmap(), origin.x + rock.x, origin.y + scissors.y);
        }


        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        float mouseX = applet.mouseX - origin.x;
        float mouseY = applet.mouseY - origin.y;

        applet.stroke(0);
        applet.strokeWeight(2);
        applet.line(rock.x, rock.y, paper.x, paper.y);
        applet.line(rock.x, rock.y, scissors.x, scissors.y);
        applet.line(scissors.x, scissors.y, paper.x, paper.y);
        rock.draw(applet);
        paper.draw(applet);
        scissors.draw(applet);

        if (enabled) {
            calculateAxisDistance(mouseX - rock.x, rock.y - mouseY);

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
                    planned.show();
                    planned.update(mouseX, mouseY);
                }
            } else {
                if (current.isGrabbed()) {
                    current.release();
                }
                planned.hide();
                applet.cursor();
            }

            if (!mouseInTriangle && applet.mousePressed) {
                for (int i = R; i <= S; i++) {
                    if (stratSlider[i].isGhostGrabbed()) {
                        planned.show();
                        if (applet.keyPressed && applet.key == PApplet.CODED && applet.keyCode == PApplet.CONTROL) {

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
            applet.line(planned.x, planned.y, pRDrop.x, pRDrop.y);
            applet.line(planned.x, planned.y, pPDrop.x, pPDrop.y);
            applet.line(planned.x, planned.y, pSDrop.x, pSDrop.y);

            planned.setLabel(payoffFunction.getPayoff(currentPercent, plannedStrat, opponentStrat));

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
            applet.line(current.x, current.y, rDrop.x, rDrop.y);
            applet.line(current.x, current.y, pDrop.x, pDrop.y);
            applet.line(current.x, current.y, sDrop.x, sDrop.y);
        }

        current.setLabel(payoffFunction.getPayoff(currentPercent, playedStrat, opponentStrat));

        current.update();
        if (enabled) {
            calculatePlayedStrats(current.x - rock.x, rock.y - current.y);
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

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (enabled) {
            float mouseX = e.getX() - origin.x;
            float mouseY = e.getY() - origin.y;
            if (mouseInTriangle) {
                adjustLabels();
                current.grab();
                planned.hide();
            } else {
                for (int i = 0; i < stratSlider.length; i++) {
                    if (stratSlider[i].mouseOnGhost(mouseX, mouseY)) {
                        stratSlider[i].grabGhost();
                        planned.show();
                        balancePlannedStrats(i, stratSlider[i].getGhostValue());
                        float[] coords = calculateStratCoords(plannedStrat[R], plannedStrat[P], plannedStrat[S]);
                        planned.update(coords[0], coords[1]);
                        break;
                    }
                }
            }
        }
    }

    @Override
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
                    planned.hide();
                    break;
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
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
        current.setLocation(coords[0], coords[1]);
    }

    public void setOpponentRPS(float r, float p, float s) {
        opponentStrat[R] = r;
        opponentStrat[P] = p;
        opponentStrat[S] = s;
        float[] coords = calculateStratCoords(r, p, s);
        opponent.update(coords[0], coords[1]);
    }

    public float[] getPlayerRPS() {
        return playedStrat;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            current.show();
            opponent.show();
            rDrop.show();
            pDrop.show();
            sDrop.show();
            for (int i = R; i <= S; ++i) {
                stratSlider[i].showGhost();
            }
        } else {
            current.hide();
            opponent.hide();
            rDrop.hide();
            pDrop.hide();
            sDrop.hide();
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

        current.hide();
        planned.hide();
        opponent.hide();
        rDrop.hide();
        pDrop.hide();
        sDrop.hide();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setRateOfChange(float rate) {
        current.setSpeed(rate);
    }

    // adjust the labeling modes of the Markers depending on the
    // current strategies being played.
    private void adjustLabels() {
        if (playedStrat[S] < 0.2f) {
            if (playedStrat[R] > playedStrat[P]) {
                current.setLabelMode(Marker.RIGHT);
                pDrop.setLabelMode(Marker.TOP);
                rDrop.setLabelMode(Marker.RIGHT);
            } else {
                current.setLabelMode(Marker.LEFT);
                pDrop.setLabelMode(Marker.LEFT);
                rDrop.setLabelMode(Marker.TOP);
            }
        } else {
            current.setLabelMode(Marker.BOTTOM);
            pDrop.setLabelMode(Marker.LEFT);
            rDrop.setLabelMode(Marker.RIGHT);
        }
    }

    private void adjustPlannedLabels() {
        if (plannedStrat[S] < 0.2f) {
            if (plannedStrat[R] > plannedStrat[P]) {
                planned.setLabelMode(Marker.RIGHT);
                pPDrop.setLabelMode(Marker.TOP);
                pRDrop.setLabelMode(Marker.RIGHT);
            } else {
                planned.setLabelMode(Marker.LEFT);
                pPDrop.setLabelMode(Marker.LEFT);
                pRDrop.setLabelMode(Marker.TOP);
            }
        } else {
            planned.setLabelMode(Marker.BOTTOM);
            pPDrop.setLabelMode(Marker.LEFT);
            pRDrop.setLabelMode(Marker.RIGHT);
        }
    }

    // calculate playedR, playedP, playedS from x, y
    private void calculatePlayedStrats(float x, float y) {
        float newS = y / maxDist;

        // constant factor for determining distance
        float epsilon = y + (1 / PApplet.sqrt(3)) * x;

        // calculate distance from paper 3D axis
        float deltaX = x - (PApplet.sqrt(3) / 4) * epsilon;
        float deltaY = y - .75f * epsilon;
        float distP = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));

        float newP = distP / maxDist;

        float newR = 1 - newS - newP;

        playedStrat[R] = newR;
        playedStrat[P] = newP;
        playedStrat[S] = newS;

        for (int i = R; i <= S; i++) {
            stratSlider[i].setStratValue(playedStrat[i]);
        }

        server.strategyChanged(client.getFullName());
    }

    public float[] translate(float x, float y) {
        float newS = y / maxDist;

        // constant factors for determining distances
        float epsilon1 = y + (1 / PApplet.sqrt(3)) * x;
        float epsilon2 = y - (1 / PApplet.sqrt(3)) * x;

        // calculate distance from paper 3D axis
        float deltaX = x - (PApplet.sqrt(3) / 4) * epsilon1;
        float deltaY = y - .75f * epsilon1;
        float newP;
        if (deltaX < 0 && deltaY > 0) {
            newP = -1;
        } else {
            float distP = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));
            newP = distP / maxDist;
        }

        // calculate distance from rock 3D axis
        deltaX = x - .75f * sideLength + (PApplet.sqrt(3) / 4) * epsilon2;
        deltaY = y - (PApplet.sqrt(3) / 4) * sideLength - .75f * epsilon2;
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
        float epsilon1 = y + (1 / PApplet.sqrt(3)) * x;
        float epsilon2 = y - (1 / PApplet.sqrt(3)) * x;

        float deltaX, deltaY;

        deltaX = x - (PApplet.sqrt(3) / 4) * epsilon1;
        deltaY = y - .75f * epsilon1;
        if (deltaX < 0 && deltaY > 0) {
            axisDistance[P] = -1;
        } else {
            axisDistance[P] = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));
        }

        deltaX = x - .75f * sideLength + (PApplet.sqrt(3) / 4) * epsilon2;
        deltaY = y - (PApplet.sqrt(3) / 4) * sideLength - .75f * epsilon2;
        if (deltaX > 0 && deltaY > 0) {
            axisDistance[R] = -1;
        } else {
            axisDistance[R] = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));
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
    }

    // calculate x, y coordinates given r, p, s values
    private float[] calculateStratCoords(float r, float p, float s) {
        float[] coords = new float[2];

        coords[0] = rock.x + (maxDist * p) / PApplet.sin(PApplet.PI / 3) + maxDist * s * PApplet.tan(PApplet.PI / 6);
        coords[1] = rock.y - maxDist * s;

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
        sDrop.update(current.x, rock.y);

        float x, y;
        x = current.x - maxDist * playedStrat[P] * PApplet.cos(PApplet.PI / 6);
        y = current.y - maxDist * playedStrat[P] * PApplet.sin(PApplet.PI / 6);
        pDrop.update(x, y);

        x = current.x + maxDist * playedStrat[R] * PApplet.cos(PApplet.PI / 6);
        y = current.y - maxDist * playedStrat[R] * PApplet.sin(PApplet.PI / 6);
        rDrop.update(x, y);
    }

    private void updatePlannedDropLines() {
        pSDrop.update(planned.x, rock.y);

        float x, y;
        x = planned.x - maxDist * plannedStrat[P] * PApplet.cos(PApplet.PI / 6);
        y = planned.y - maxDist * plannedStrat[P] * PApplet.sin(PApplet.PI / 6);
        pPDrop.update(x, y);

        x = planned.x + maxDist * plannedStrat[R] * PApplet.cos(PApplet.PI / 6);
        y = planned.y - maxDist * plannedStrat[R] * PApplet.sin(PApplet.PI / 6);
        pRDrop.update(x, y);
    }

    private void setTargetRPS (float targetR, float targetP, float targetS) {
        targetStrat[R] = targetR;
        targetStrat[P] = targetP;
        targetStrat[S] = targetS;
        for (int i = R; i <= S; ++i) {
            stratSlider[i].setGhostValue(targetStrat[i]);
        }
    }

    private void setPlannedRPS (float plannedR, float plannedP, float plannedS) {
        plannedStrat[R] = plannedR;
        plannedStrat[P] = plannedP;
        plannedStrat[S] = plannedS;
    }
}
