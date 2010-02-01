package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import processing.core.PApplet;

public class RPSDisplay extends Sprite implements MouseListener, KeyListener {

    public String rLabel = "Rock";
    public String pLabel = "Paper";
    public String sLabel = "Scissors";
    private final int MARKER_RADIUS = 7;
    private final int R = 0;
    private final int P = 1;
    private final int S = 2;
    private final int D = 3;
    private float sideLength;
    private Marker rock, paper, scissors;
    private float maxDist;
    private Marker current, planned, opponent;
    private float[] plannedDist;
    private float[] plannedStrat;
    // current played strategies stored here (R, P, S, D)
    // D can only be 1 or 0
    private float[] playedStrat;
    // average of opponents' strategies
    private float[] opponentStrat;
    private Slider[] stratSlider;
    private boolean mouseInTriangle;
    private TwoStrategySelector playOrDefer;
    private Color rColor, pColor, sColor;
    private boolean active;
    private ServerInterface server;
    private ClientInterface client;

    public RPSDisplay(
            float x, float y, int width, int height,
            PApplet applet,
            ServerInterface server,
            ClientInterface client) {
        super(x, y, width, height);

        this.server = server;
        this.client = client;

        mouseInTriangle = false;
        plannedDist = new float[3];
        plannedStrat = new float[3];
        playedStrat = new float[4];
        opponentStrat = new float[4];
        for (int i = R; i <= S; i++) {
            plannedDist[i] = 0f;
            plannedStrat[i] = 0f;
            playedStrat[i] = 0f;
            opponentStrat[i] = 0f;
        }

        playedStrat[D] = 1.0f;
        opponentStrat[D] = 1.0f;

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
        planned = new Marker(0, 0, false, MARKER_RADIUS);
        planned.setAlpha(140);
        planned.setColor(25, 255, 25);
        opponent = new Marker(0, 0, false, MARKER_RADIUS);
        opponent.setColor(200, 40, 40);

        // set up Sliders
        stratSlider[R] = new Slider(50, width - 50, height / 2 + 50,
                rColor, rLabel);
        stratSlider[P] = new Slider(50, width - 50, height / 2 + 100,
                pColor, pLabel);
        stratSlider[S] = new Slider(50, width - 50, height / 2 + 150,
                sColor, sLabel);

        active = false;

        applet.addMouseListener(this);
        applet.addKeyListener(this);
    }

    @Override
    public void draw(PApplet applet) {
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        float mouseX = applet.mouseX - origin.x;
        float mouseY = applet.mouseY - origin.y;

        applet.stroke(0);
        applet.strokeWeight(3);
        applet.line(rock.x, rock.y, paper.x, paper.y);
        applet.line(rock.x, rock.y, scissors.x, scissors.y);
        applet.line(scissors.x, scissors.y, paper.x, paper.y);
        rock.draw(applet);
        paper.draw(applet);
        scissors.draw(applet);

        if (active) {
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

                        break;
                    }
                }
            }
        }

        playOrDefer.draw(applet);
        planned.draw(applet);
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
        if (active) {
            float mouseX = e.getX() - origin.x;
            float mouseY = e.getY() - origin.y;
            if (mouseInTriangle) {
                calculatePlayedStrats(mouseX - rock.x, rock.y - mouseY);
            } else if (playOrDefer.mouseOnAButton(e.getX(), e.getY())) {
                playOrDefer.pressButton();
                if (playOrDefer.getSelection() == 1) {
                    if (playedStrat[D] == 1.0f) {
                        setPlayerRPSD(.33f, .33f, .33f, 0f);
                    }
                } else {
                    if (playedStrat[D] == 0f) {
                        setPlayerRPSD(0f, 0f, 0f, 1.0f);
                    }
                }
            } else {
                for (int i = 0; i < stratSlider.length; i++) {
                    if (stratSlider[i].mouseOnHandle(mouseX, mouseY)) {
                        stratSlider[i].grab();
                        if (playedStrat[D] == 1.0f) {
                            playedStrat[D] = 0f;
                            current.show();
                            playOrDefer.chooseStrategyOne();
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (int i = 0; i < stratSlider.length; i++) {
            if (stratSlider[i].isGrabbed()) {
                stratSlider[i].release();
                break;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.isActionKey() && active) {
            if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                if (playedStrat[D] == 1.0f) {
                    setPlayerRPSD(.33f, .33f, .33f, 0f);
                    playOrDefer.chooseStrategyOne();
                }
            } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                if (playedStrat[D] == 0f) {
                    setPlayerRPSD(0f, 0f, 0f, 1.0f);
                    playOrDefer.chooseStrategyTwo();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    public void setPlayerRPSD(float newR, float newP, float newS, float newD) {
        playedStrat[R] = newR;
        playedStrat[P] = newP;
        playedStrat[S] = newS;
        playedStrat[D] = newD;

        for (int i = R; i <= S; i++) {
            stratSlider[i].setStratValue(playedStrat[i]);
        }

        if (newD == 1.0f) {
            current.hide();
        } else {
            current.show();
            float[] coords = calculateStratCoords(newR, newP, newS);
            current.update(coords[0], coords[1]);
        }
        server.strategyChanged(client.getFullName());
    }

    public void setOpponentRPSD(float r, float p, float s, float d) {
        opponentStrat[R] = r;
        opponentStrat[P] = p;
        opponentStrat[S] = s;
        opponentStrat[D] = d;

        if (d == 1.0f) {
            opponent.hide();
        } else {
            opponent.show();
            float[] coords = calculateStratCoords(r, p, s);
            opponent.update(coords[0], coords[1]);
        }
    }

    public float[] getPlayerRPSD() {
        return playedStrat;
    }

    public void activate() {
        active = true;
    }

    public void pause() {
        active = false;
    }

    public void reset() {
        active = false;
        for (int i = R; i <= S; i++) {
            plannedDist[i] = 0f;
            plannedStrat[i] = 0f;
            playedStrat[i] = 0f;
            opponentStrat[i] = 0f;
            stratSlider[i].setStratValue(0f);
        }

        playedStrat[D] = 1.0f;
        opponentStrat[D] = 1.0f;
        playOrDefer.chooseStrategyTwo();

        current.hide();
        planned.hide();
        opponent.hide();
    }

    public boolean isActive() {
        return active;
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

        if (playedStrat[D] == 1.0f) {
            playOrDefer.chooseStrategyOne();
        }
        setPlayerRPSD(newR, newP, newS, 0f);
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
    private float[] calculateStratCoords(float r, float p, float s) {
        float[] coords = new float[2];

        coords[0] = rock.x + (maxDist * p) / PApplet.sin(PApplet.PI / 3) + maxDist * s * PApplet.tan(PApplet.PI / 6);
        coords[1] = rock.y - maxDist * s;

        return coords;
    }

    // balance other strat values when using sliders
    private void balanceStratValues(int strat, float value) {
        float pValue, deltaV, percentR, percentP, percentS;
        float newR, newP, newS;
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

                newR = value;
                newP = (PStotal - deltaV) * percentP;
                newS = 1 - newR - newP;
                setPlayerRPSD(newR, newP, newS, 0f);
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

                newP = value;
                newR = (RStotal - deltaV) * percentR;
                newS = 1 - newR - newP;
                setPlayerRPSD(newR, newP, newS, 0f);
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

                newS = value;
                newR = (RPtotal - deltaV) * percentR;
                newP = 1 - newR - newS;
                setPlayerRPSD(newR, newP, newS, 0f);
                break;

            default:
                throw new RuntimeException("RPSD Error: strat value " + "out of bounds in balanceStratValues()");
        }
    }
}
