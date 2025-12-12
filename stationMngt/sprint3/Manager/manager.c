#include "ui.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "asm.h"
#include "structs.h"

#define MAX_LINE_LEN 256

// New function prototypes
User *perform_login(StationManager *mgr);
void wait_for_data_from_sensors(char *buffer);
void update_sensors_data(StationManager *mgr, char *data);
void process_instruction(StationManager *mgr, int instruction);
void send_data_to_board(void *data);
void send_cmd_to_lightsigns(void *cmd);
void record_log(User *user, int instruction);

// Helper to remove newline
void trim_newline(char *str) {
  char *pos;
  if ((pos = strchr(str, '\n')) != NULL)
    *pos = '\0';
}

// Count items to allocate memory
int count_and_allocate(const char *filename, StationManager *mgr) {
  // Initialize struct to zero to ensure pointers are NULL
  memset(mgr, 0, sizeof(StationManager));

  FILE *f = fopen(filename, "r");
  if (!f)
    return 0;

  char line[MAX_LINE_LEN];
  mgr->num_users = 0;
  mgr->num_tracks = 0;
  mgr->num_trains = 0;
  // Logs começam vazios, definimos uma capacidade inicial arbitrária
  mgr->num_logs = 0;

  while (fgets(line, sizeof(line), f)) {
    if (strncmp(line, "USER", 4) == 0)
      mgr->num_users++;
    else if (strncmp(line, "TRACK", 5) == 0)
      mgr->num_tracks++;
    else if (strncmp(line, "TRAIN", 5) == 0)
      mgr->num_trains++;
  }
  fclose(f);

  // Alocar memória na Heap
  if (mgr->num_users > 0) {
    mgr->users = (User *)malloc(mgr->num_users * sizeof(User));
    if (!mgr->users)
      return 0; // Allocation failed
  }

  if (mgr->num_tracks > 0) {
    mgr->tracks = (Track *)malloc(mgr->num_tracks * sizeof(Track));
    if (!mgr->tracks)
      return 0; // Allocation failed
  }

  if (mgr->num_trains > 0) {
    mgr->trains = (Train *)malloc(mgr->num_trains * sizeof(Train));
    if (!mgr->trains)
      return 0; // Allocation failed
  }

  // Alocar logs (ex: capacidade para 50 logs iniciais)
  mgr->logs = (Log *)malloc(50 * sizeof(Log));
  if (!mgr->logs)
    return 0; // Allocation failed

  return 1;
}

// Load config from file
int load_config(const char *filename, StationManager *mgr) {

  // Tenta alocar a memória primeiro
  if (!count_and_allocate(filename, mgr)) {
    printf("[ERRO] Nao foi possivel abrir o ficheiro %s\n", filename);
    return 0;
  }

  FILE *f = fopen(filename, "r");
  if (!f)
    return 0; // Should not happen if count_and_allocate succeeded, but good
              // practice.

  char line[MAX_LINE_LEN];

  int idx_user = 0;
  int idx_track = 0;

  while (fgets(line, sizeof(line), f)) {
    trim_newline(line);

    // Parse User
    if (strncmp(line, "USER", 4) == 0) {
      char *token = strtok(line, ";");
      if (!token)
        continue;
      char *name_raw = strtok(NULL, ";");
      char *user_raw = strtok(NULL, ";");
      char *pass_raw = strtok(NULL, ";");
      char *key_str_ = strtok(NULL, ";");

      if (name_raw && user_raw && pass_raw && key_str_) {
        int key = atoi(key_str_);
        char encrypted_pass[100];

        int success = encrypt_data(pass_raw, key, encrypted_pass);
        if (!success) {
          strcpy(encrypted_pass, "INVALID");
        }

        char *final_name = strdup(name_raw);
        char *final_user = strdup(user_raw);
        char *final_pass = strdup(encrypted_pass);

        asm_fill_user_struct(&mgr->users[idx_user], final_name, final_user,
                             final_pass, key);
        idx_user++;
      }
    }

    // Parse Track
    else if (strncmp(line, "TRACK", 5) == 0) {
      char *token = strtok(line, ";");
      if (!token)
        continue;
      char *id_str = strtok(NULL, ";");
      if (id_str) {
        int id = atoi(id_str);
        mgr->tracks[idx_track].id = id;
        mgr->tracks[idx_track].state = 0;
        mgr->tracks[idx_track].train_id = -1;
        idx_track++;
      }
    }

    // Parse Sensor Temp
    else if (strncmp(line, "SENSOR_TEMP", 11) == 0) {
      char *token = strtok(line, ";");
      if (!token)
        continue;
      char *buf_len_str = strtok(NULL, ";");
      char *win_len_str = strtok(NULL, ";");

      if (buf_len_str && win_len_str) {
        int buf_len = atoi(buf_len_str);
        int win_len = atoi(win_len_str);

        mgr->sensor_temp.buffer_len = buf_len;
        mgr->sensor_temp.window_len = win_len;

        mgr->sensor_temp.values = (int *)calloc(buf_len, sizeof(int));
        mgr->sensor_temp.head = 0;
        mgr->sensor_temp.tail = 0;
        mgr->sensor_temp.nelem = 0;
      }
    }

    // Logica similar para SENSOR_HUM...
    else if (strncmp(line, "SENSOR_HUM", 10) == 0) {
      char *token = strtok(line, ";");
      if (!token)
        continue;
      char *buf_len_str = strtok(NULL, ";");
      char *win_len_str = strtok(NULL, ";");

      if (buf_len_str && win_len_str) {
        int buf_len = atoi(buf_len_str);
        int win_len = atoi(win_len_str);

        // Preenche configuração
        mgr->sensor_hum.buffer_len = buf_len;
        mgr->sensor_hum.window_len = win_len;

        // PREPARAÇÃO PARA BUFFER CIRCULAR (USAC05/06)
        // É crítico alocar este array agora, senão o Assembly vai falhar depois
        mgr->sensor_hum.values = (int *)calloc(buf_len, sizeof(int));
        mgr->sensor_hum.head = 0;
        mgr->sensor_hum.tail = 0;
        mgr->sensor_hum.nelem = 0;
      }
    }
  }

  fclose(f);
  return 1; // Sucesso
}

