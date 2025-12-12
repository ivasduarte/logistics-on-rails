#include "ui.h"
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Limpa o buffer de entrada em caso de erro
void clean_stdin() {
  int c;
  while ((c = getchar()) != '\n' && c != EOF)
    ;
}

// Implementação: Leitura robusta de inteiros
int read_int_robust(int *value) {
  char buffer[64];
  if (fgets(buffer, sizeof(buffer), stdin) == NULL)
    return 0;

  // Verificar se é apenas um newline
  if (buffer[0] == '\n')
    return 0;

  // Verificar se todos os caracteres são dígitos (ignorando o \n final)
  for (int i = 0; buffer[i] != '\0'; i++) {
    if (buffer[i] == '\n') {
      buffer[i] = '\0';
      break;
    }
    if (!isdigit(buffer[i])) {
      return 0; // Contém caracteres não numéricos
    }
  }

  *value = atoi(buffer);
  return 1;
}

void read_string_robust(char *buffer, int max_len) {
  if (fgets(buffer, max_len, stdin) != NULL) {
    size_t len = strlen(buffer);
    if (len > 0 && buffer[len - 1] == '\n') {
      buffer[len - 1] = '\0';
    } else {
      clean_stdin(); // Limpar resto da linha se excedeu max_len
    }
  }
}

// Retorna a opção escolhida pelo utilizador
int ui_get_instruction() {
  int option = -1;

  printf("\n=== STATION MANAGER UI ===\n");
  printf("1. USAC13: Obter Dados dos Sensores\n");
  printf("2. USAC14: Controlar Sinais Luminosos\n");
  printf("3. USAC15: Enviar Mensagem para Board\n");
  printf("4. USAC16: Gerir Vias Ferras\n");
  printf("0. Sair\n");
  printf("Opcao: ");

  if (!read_int_robust(&option)) {
    printf("[ERRO] Input invalido! Insira um numero.\n");
    return -1; // -1 indica erro/tente novamente
  }
  return option;
}
