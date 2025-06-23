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
    private static final double DISTANCIA_MAX_FOGO = 400.0; 
    private String trackName = null; 
    private double gunTurnAmt; 

    public void run() {
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(new Color(200, 200, 70));
        setScanColor(Color.white);
        setBulletColor(Color.blue);

        setAdjustGunForRobotTurn(true); 
        setAdjustRadarForGunTurn(true); 

        while (true) {
            if (alvoAtual == null || !inimigos.containsKey(alvoAtual) || (inimigos.get(alvoAtual) != null && getTime() - inimigos.get(alvoAtual).ultimoVisto > 15)) {
                setTurnRadarRight(360);
            }
            execute(); 
        }
    }

    @Override 
    public void onScannedRobot(ScannedRobotEvent e) {
        String nome = e.getName();
        Inimigo inimigo = inimigos.getOrDefault(nome, new Inimigo());
        inimigo.atualizar(e);
        inimigos.put(nome, inimigo);
        escanearInimigoMaisProximo();

        if (!e.getName().equals(alvoAtual)) {
            setTurnRadarRight(Utils.normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading())));
            return;
        } else {
            setTurnRadarRight(Utils.normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading())));
        }

        // --- Detecção de Tiro e Evasiva Reativa ---
        double perdaEnergia = energiaAnterior - e.getEnergy();
        if (perdaEnergia > 0 && perdaEnergia <= 3.0) { 
            executarDefesa();
        }
        energiaAnterior = e.getEnergy();

        // --- Movimentação Lateral Contínua ---
        double anguloMovimento = Utils.normalRelativeAngleDegrees(e.getBearing() + 90 - (15 * moveDirection));
        setTurnRight(anguloMovimento);
        setAhead(100 * moveDirection);

        // --- Lógica de Mira Preditiva e Tiro Adaptativo ---
        if (e.getDistance() <= DISTANCIA_MAX_FOGO) {
            double firePower = calcularFirePowerAdaptativo(e.getDistance());
            double bulletSpeed = 20 - 3 * firePower;

            double absBearing = getHeadingRadians() + e.getBearingRadians();
            double enemyX = getX() + e.getDistance() * Math.sin(absBearing);
            double enemyY = getY() + e.getDistance() * Math.cos(absBearing);

            double enemyHeading = e.getHeadingRadians();
            double enemyVelocity = e.getVelocity();
            double predictedX = enemyX;
            double predictedY = enemyY;
            double deltaTime = 0;

            while ((++deltaTime) * bulletSpeed < Point2D.distance(getX(), getY(), predictedX, predictedY)) {
                predictedX += Math.sin(enemyHeading) * enemyVelocity;
                predictedY += Math.cos(enemyHeading) * enemyVelocity;

                if (predictedX < 18.0 || predictedY < 18.0 ||
                    predictedX > getBattleFieldWidth() - 18.0 ||
                    predictedY > getBattleFieldHeight() - 18.0) {
                    predictedX = Math.min(Math.max(18.0, predictedX), getBattleFieldWidth() - 18.0);
                    predictedY = Math.min(Math.max(18.0, predictedY), getBattleFieldHeight() - 18.0);
                    break;
                }
            }

            double theta = Math.atan2(predictedX - getX(), predictedY - getY());
            double ajuste = Utils.normalRelativeAngle(theta - getGunHeadingRadians());
            setTurnGunRightRadians(ajuste);

            if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
                setFire(firePower);
            }
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        executarDefesa(); 
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        if (trackName != null && !trackName.equals(e.getName())) {
            out.println("Tracking " + e.getName() + " due to collision");
        }
        trackName = e.getName();
        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
        fire(3);
        moveDirection *= -1;
        setBack(100);
    }

    @Override
    public void onHitWall(HitWallEvent e) { 
        moveDirection *= -1; 
        setBack(50);
        setTurnRight(90);
    }

    private void executarDefesa() {
        if (getTime() - ultimoTickDefesa < 20) return;
        ultimoTickDefesa = getTime();

        moveDirection *= -1;
        double angulo = 60 + Math.random() * 60;
        setTurnRight(angulo * moveDirection);
        setAhead(150 + Math.random() * 100);
    }

    @Override
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }

    private double calcularFirePowerAdaptativo(double distancia) { 
        distancia = Math.min(distancia, DISTANCIA_MAX_FOGO);
        double power = 4.0 - (2.9 * (distancia / DISTANCIA_MAX_FOGO));
        return Math.max(0.1, Math.min(3.0, power));
    }

    private void escanearInimigoMaisProximo() {
        double menorDistancia = Double.MAX_VALUE;
        String maisProximo = null;

        Iterator<Map.Entry<String, Inimigo>> it = inimigos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Inimigo> entry = it.next();
            Inimigo i = entry.getValue();

            if (getTime() - i.ultimoVisto > 10) { 
                it.remove();
                continue;
            }

            if (i.distancia < menorDistancia) {
                menorDistancia = i.distancia;
                maisProximo = entry.getKey();
            }
        }
        alvoAtual = maisProximo;
    }

    private double normalRelativeAngleDegrees(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}

class Inimigo {
    String nome; 
    double distancia; 
    double energia; 
    double heading; 
    double velocidade; 
    long ultimoVisto; 

    public Inimigo() {
        
    }

    public void atualizar(ScannedRobotEvent e) {
        this.nome = e.getName();
        this.distancia = e.getDistance();
        this.energia = e.getEnergy();
        this.heading = e.getHeading();
        this.velocidade = e.getVelocity();
        this.ultimoVisto = e.getTime();
    }
}