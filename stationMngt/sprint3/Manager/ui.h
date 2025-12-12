#ifndef UI_H
#define UI_H

// Retorna a instrução/opção do utilizador (ou -1 em erro)
int ui_get_instruction();

// Função robusta para ler inteiros
// Retorna 1 se sucesso, 0 se erro (ex: input não numérico)
int read_int_robust(int *value);

// Função robusta para ler strings (remove newline automaticamente)
void read_string_robust(char *buffer, int max_len);

#endif
