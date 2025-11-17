# USBD15 - As a planner, I want to know how many lines depart from a given endpoint. #


**Objetivo:**
- Determinar quantas linhas ferroviárias têm início num endpoint específico, ou seja, quantas linhas têm esse endpoint como ponto inicial (startID).

**Solução Proposta:**

No modelo de dados, cada linha (Rail_Line) contém:

- um endpoint inicial → startID

- um endpoint final → endID

Para esta user story, apenas interessam as linhas cujo endpoint inicial coincide com o endpoint que queremos analisar.

A query realiza os seguintes passos:

- junta a tabela Rail_Line com Endpoint

- filtra pelo endpoint desejado

- devolve o total de linhas que partem desse endpoint