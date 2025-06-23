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

        double perdaEnergia = energiaAnterior - e.getEnergy();
        if (perdaEnergia > 0 && perdaEnergia <= 3.0) {
            // Lógica de defesa virá em um commit futuro
        }
        energiaAnterior = e.getEnergy();

        double anguloMovimento = Utils.normalRelativeAngleDegrees(e.getBearing() + 90 - (15 * moveDirection));
        setTurnRight(anguloMovimento);
        setAhead(100 * moveDirection);
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
        back(50);
    }

    @Override
    public void onHitWall(HitWallEvent e) { 
        moveDirection *= -1; 
        setBack(50);
    }

    private void executarDefesa() {
        if (getTime() - ultimoTickDefesa < 15) return;
        ultimoTickDefesa = getTime();

        moveDirection *= -1;
        double angulo = 60 + Math.random() * 60; 
        setTurnRight(angulo);
        setAhead(120 + Math.random() * 80);
    }

    @Override
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
    
    private double calcularFirePowerAdaptativo(double distancia) { 
        return 0.0; 
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