#ifndef STRUCTS_H
#define STRUCTS_H

typedef struct {
  char *name;
  char *username;
  char *password; // Encriptada
  int key;        // Chave César
} User;

typedef struct {
  int id;
  int state;
  int train_id; // -1 se vazio
} Track;

typedef struct {
  int id;
} Train;

typedef struct {
  int id;
  char *user_id;
  char *action;
  char *timestamp;
} Log;

typedef struct {
  int buffer_len;
  int window_len; // Mediana móvel

  // Estado runtime (Circular Buffer)
  int *values;
  int head;
  int tail;
  int nelem;
} Sensor;

typedef struct {
  User *users;
  int num_users;

  Track *tracks;
  int num_tracks;

  Train *trains;
  int num_trains;

  Log *logs;
  int num_logs;
  int logs_capacity;

  Sensor sensor_temp;
  Sensor sensor_hum;
} StationManager;

#endif