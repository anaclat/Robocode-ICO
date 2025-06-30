package divas;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import robocode.*;
import robocode.util.Utils;

public class Molieres extends AdvancedRobot {
    static double POTENCIA_TIRO = 3;

    class Robo extends Point2D.Double {
        public long tempoVarredura; 
        public boolean vivo = true;
        public double energia;
        public String nome;
        public double anguloCanhaoRadianos;
        public double anguloAbsolutoRadianos;
        public double velocidade;
        public double direcao;
        public double ultimaDirecao;
        public double pontuacaoDisparo;
        public double distancia; 
    }
    
    public static class Utilitario {
        static double limitar(double valor, double min, double max) {
            return Math.max(min, Math.min(max, valor));
        }
        
        static double aleatorioEntre(double min, double max) {
            return min + Math.random() * (max - min);
        }
        
        static Point2D projetar(Point2D origem, double angulo, double distancia) {
            return new Point2D.Double(origem.getX() + Math.sin(angulo) * distancia,
                    origem.getY() + Math.cos(angulo) * distancia);
        }
        
        static double anguloAbsoluto(Point2D origem, Point2D alvo) {
            return Math.atan2(alvo.getX() - origem.getX(), alvo.getY() - origem.getY());
        }
        
        static int sinal(double v) {
            return v < 0 ? -1 : 1;
        }
    }

    class Movimento_1VS1 {
        private static final double LARGURA_CAMPO = 800;
        private static final double ALTURA_CAMPO = 600;
        private static final double TEMPO_MAX_TENTATIVA = 125;
        private static final double AJUSTE_REVERSA = 0.421075;
        private static final double EVASAO_PADRAO = 1.2;
        private static final double AJUSTE_QUIQUE_PAREDE = 0.699484;
        private final AdvancedRobot robô;
        private final Rectangle2D areaDisparo = new Rectangle2D.Double(MARGEM_PAREDE, MARGEM_PAREDE,
                LARGURA_CAMPO - MARGEM_PAREDE * 2, ALTURA_CAMPO - MARGEM_PAREDE * 2);
        private double direcao = 0.4;
        
        Movimento_1VS1(AdvancedRobot _robô) {
            this.robô = _robô;
        }
        
        public void onScannedRobot(ScannedRobotEvent e) {
            Robo inimigo = new Robo();
            inimigo.anguloAbsolutoRadianos = robô.getHeadingRadians() + e.getBearingRadians();
            inimigo.distancia = e.getDistance();
            Point2D posicaoRobo = new Point2D.Double(robô.getX(), robô.getY());
            Point2D posicaoInimigo = Utilitario.projetar(posicaoRobo, inimigo.anguloAbsolutoRadianos, inimigo.distancia);
            Point2D destinoRobo;
            double tempoTentativa = 0;
            while (!areaDisparo.contains(destinoRobo = Utilitario.projetar(posicaoInimigo, inimigo.anguloAbsolutoRadianos + Math.PI + direcao,
                    inimigo.distancia * (EVASAO_PADRAO - tempoTentativa / 100.0))) && tempoTentativa < TEMPO_MAX_TENTATIVA)
                tempoTentativa++;
            if ((Math.random() < (Rules.getBulletSpeed(POTENCIA_TIRO) / AJUSTE_REVERSA) / inimigo.distancia ||
                    tempoTentativa > (inimigo.distancia / Rules.getBulletSpeed(POTENCIA_TIRO) / AJUSTE_QUIQUE_PAREDE)))
                direcao = -direcao;
            double angulo = Utilitario.anguloAbsoluto(posicaoRobo, destinoRobo) - robô.getHeadingRadians();
            robô.setAhead(Math.cos(angulo) * 100);
            robô.setTurnRightRadians(Math.tan(angulo));
        }
    }

    static class Onda extends Condition {
        static Point2D posicaoAlvo;
        double potenciaTiro;
        Point2D posicaoCanhao;
        double angulo;
        double direcaoLateral;
        private static final double DISTANCIA_MAXIMA = 900;
        private static final int INDICES_DISTANCIA = 5;
        private static final int INDICES_VELOCIDADE = 5;
        private static final int BINS = 25;
        private static final int BIN_CENTRAL = (BINS - 1) / 2;
        private static final double ANGULO_ESCAPE_MAXIMO = 0.7;
        private static final double LARGURA_BIN = ANGULO_ESCAPE_MAXIMO / (double) BIN_CENTRAL; 
        private static final int[][][][] buffersEstatisticos = new int[INDICES_DISTANCIA][INDICES_VELOCIDADE][INDICES_VELOCIDADE][BINS];
        private int[] buffer;
        private double distanciaPercorrida;
        private final AdvancedRobot robô;
        
