#ifndef ASM_H
#define ASM_H

// Precisamos de incluir o structs.h porque uma das funções 
// recebe um ponteiro para a estrutura "User"
#include "structs.h"

// USAC01: Encriptação (Cifra de César)
// Retorna 1 em sucesso, 0 em falha
int encrypt_data(char* in, int key, char *out);

// USAC02: Decifragem
int decrypt_data(char* in, int key, char *out);

// USAC03: Extração de dados de sensores (ex: "TEMP&unit:celsius&value:20")
int extract_data(char* str, char* token, char* unit, int* value);

// USAC04: Formatação de comandos para o LightSigns (ex: "RE, 05")
int format_command(char* op, int n, char *cmd);

// USAC05: Inserir valor no Buffer Circular
int enqueue_value(int* buffer, int length, int *nelem, int* tail, int* head, int value);

// USAC06: Remover valor do Buffer Circular
int dequeue_value(int* buffer, int length, int *nelem, int* tail, int* head, int *value);

// USAC07: Mover N elementos mais antigos para um array
int move_n_to_array(int* buffer, int length, int *nelem, int *tail, int *head, int n, int *array);

// USAC08: Ordenar array (1=Ascendente, 0=Descendente)
int sort_array(int* vec, int length, char order);

// USAC09: Calcular a mediana (após ordenar)
int median(int* vec, int length, int *me);

// =============================================================
//  FUNÇÕES DO SPRINT 3 (Novos Requisitos)
// =============================================================

/**
 * Função OBRIGATÓRIA que recebe uma struct como parâmetro.
 * Esta função deve ser implementada em Assembly.
 * * Objetivo: Preencher os campos da struct User diretamente na memória.
 * * @param u        Ponteiro para a struct User (registo a0)
 * @param name     Ponteiro para a string nome (registo a1)
 * @param username Ponteiro para a string username (registo a2)
 * @param enc_pass Ponteiro para a string password já encriptada (registo a3)
 * @param key      Inteiro da chave de cifra (registo a4)
 */
void asm_fill_user_struct(User *u, char *name, char *username, char *enc_pass, int key);

#endif