// IMPLEMENTATION OF GENERIC ALGORITHM FUNCTIONS
User *perform_login(StationManager *mgr) {
  char username[50];
  char password[50];
  char enc_pass[100];

  printf("\n=== LOGIN ===\n");
  while (1) {
    printf("Username: ");
    read_string_robust(username, 50);
    printf("Password: ");
    read_string_robust(password, 50);

    for (int i = 0; i < mgr->num_users; i++) {
      if (strcmp(mgr->users[i].username, username) == 0) {
        encrypt_data(password, mgr->users[i].key, enc_pass);

        if (strcmp(mgr->users[i].password, enc_pass) == 0) {
          printf("Login com sucesso! Bem-vindo %s.\n", mgr->users[i].name);
          return &mgr->users[i];
        }
      }
    }
    printf("Credenciais invalidas. Tente novamente.\n");
  }
}

// TODO: Serial comms (USAC13)
void wait_for_data_from_sensors(char *buffer) { (void)buffer; }

// TODO: Update structs (USAC05/06)
void update_sensors_data(StationManager *mgr, char *data) {
  (void)mgr;
  (void)data;
}

void process_instruction(StationManager *mgr, int instruction) {
  (void)mgr;
  if (instruction == -1)
    return;
  // TODO: Logic (USAC13-16)
}

void send_data_to_board(void *data) {
  (void)data;
  // TODO: Board output (USAC15)
}

void send_cmd_to_lightsigns(void *cmd) {
  (void)cmd;
  // TODO: LightSigns cmd (USAC14)
}

void record_log(User *user, int instruction) {
  (void)user;
  (void)instruction;
  // TODO: Logging (USAC12)
}

int main() {
  StationManager mgr;

  printf("A carregar configuracao...\n");
  if (!load_config("setup.txt", &mgr)) {
    return 1;
  }
  printf("Configuracao carregada. %d users.\n", mgr.num_users);

  // 1. LOGIN
  User *active_user = perform_login(&mgr);

  // 2. MAIN LOOP
  char sensor_buffer[100];

  while (1) {
    // A. Wait for Sensors
    wait_for_data_from_sensors(sensor_buffer);

    // B. Extract & Update
    update_sensors_data(&mgr, sensor_buffer);

    // C. Wait for UI Instruction
    int inst = ui_get_instruction();

    if (inst == 0)
      break; // Sair

    // D. Process
    process_instruction(&mgr, inst);

    // E. Output & Log
    send_data_to_board(NULL);
    send_cmd_to_lightsigns(NULL);
    record_log(active_user, inst);
  }

  if (mgr.users)
    free(mgr.users);
  if (mgr.tracks)
    free(mgr.tracks);
  if (mgr.trains)
    free(mgr.trains);
  if (mgr.logs)
    free(mgr.logs);
  if (mgr.sensor_temp.values)
    free(mgr.sensor_temp.values);
  if (mgr.sensor_hum.values)
    free(mgr.sensor_hum.values);

  printf("A encerrar.\n");
  return 0;
}
