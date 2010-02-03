package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.RPSPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import processing.core.PApplet;
import processing.core.PGraphics;

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
    private Color rColor, pColor, sColor;
    private boolean enabled;
    private ServerInterface server;
    private ClientInterface client;
    private PApplet applet;
    private RPSPayoffFunction payoffFunction;
    private PGraphics heatmap;
    private boolean visible = false;

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
        playedStrat = new float[3];
        opponentStrat = new float[3];
        for (int i = R; i <= S; i++) {
            plannedDist[i] = 0f;
            plannedStrat[i] = 0f;
            playedStrat[i] = 0f;
            opponentStrat[i] = 0f;
        }

        stratSlider = new Slider[3];

        rColor = new Color(255, 25, 25);
        pColor = new Color(25, 25, 255);
        sColor = new Color(255, 0, 255);

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

        setEnabled(false);

        applet.addMouseListener(this);
        this.applet = applet;
    }

    public void setPayoffFunction(RPSPayoffFunction payoffFunction) {
        if (payoffFunction == null) {
            visible = false;
            return;
        } else {
            visible = true;
            this.payoffFunction = payoffFunction;
            heatmap = calculateHeatmap(applet);
        }
    }

    @Override
    public void draw(PApplet applet) {
        if (!visible) {
            return;
        }

        if (heatmap != null) {
            applet.image(heatmap, origin.x + rock.x, origin.y + scissors.y);
        }


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

        if (enabled) {
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
        if (enabled) {
            float mouseX = e.getX() - origin.x;
            float mouseY = e.getY() - origin.y;
            if (mouseInTriangle) {
                calculatePlayedStrats(mouseX - rock.x, rock.y - mouseY);
            } else {
                for (int i = 0; i < stratSlider.length; i++) {
                    if (stratSlider[i].mouseOnHandle(mouseX, mouseY)) {
                        stratSlider[i].grab();
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

    public void setPlayerRPS(float newR, float newP, float newS) {
        playedStrat[R] = newR;
        playedStrat[P] = newP;
        playedStrat[S] = newS;

        for (int i = R; i <= S; i++) {
            stratSlider[i].setStratValue(playedStrat[i]);
        }

        float[] coords = calculateStratCoords(newR, newP, newS);
        current.update(coords[0], coords[1]);
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
        } else {
            current.hide();
            opponent.hide();
        }
    }

    public void pause() {
        setEnabled(false);
    }

    public void reset() {
        setEnabled(false);
        for (int i = R; i <= S; i++) {
            plannedDist[i] = 0f;
            plannedStrat[i] = 0f;
            playedStrat[i] = 0f;
            opponentStrat[i] = 0f;
            stratSlider[i].setStratValue(0f);
        }

        current.hide();
        planned.hide();
        opponent.hide();
    }

    public boolean isEnabled() {
        return enabled;
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

        setPlayerRPS(newR, newP, newS);
        server.strategyChanged(client.getFullName());
    }

    private float[] translate(float x, float y) {
        float newS = y / maxDist;

        // constant factor for determining distance
        float epsilon = y + (1 / PApplet.sqrt(3)) * x;

        // calculate distance from paper 3D axis
        float deltaX = x - (PApplet.sqrt(3) / 4) * epsilon;
        float deltaY = y - .75f * epsilon;
        float distP = PApplet.sqrt(PApplet.sq(deltaX) + PApplet.sq(deltaY));

        float newP = distP / maxDist;

        float newR = 1 - newS - newP;

        return new float[]{newR, newP, newS};
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
                setPlayerRPS(newR, newP, newS);
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
                setPlayerRPS(newR, newP, newS);
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
                setPlayerRPS(newR, newP, newS);
                break;

            default:
                throw new RuntimeException("RPS Error: strat value " + "out of bounds in balanceStratValues()");
        }

        server.strategyChanged(client.getFullName());
    }

    private PGraphics calculateHeatmap(PApplet applet) {
        int w = Math.round(paper.x - rock.x);
        int h = Math.round(rock.y - scissors.y);
        PGraphics g = applet.createGraphics(w, h, PApplet.P2D);
        g.beginDraw();
        g.loadPixels();
        for (int x = 0; x < g.width; x++) {
            for (int y = 0; y < g.height; y++) {
                int i = y * g.width + x;
                float[] coords = translate(x, g.height - y);
                if (coords[0] >= 0 && coords[1] >= 0 && coords[2] >= 0) {
                    float u = payoffFunction.getPayoff(
                            coords[0], coords[1], coords[2],
                            coords[0], coords[1], coords[2]) / 100f;
                    g.pixels[i] = applet.color(u * 255, 0, 0);
                } else {
                    g.pixels[i] = applet.color(255, 255, 255);
                }
            }
        }
        g.updatePixels();
        g.endDraw();
        return g;
    }
}