        Onda(AdvancedRobot _robô) {
            this.robô = _robô;
        }
        
        public boolean test() {
            avancar();
            if (chegou()) {
                buffer[binAtual()]++;
                robô.removeCustomEvent(this);
            }
            return false;
        }
        
        double offsetAnguloMaisVisitado() {
            return (direcaoLateral * LARGURA_BIN) * (binMaisVisitado() - BIN_CENTRAL);
        }
        
        void definirSegmentacoes(double distancia, double velocidade, double ultimaVelocidade) {
            int indiceDistancia = (int) (distancia / (DISTANCIA_MAXIMA / INDICES_DISTANCIA));
            int indiceVelocidade = (int) Math.abs(velocidade / 2);
            int indiceUltimaVelocidade = (int) Math.abs(ultimaVelocidade / 2);
            buffer = buffersEstatisticos[indiceDistancia][indiceVelocidade][indiceUltimaVelocidade];
        }
        
        private void avancar() {
            distanciaPercorrida += Rules.getBulletSpeed(potenciaTiro);
        }
        
        private boolean chegou() {
            return distanciaPercorrida > posicaoCanhao.distance(posicaoAlvo) - MARGEM_PAREDE;
        }
        
        private int binAtual() {
            int bin = (int) Math.round(((Utils.normalRelativeAngle
                    (Utilitario.anguloAbsoluto(posicaoCanhao, posicaoAlvo) - angulo)) /
                    (direcaoLateral * LARGURA_BIN)) + BIN_CENTRAL);
            return (int) Utilitario.limitar(bin, 0, BINS - 1);
        }
        
        private int binMaisVisitado() {
            int maisVisitado = BIN_CENTRAL;
            for (int i = 0; i < BINS; i++)
                if (buffer[i] > buffer[maisVisitado])
                    maisVisitado = i;
            return maisVisitado;
        }
    }
    
     static Random aleatorio = new Random();
    
    private void corMolieres() {
        setColors(new Color(128, 0, 128),
                new Color(128, 0, 128),
                new Color(128, 0, 128),
                new Color(128, 0, 128),
                new Color(128, 0, 128));
    }
    
    private void Cor() {
        setColors(new Color(128, 0, 128),
                new Color(128, 0, 128),
                new Color(128, 0, 128),
                new Color(128, 0, 128),
                new Color(128, 0, 128));
    }
    
    public void onWin(WinEvent event) {
        while (true) {
            Cor();
            turnRadarRight(360);
        }
    }

    static final int QUANTIDADE_PONTOS_PREVISTOS = 150;
    static final double MARGEM_PAREDE = 18;
    HashMap<String, Robo> listaInimigos = new HashMap<>();
    Robo meuRobo = new Robo();
    Robo alvo;
    List<Point2D.Double> posicoesPossiveis = new ArrayList<>();
    Point2D.Double pontoAlvo = new Point2D.Double(60, 60);
    Rectangle2D.Double campoBatalha = new Rectangle2D.Double();
    int tempoInativo = 30;
    private static double direcaoLateral;
    private static double velocidadeInimigoAnterior;
    private static Movimento_1VS1 movimento1VS1;
    
    {
        movimento1VS1 = new Movimento_1VS1(this);
    }

