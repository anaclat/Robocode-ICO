package myrobots;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Molieres extends AdvancedRobot {

    private int moveDirection = 1;
    private double energiaAnterior = 100;
    private long ultimoTickDefesa = 0;

    private Map<String, Inimigo> inimigos = new HashMap<>();
    private String alvoAtual = null;
    private static final double DISTANCIA_MAX_FOGO = 350.0;

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
			
			if (count > 2) {
				gunTurnAmt = -10;
			}
			
			if (count > 5) {
				gunTurnAmt = 10;
			}
			
			if (count > 11) {
				trackName = null;
			}
		}
	}

	
	public void onScannedRobot(ScannedRobotEvent e) {


	    // Detecta tiro e executa evasiva
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

	public void onHitRobot(HitRobotEvent e) {
		
		if (trackName != null && !trackName.equals(e.getName())) {
			out.println("Tracking " + e.getName() + " due to collision");
		}
		
		trackName = e.getName();
		
		gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
		turnGunRight(gunTurnAmt);
		fire(3);
		back(50);
	}

	private void executarDefesa() {
    if (getTime() - ultimoTickDefesa < 15) return; // Cooldown
    ultimoTickDefesa = getTime();

    moveDirection *= -1; // Inverte direção
    double angulo = 60 + Math.random() * 60; // Ângulo entre 60 e 120 graus
    setTurnRight(angulo);
    setAhead(120 + Math.random() * 80); // Avança entre 120 e 200
}

	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}
}
