package edu.ucsc.leeps.fire.cong.client;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import processing.core.PApplet;

public class RPSDisplay extends Sprite implements MouseListener {

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
    private Marker current, planned;
    private float[] plannedDist;
    private float[] plannedStrat;

    // current played strategies stored here (R, P, S)
    private float[] playedStrat;
    
    private Slider[] stratSlider;
    private boolean mouseInTriangle;
    private TwoStrategySelector playOrDefer;
    private boolean isPlaying;
    private Color rColor, pColor, sColor;

    public RPSDisplay(float x, float y, int width, int height, PApplet applet) {
        super(x, y, width, height);

        mouseInTriangle = false;
        plannedDist = new float[3];
        plannedStrat = new float[3];
        playedStrat = new float[3];
        for (int i = R; i <= S; i++) {
            plannedDist[i] = 0f;
            plannedStrat[i] = 0f;
            playedStrat[i] = 0f;
        }

        isPlaying = false;

        stratSlider = new Slider[3];

        rColor = new Color(255, 25, 25);
        pColor = new Color(25, 25, 255);
        sColor = new Color(255, 0, 255);

        playOrDefer = new TwoStrategySelector(width / 6, 0, (2 * width) / 3, 60,
                "Play", "Defer");
        playOrDefer.chooseStrategyTwo();

        sideLength = width - 10;
        maxDist = (PApplet.sqrt(3) / 2f) * sideLength;

        rock = new Marker(5, height / 2, true, 10);
        rock.setColor(rColor);
        paper = new Marker(rock.x + sideLength, rock.y, true, 10);
        paper.setColor(pColor);
        scissors = new Marker(rock.x + sideLength / 2,
                rock.y - (int) maxDist, true, 10);
        scissors.setColor(sColor);

        // set up strategy markers
        current = new Marker(0, 0, false, MARKER_RADIUS + 2);
        current.setColor(50, 255, 50);
        planned = new Marker(0, 0, true, MARKER_RADIUS);
        planned.setAlpha(140);
        planned.setColor(25, 255, 25);

        // set up Sliders
        stratSlider[R] = new Slider(50, width - 50, height / 2 + 50,
                rColor, rLabel);
        stratSlider[P] = new Slider(50, width - 50, height / 2 + 100,
                pColor, pLabel);
        stratSlider[S] = new Slider(50, width - 50, height / 2 + 150,
                sColor, sLabel);

        applet.addMouseListener(this);
    }

    @Override
    public void draw(PApplet applet) {
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        float mouseX = applet.mouseX - origin.x;
        float mouseY = applet.mouseY - origin.y;

        applet.background(255);

        applet.stroke(0);
        applet.strokeWeight(3);
        applet.line(rock.x, rock.y, paper.x, paper.y);
        applet.line(rock.x, rock.y, scissors.x, scissors.y);
        applet.line(scissors.x, scissors.y, paper.x, paper.y);
        rock.draw(applet);
        paper.draw(applet);
        scissors.draw(applet);

        calculatePlannedDist(mouseX - rock.x, rock.y - mouseY);

        if (plannedDist[R] <= maxDist && plannedDist[R] >= 0 && plannedDist[P] <= maxDist && plannedDist[P] >= 0 && plannedDist[S] <= maxDist && plannedDist[S] >= 0) {

            mouseInTriangle = true;
        } else {
            mouseInTriangle = false;
        }

        if (mouseInTriangle) {
            calculatePlannedStrats();
            applet.noCursor();
            planned.show();
            planned.update(mouseX, mouseY);

            for (int i = R; i <= S; i++) {
                stratSlider[i].showPlan();
            }
            stratSlider[R].setPlan(plannedStrat[R]);
            stratSlider[P].setPlan(plannedStrat[P]);
            stratSlider[S].setPlan(plannedStrat[S]);
        } else {
            planned.hide();
            for (int i = R; i <= S; i++) {
                stratSlider[i].hidePlan();
            }
            applet.cursor();
        }

        if (!mouseInTriangle && applet.mousePressed) {
            for (int i = R; i <= S; i++) {
                if (stratSlider[i].isGrabbed()) {
                    if (applet.keyPressed && applet.key == PApplet.CODED && applet.keyCode == PApplet.CONTROL) {

                        float currentPos = stratSlider[i].getSliderPos();
                        if (applet.mouseX > applet.pmouseX) {
                            stratSlider[i].moveSlider(currentPos + stratSlider[i].getLength() / 300f);
                        } else if (applet.mouseX < applet.pmouseX) {
                            stratSlider[i].moveSlider(currentPos - stratSlider[i].getLength() / 300f);
                        }
                    } else {
                        stratSlider[i].moveSlider(mouseX);
                    }

                    balanceStratValues(i, stratSlider[i].getStratValue());
                    float[] coords = calculateStratCoords(playedStrat[R],
                            playedStrat[P],
                            playedStrat[S]);

                    current.update(coords[0], coords[1]);

                    break;
                }
            }
        }

        playOrDefer.draw(applet);
        planned.draw(applet);
        current.draw(applet);
        for (int i = R; i <= S; i++) {
            stratSlider[i].draw(applet);
        }

        applet.popMatrix();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        float mouseX = e.getX() - origin.x;
        float mouseY = e.getY() - origin.y;
        if (mouseInTriangle) {
            calculatePlayedStrats(mouseX - rock.x, rock.y - mouseY);
            current.update(mouseX, mouseY);
            current.show();

            stratSlider[R].setStratValue(playedStrat[R]);
            stratSlider[P].setStratValue(playedStrat[P]);
            stratSlider[S].setStratValue(playedStrat[S]);

            if (!isPlaying) {
                isPlaying = true;
                playOrDefer.chooseStrategyOne();
            }
        } else if (playOrDefer.mouseOnAButton(e.getX(), e.getY())) {
            playOrDefer.pressButton();
            if (playOrDefer.getSelection() == 1) {
                if (!isPlaying) {
                    isPlaying = true;
                    for (int i = R; i <= S; i++) {
                        playedStrat[i] = .33f;
                        stratSlider[i].setStratValue(.33f);
                    }
                    float[] coords = calculateStratCoords(.33f, .33f, .33f);
                    current.update(coords[0], coords[1]);
                    current.show();
                }
            } else {
                if (isPlaying) {
                    isPlaying = false;

                    current.hide();
                    for (int i = R; i <= S; i++) {
                        playedStrat[i] = 0f;
                        stratSlider[i].setStratValue(0f);
                    }

                }
            }
        } else {
            for (int i = 0; i < stratSlider.length; i++) {
                if (stratSlider[i].mouseOnHandle(mouseX, mouseY)) {
                    stratSlider[i].grab();
                    if (!isPlaying) {
                        isPlaying = true;
                        current.show();
                        playOrDefer.chooseStrategyOne();
                    }
                    break;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        for (int i = 0; i < stratSlider.length; i++) {
            if (stratSlider[i].isGrabbed()) {
                stratSlider[i].release();
                break;
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    // calculate playedR, playedP, playedS from x, y
    private void calculatePlayedStrats(float x, float y) {
        playedStrat[S] = y / maxDist;

        // constant factor for determining distance
        float epsilon = y + (1 / PApplet.sqrt(3)) * x;

        // calculate distance from paper 3D axis
        float deltaX = x - (PApplet.sqrt(3) / 4) * epsilon;
        float deltaY = y - .75f * epsilon;
        float distP = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));

        playedStrat[P] = distP / maxDist;

        playedStrat[R] = 1 - playedStrat[S] - playedStrat[P];
    }

    // calculate plannedDist entries
    private void calculatePlannedDist(float x, float y) {
        plannedDist[S] = y;

        // constant factors for determining distances
        float epsilon1 = y + (1 / PApplet.sqrt(3)) * x;
        float epsilon2 = y - (1 / PApplet.sqrt(3)) * x;

        float deltaX, deltaY;

        deltaX = x - (PApplet.sqrt(3) / 4) * epsilon1;
        deltaY = y - .75f * epsilon1;
        if (deltaX < 0 && deltaY > 0) {
            plannedDist[P] = -1;
        } else {
            plannedDist[P] = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));
        }

        deltaX = x - .75f * sideLength + (PApplet.sqrt(3) / 4) * epsilon2;
        deltaY = y - (PApplet.sqrt(3) / 4) * sideLength - .75f * epsilon2;
        if (deltaX > 0 && deltaY > 0) {
            plannedDist[R] = -1;
        } else {
            plannedDist[R] = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));
        }
    }