    public void run() {
        campoBatalha.height = getBattleFieldHeight();
        campoBatalha.width = getBattleFieldWidth();
        meuRobo.x = getX();
        meuRobo.y = getY();
        meuRobo.energia = getEnergy();
        pontoAlvo.x = meuRobo.x;
        pontoAlvo.y = meuRobo.y;
        alvo = new Robo();
        alvo.vivo = false;
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        if (getOthers() > 1) {
            atualizarListaPosicoes(QUANTIDADE_PONTOS_PREVISTOS);
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            while (true) {
                meuRobo.ultimaDirecao = meuRobo.direcao;
                meuRobo.direcao = getHeadingRadians();
                meuRobo.x = getX();
                meuRobo.y = getY();
                meuRobo.energia = getEnergy();
                meuRobo.anguloCanhaoRadianos = getGunHeadingRadians();
                Iterator<Robo> iteradorInimigos = listaInimigos.values().iterator();
                while (iteradorInimigos.hasNext()) {
                    Robo r = iteradorInimigos.next();
                    if (getTime() - r.tempoVarredura > 25) {
                        r.vivo = false;
                        if (alvo.nome != null && r.nome.equals(alvo.nome))
                            alvo.vivo = false;
                    }
                }
                movimento();
                if (alvo.vivo)
                    disparar();
                execute();
            }
        }
        else {
            direcaoLateral = 1;
            velocidadeInimigoAnterior = 0;
            while (true)
                turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

     public void onScannedRobot(ScannedRobotEvent e) {
        corMolieres();
        
        if (getOthers() > 1) {
            Robo inimigo = listaInimigos.get(e.getName());
            if (inimigo == null) {
                inimigo = new Robo();
                listaInimigos.put(e.getName(), inimigo);
            }
            inimigo.anguloAbsolutoRadianos = e.getBearingRadians();
            inimigo.setLocation(new Point2D.Double(
                    meuRobo.x + e.getDistance() * Math.sin(getHeadingRadians() + inimigo.anguloAbsolutoRadianos),
                    meuRobo.y + e.getDistance() * Math.cos(getHeadingRadians() + inimigo.anguloAbsolutoRadianos)));
            inimigo.ultimaDirecao = inimigo.direcao;
            inimigo.nome = e.getName();
            inimigo.energia = e.getEnergy();
            inimigo.vivo = true;
            inimigo.tempoVarredura = getTime();
            inimigo.velocidade = e.getVelocity();
            inimigo.direcao = e.getHeadingRadians();
            inimigo.pontuacaoDisparo = inimigo.energia < 25 ? (inimigo.energia < 5 ?
                    (inimigo.energia == 0 ? Double.MIN_VALUE : inimigo.distance(meuRobo) * 0.1) :
                    inimigo.distance(meuRobo) * 0.75) : inimigo.distance(meuRobo);
            if (getOthers() == 1)
                setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
            
            if (!alvo.vivo || inimigo.pontuacaoDisparo < alvo.pontuacaoDisparo)
                alvo = inimigo;
        }
        
        else {
            setScanColor(Color.red);
            Robo inimigo = new Robo();
            inimigo.anguloAbsolutoRadianos = getHeadingRadians() + e.getBearingRadians();
            inimigo.distancia = e.getDistance();
            inimigo.velocidade = e.getVelocity();
            if (inimigo.velocidade != 0)
                direcaoLateral = Utilitario.sinal(inimigo.velocidade * Math.sin(e.getHeadingRadians() - inimigo.anguloAbsolutoRadianos));
            Onda onda = new Onda(this);
            onda.posicaoCanhao = new Point2D.Double(getX(), getY());
            Onda.posicaoAlvo = Utilitario.projetar(onda.posicaoCanhao, inimigo.anguloAbsolutoRadianos, inimigo.distancia);
            onda.direcaoLateral = direcaoLateral;
            onda.definirSegmentacoes(inimigo.distancia, inimigo.velocidade, velocidadeInimigoAnterior);
            velocidadeInimigoAnterior = inimigo.velocidade;
            onda.angulo = inimigo.anguloAbsolutoRadianos;
            setTurnGunRightRadians(Utils.normalRelativeAngle(
                    inimigo.anguloAbsolutoRadianos - getGunHeadingRadians() + onda.offsetAnguloMaisVisitado()));
            POTENCIA_TIRO = Math.min(3, Math.min(this.getEnergy(), e.getEnergy()) / (double) 4);
            onda.potenciaTiro = POTENCIA_TIRO;
            if (getEnergy() < 2 && e.getDistance() < 500)
                onda.potenciaTiro = 0.1;
            else if (e.getDistance() >= 500)
                onda.potenciaTiro = 1.1;
            setFire(onda.potenciaTiro);
            if (getEnergy() >= onda.potenciaTiro)
                addCustomEvent(onda);
            movimento1VS1.onScannedRobot(e);
            setTurnRadarRightRadians(Utils.normalRelativeAngle(inimigo.anguloAbsolutoRadianos - getRadarHeadingRadians()) * 2);
        }
    }
}
