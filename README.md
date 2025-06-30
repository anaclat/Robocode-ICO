## 🤖 Projeto Robocode - IFSC

Este repositório contém o desenvolvimento do robô **Molieres**, criado como parte da atividade de Robocode na disciplina de **Introdução à Computação** no [IFSC - Instituto Federal de Santa Catarina - Câmpus São José](https://www.ifsc.edu.br/web/campus-sao-jose).

## 🎯 Objetivo da Atividade

- Aprender a usar **Git e GitHub** para versionamento de código.
- Desenvolver um robô competitivo para o simulador **Robocode**.
- Trabalhar em equipe com uso de **branches**, **commits** e **integração de código**.
- Aplicar conceitos de lógica e programação em um projeto prático.

## 👥 Integrantes da Equipe

- Ana Ronzani (GitHub: [@anaclat](https://github.com/anaclat))
- Vitoria Corrêa (GitHub: [@vitoriacorrea](https://github.com/vitoriacorrea))
- Damares Gaia (GitHub: [@damaresgaia](https://github.com/damaresgaia))
- Noemi Souza (GitHub: [@noemiDsouza](https://github.com/noemiDsouza))

## 📁 Estrutura do Projeto

- `Molieres.java`: código-fonte do robô.
- `README.md`: este arquivo de documentação.

> ⚠️ Arquivos `.class` (compilados) estão ignorados via `.gitignore`.

## 🧠 Estratégia do Robô Molieres

O robô Molieres adota uma estratégia de combate dinâmica e adaptativa para maximizar sua sobrevivência e precisão de ataque:

🔄 Radar inteligente: rastreia continuamente o inimigo mais próximo com alta precisão, priorizando o adversário mais recente e mais próximo.

🏃‍♂️ Movimentação lateral com evasivas: se move lateralmente ao inimigo e inverte a direção quando detecta tiros ou colisões, dificultando ser atingido.

🎯 Mira com previsão de movimento: calcula a posição futura do inimigo com base em velocidade e direção, ajustando o ângulo do canhão para disparos mais precisos.

💥 Tiro adaptativo por distância: calibra a força do tiro de acordo com a distância do oponente, otimizando dano e velocidade do projétil.

🛡️ Defesa reativa: ao detectar perda de energia do inimigo (indício de disparo) ou ao ser atingido, realiza manobras evasivas para escapar da linha de fogo.

🧠 Memória de inimigos: armazena informações dos robôs avistados (nome, energia, distância e tempo) e limpa os dados de inimigos inativos.

## 💡 Controle de Versão com Git

- Cada membro trabalhou em sua **branch** separada.
- As alterações foram revisadas antes de integrar à `main`.
- Todos os membros contribuíram com **commits** documentados.

## 🚀 Como executar o robô

1. Instale o [Robocode](https://robocode.sourceforge.io/).
2. Copie o arquivo `Molieres.java` para a pasta `robots/` do Robocode.
3. Compile o código pelo próprio Robocode ou com linha de comando.
4. Inicie uma batalha e selecione o robô **Molieres**.

## 📝 Relatório Final

# Estratégias Implementadas:

### Movimento Inteligente
- Gera pontos aleatórios ao redor do robô.
- Avalia cada ponto calculando um “risco”, considerando:
- Distância aos inimigos.
- Proximidade de paredes ou cantos.
- Possíveis trajetórias de tiros.
- Escolhe o ponto mais seguro para se mover.
- Ajusta a velocidade e decide se avança ou recua para não ser previsível.


### Mira e Disparo
- Não atira onde o inimigo está, mas prevê onde ele estará:
- Simula passo a passo o movimento do inimigo.
- Calcula o tempo que o tiro leva para chegar.
- Ajusta a potência do tiro:
- Tiros fracos se o inimigo está longe ou o robô está com pouca energia.
- Tiros fortes em curta distância ou inimigos com baixa energia.


### Gerenciamento de Inimigos
- Mantém dados atualizados dos inimigos:
- Energia.
- Velocidade.
- Posição.
- Marca inimigos mortos para evitar disparos desnecessários.
- Escolhe sempre o alvo mais frágil ou próximo.

### Técnica Wave Surfing (1 vs 1)
- Cria ondas virtuais simulando tiros.
- Aprende onde o inimigo tende a se mover quando a onda chega.
- Ajusta a mira para aumentar a chance de acerto e diminuir o risco de ser atingido.

## 📜 Licença

Projeto educacional sem fins lucrativos. Livre para uso em estudos.
