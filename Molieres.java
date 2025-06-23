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
        setColors(new Color(128, 0, 128), Color.DARK_GRAY, Color.BLACK); // Roxo, cinza e preto
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        while (true) {
            escanearInimigoMaisProximo();

            if (alvoAtual != null) {
                Inimigo alvo = inimigos.get(alvoAtual);
                if (alvo == null || getTime() - alvo.ultimoVisto > 5) {
                    alvoAtual = null;
                } else {
                    double radarTurn = getHeadingRadians() + alvo.anguloRelativo - getRadarHeadingRadians();
                    setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn) * 2);
                }
            } else {
                setTurnRadarRight(360); // Radar contínuo se sem alvo
            }

            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Atualiza dados do inimigo
        String nome = e.getName();
        Inimigo inimigo = inimigos.getOrDefault(nome, new Inimigo());
        inimigo.atualizar(e);
        inimigos.put(nome, inimigo);
        escanearInimigoMaisProximo();

        // Detecta tiro e executa evasiva
        double perdaEnergia = energiaAnterior - e.getEnergy();
        if (perdaEnergia > 0 && perdaEnergia <= 3.0) {
            executarDefesa();
        }
        energiaAnterior = e.getEnergy();

        // Movimentação lateral
        double anguloMovimento = Utils.normalRelativeAngleDegrees(e.getBearing() + 90 - (15 * moveDirection));
        setTurnRight(anguloMovimento);
        setAhead(100 * moveDirection);

        // Tiro adaptativo com previsão de posição
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

    private double calcularFirePowerAdaptativo(double distancia) {
        distancia = Math.min(distancia, DISTANCIA_MAX_FOGO);
        double power = 4.0 - (2.9 * (distancia / DISTANCIA_MAX_FOGO));
        return Math.max(0.1, Math.min(3.0, power));
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

    // Classe para guardar dados dos inimigos
    static class Inimigo {
        double energia;
        double anguloRelativo;
        double distancia;
        long ultimoVisto;

        void atualizar(ScannedRobotEvent e) {
            this.energia = e.getEnergy();
            this.anguloRelativo = e.getBearingRadians();
            this.distancia = e.getDistance();
            this.ultimoVisto = e.getTime();
        }
    }
}
