// Detecta tiro e executa evasiva
    double perdaEnergia = energiaAnterior - e.getEnergy();
    if (perdaEnergia > 0 && perdaEnergia <= 3.0) {
        executarDefesa();
    }

    // Movimentação lateral padrão com leve aleatoriedade
    double anguloMovimento = Utils.normalRelativeAngleDegrees(e.getBearing() + 90 - (15 * moveDirection));
    setTurnRight(anguloMovimento);
    setAhead(100 * moveDirection);

    // Também usada em:
    moveDirection *= -1; // Alternância de direção em defesa, colisão, parede