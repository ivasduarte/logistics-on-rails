# USBD16 - As a planner, I want to know the minimum length of double-track segments in a given line.

**Objetivo:**

- Determinar o comprimento mínimo dos segmentos ferroviários que pertencem a uma linha e que possuem via dupla (ou seja, segmentos onde numberTracks = 2).

**Solução Proposta:**

Cada segmento ferroviário (Line_Segment) está associado a uma linha (Rail_Line) e a um tipo de via (Line_Type).

Para identificar os segmentos de via dupla e calcular o seu menor comprimento, a solução segue estes passos:
- junta as tabelas Line_Segment, Rail_Line, Endpoint e Line_Type
- filtra apenas os segmentos cujo tipo de via tem duas vias (numberTracks = 2)
- seleciona a linha pretendida (hardcoded, conforme pedido pelo professor)
- utiliza a função MIN() para obter o menor comprimento entre os segmentos filtrados

**O resultado final apresenta:**
- o ID e nome da linha
- o endpoint inicial e final
- o comprimento mínimo dos segmentos de via dupla