    // calculate plannedPlay entries
    private void calculatePlannedStrats() {
        plannedStrat[S] = plannedDist[S] / maxDist;
        plannedStrat[P] = plannedDist[P] / maxDist;
        plannedStrat[R] = 1 - plannedStrat[S] - plannedStrat[P];
    }

    // calculate x, y coordinates given r, p, s values
    private float[] calculateStratCoords(float R, float P, float S) {
        float[] coords = new float[2];

        coords[0] = rock.x + (maxDist * P) / PApplet.sin(PApplet.PI / 3) + maxDist * S * PApplet.tan(PApplet.PI / 6);
        coords[1] = rock.y - maxDist * S;

        return coords;
    }

    // balance other strat values when using sliders
    private void balanceStratValues(int strat, float value) {
        float pValue, deltaV, percentR, percentP, percentS;
        switch (strat) {
            case R:
                pValue = playedStrat[R];
                deltaV = value - pValue;

                float PStotal = playedStrat[P] + playedStrat[S];
                if (PStotal > 0) {
                    percentP = playedStrat[P] / PStotal;
                    percentS = playedStrat[S] / PStotal;
                } else {
                    PStotal = 1 - value;
                    percentP = .50f;
                    percentS = .50f;
                }

                playedStrat[R] = value;
                playedStrat[P] = (PStotal - deltaV) * percentP;
                playedStrat[S] = 1 - playedStrat[R] - playedStrat[P];
                break;

            case P:
                pValue = playedStrat[P];
                deltaV = value - pValue;

                float RStotal = playedStrat[R] + playedStrat[S];
                if (RStotal > 0) {
                    percentR = playedStrat[R] / RStotal;
                    percentS = playedStrat[S] / RStotal;
                } else {
                    RStotal = 1 - value;
                    percentR = .50f;
                    percentS = .50f;
                }

                playedStrat[P] = value;
                playedStrat[R] = (RStotal - deltaV) * percentR;
                playedStrat[S] = 1 - playedStrat[R] - playedStrat[P];
                break;

            case S:
                pValue = playedStrat[S];
                deltaV = value - pValue;

                float RPtotal = playedStrat[R] + playedStrat[P];
                if (RPtotal > 0) {
                    percentR = playedStrat[R] / RPtotal;
                    percentP = playedStrat[P] / RPtotal;
                } else {
                    RPtotal = 1 - value;
                    percentR = .50f;
                    percentP = .50f;
                }

                playedStrat[S] = value;
                playedStrat[R] = (RPtotal - deltaV) * percentR;
                playedStrat[P] = 1 - playedStrat[R] - playedStrat[S];
                break;

            default:
                throw new RuntimeException("RPSD Error: strat value " + "out of bounds in balanceStratValues()");
        }

        for (int i = R; i <= S; i++) {
            stratSlider[i].setStratValue(playedStrat[i]);
        }
    }
}
