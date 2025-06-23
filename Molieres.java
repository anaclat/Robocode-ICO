package myrobots;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Molieres extends AdvancedRobot {
    private int moveDirection = 1;
    private double energiaAnterior = 100;
    private long ultimoTickDefesa = 0;
    private double moveDirection = 1;
    private String trackName;
    private Map<String, Inimigo> inimigos = new HashMap<>();
    private String alvoAtual = null;
    private static final double DISTANCIA_MAX_FOGO = 350.0;

    int count = 0;
    double gunTurnAmt;

    public void run() {
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(new Color(200, 200, 70));
        setScanColor(Color.white);
        setBulletColor(Color.blue);

        trackName = null;
        setAdjustGunForRobotTurn(true);
        gunTurnAmt = 10;

        while (true) {
            turnGunRight(gunTurnAmt);
            count++;

            if (count > 2) gunTurnAmt = -10;
            if (count > 5) gunTurnAmt = 10;
            if (count > 11) trackName = null;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // DEFESA: detecta tiro

        double perdaEnergia = energiaAnterior - e.getEnergy();
        if (perdaEnergia > 0 && perdaEnergia <= 3.0) {
            executarDefesa();
        }
        energiaAnterior = e.getEnergy();

        if (trackName != null && !e.getName().equals(trackName)) {
            return;
        }

        if (trackName == null) {
            trackName = e.getName();
            out.println("Tracking " + trackName);
        }

        count = 0;

        if (e.getDistance() > 150) {
            gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
            turnGunRight(gunTurnAmt);
            turnRight(e.getBearing());
            ahead(e.getDistance() - 140);
            return;
        }

        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
        fire(3);

        if (e.getDistance() < 100) {
            if (e.getBearing() > -90 && e.getBearing() <= 90) {
                back(40);
            } else {
                ahead(40);
            }
        }

        scan();
    }

    public void onHitByBullet(HitByBulletEvent e) {
        executarDefesa();
    }

    public void onHitWall(HitWallEvent e) {
        moveDirection *= -1;
        setBack(50);
        setTurnRight(90);
    }

    public void onHitRobot(HitRobotEvent e) {
        alvoAtual = e.getName();
        setTurnRadarRight(360);
        double angle = Utils.normalRelativeAngleDegrees(e.getBearing());
        setTurnGunRight(angle);
        setFire(3);
        moveDirection *= -1;
        setBack(100);
    }

    private void executarDefesa() {
		if (getTime() - ultimoTickDefesa < 15) return;
        ultimoTickDefesa = getTime();

        moveDirection *= -1;
        double angulo = 60 + Math.random() * 60;
        setTurnRight(angulo);
        setAhead(120 + Math.random() * 80);
    }

    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }

    private double normalRelativeAngleDegrees(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // Classe interna exemplo (adaptar conforme uso real)
    static class Inimigo {
        // Exemplo de campos possíveis
        String nome;
        double energia;
        double x, y;
    }
}
