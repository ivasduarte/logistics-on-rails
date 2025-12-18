#include "ui.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <errno.h>

#include "asm.h"
#include "structs.h"

#define MAX_LINE_LEN 256
#define SERIAL_PORT "/dev/ttyS2" // Adjust based on setup

// File descriptor for serial port
static int serial_fd = -1;

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
        mgr->users = (User *) malloc(mgr->num_users * sizeof(User));
        if (!mgr->users)
            return 0; // Allocation failed
    }

    if (mgr->num_tracks > 0) {
        mgr->tracks = (Track *) malloc(mgr->num_tracks * sizeof(Track));
        if (!mgr->tracks)
            return 0; // Allocation failed
    }

    if (mgr->num_trains > 0) {
        mgr->trains = (Train *) malloc(mgr->num_trains * sizeof(Train));
        if (!mgr->trains)
            return 0; // Allocation failed
    }

    // Alocar logs (ex: capacidade para 50 logs iniciais)
    mgr->logs = (Log *) malloc(50 * sizeof(Log));
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

                mgr->sensor_temp.values = (int *) calloc(buf_len, sizeof(int));
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
                mgr->sensor_hum.values = (int *) calloc(buf_len, sizeof(int));
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

// Serial communication functions
int init_serial_port(const char *port) {
    serial_fd = open(port, O_RDWR | O_NOCTTY | O_NDELAY);

    if (serial_fd == -1) {
        printf("[ERROR] Unable to open serial port %s\n", port);
        return 0;
    }

    // Serial port configuration
    struct termios options;
    tcgetattr(serial_fd, &options);

    // Baud rate set to 115200
    cfsetispeed(&options, B115200);
    cfsetospeed(&options, B115200);

    // 8N1 mode
    options.c_cflag &= ~PARENB;
    options.c_cflag &= ~CSTOPB;
    options.c_cflag &= ~CSIZE;
    options.c_cflag |= CS8;

    // Enabling receiver
    options.c_cflag |= (CLOCAL | CREAD);

    // Raw input mode
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);

    // Raw output mode
    options.c_oflag &= ~OPOST;

    // Applying settings
    tcsetattr(serial_fd, TCSANOW, &options);

    // Flush existing data
    tcflush(serial_fd, TCIOFLUSH);

    printf("[INFO] Serial port %s successfully initialized!\n", port);
    return 1;
}

void close_serial_port() {
    if (serial_fd != -1) {
        close(serial_fd);
        serial_fd = -1;
        printf("[INFO] Serial port closed!\n");
    }
}

// TODO: Serial comms (USAC13)
void wait_for_data_from_sensors(char *buffer) {
    if (serial_fd == -1) {
        printf("[ERROR] Serial port is not opened...\n");
        buffer[0] = '\0';
        return;
    }

    // Format and send GTH command (USAC04)
    char cmd[20];
    int res = format_command("gTh", 0, cmd); // becomes GTH

    if (!res) {
        printf("[ERROR] Unable to format GTH command!\n");
        buffer[0] = '\0';
        return;
    }

    // Send command to arduino
    strcat(cmd, "\n");
    ssize_t bytes_written = write(serial_fd, cmd, strlen(cmd));

    if (bytes_written < 0) {
        printf("[ERROR] Cannot send the command to the sensors: %s\n", strerror(errno));
        buffer[0] = '\0';
        return;
    }

    // Wait for response
    fd_set read_fds;
    struct timeval timeout;

    FD_ZERO(&read_fds);
    FD_SET(serial_fd, &read_fds);

    timeout.tv_sec = 5; // 5 second timeout
    timeout.tv_usec = 0;

    int select_result = select(serial_fd + 1, &read_fds, NULL, NULL, &timeout);

    if (select_result == -1) {
        printf("[ERROR] Error in select(): %s\n", strerror(errno));
        buffer[0] = '\0';
        return;
    } else if (select_result == 0) {
        printf("[WARNING] Timeout while waiting for sensors response...\n");
        buffer[0] = '\0';
        return;
    }

    // Read response
    int bytes_read = 0;
    int total_bytes = 0;
    char temp_char;

    while (total_bytes < 99) {
        bytes_read = read(serial_fd, &temp_char, 1);

        if (bytes_read > 0) {
            if (temp_char == '\n' || temp_char == '\r') {
                break;
            }
            buffer[total_bytes++] = temp_char;
        } else if (bytes_read == 0) {
            break; // no more data
        } else {
            printf("[ERROR] Unable to read serial port: %s\n", strerror(errno));
            buffer[0] = '\0';
            return;
        }
    }

    buffer[total_bytes] = '\0';
}

// TODO: Update structs (USAC05/06)
void update_sensors_data(StationManager *mgr, char *data) {
    if (!data || strlen(data) == 0) {
        printf("[WARNING] Sensors data is empty...\n");
        return;
    }

    // Temp variables for extraction
    char unit[20];
    int value;

    // Extract TEMPERATURE (USAC03)
    int temp_result = extract_data(data, "TEMP", unit, &value);

    if (temp_result == 1) {
        printf("[INFO] Extracted temperature: %d %s\n", value, unit);

        // add to circular buffer (USAC05)
        int buffer_full = enqueue_value(mgr->sensor_temp.values,
                                        mgr->sensor_temp.buffer_len,
                                        &mgr->sensor_temp.nelem,
                                        &mgr->sensor_temp.tail,
                                        &mgr->sensor_temp.head,
                                        value
        );

        if (buffer_full) {
            printf("[INFO] Temperature buffer is full. Oldest value removed!\n");
        }

        // Calculate moving median (if enough samples exist)
        if (mgr->sensor_temp.nelem >= mgr->sensor_temp.window_len) {
            // Extract last N values for median calculation
            int *median_array = (int *) malloc(mgr->sensor_temp.window_len * sizeof(int));

            if (median_array) {
                int extracted = move_n_to_array(mgr->sensor_temp.values,
                                                mgr->sensor_temp.buffer_len,
                                                &mgr->sensor_temp.nelem,
                                                &mgr->sensor_temp.tail,
                                                &mgr->sensor_temp.head,
                                                mgr->sensor_temp.window_len,
                                                median_array);

                if (extracted) {
                    int temp_median;
                    int median_result = median(median_array, mgr->sensor_temp.window_len, &temp_median);

                    if (median_result) {
                        printf("[INFO] Temperature median: %d %s\n", temp_median, unit);
                        mgr->sensor_temp.last_median = temp_median;

                        for (int i = 0; i < mgr->sensor_temp.window_len; i++) {
                            enqueue_value(mgr->sensor_temp.values,
                                          mgr->sensor_temp.buffer_len,
                                          &mgr->sensor_temp.nelem,
                                          &mgr->sensor_temp.tail,
                                          &mgr->sensor_temp.head,
                                          median_array[i]);
                        }
                    }
                }

                free(median_array);
            }
        }
    } else {
        printf("[WARNING] Unable to extract temperature data...\n");
    }

    // Extract HUMIDITY (USAC03)
    int hum_result = extract_data(data, "HUM", unit, &value);

    if (hum_result == 1) {
        printf("[INFO] Extracted Humidity: %d %s\n", value, unit);

        // Add to circular buffer (USAC05)
        int buffer_full = enqueue_value(
            mgr->sensor_hum.values,
            mgr->sensor_hum.buffer_len,
            &mgr->sensor_hum.nelem,
            &mgr->sensor_hum.tail,
            &mgr->sensor_hum.head,
            value
        );

        if (buffer_full) {
            printf("[INFO] Humidity buffer full\n");
        }

        // Calculate moving median if we have enough samples
        if (mgr->sensor_hum.nelem >= mgr->sensor_hum.window_len) {
            int *median_array = (int *) malloc(mgr->sensor_hum.window_len * sizeof(int));

            if (median_array) {
                int extracted = move_n_to_array(
                    mgr->sensor_hum.values,
                    mgr->sensor_hum.buffer_len,
                    &mgr->sensor_hum.nelem,
                    &mgr->sensor_hum.tail,
                    &mgr->sensor_hum.head,
                    mgr->sensor_hum.window_len,
                    median_array
                );

                if (extracted) {
                    int hum_median;
                    int median_result = median(median_array, mgr->sensor_hum.window_len, &hum_median);

                    if (median_result) {
                        printf("[INFO] Humidity median: %d %s\n", hum_median, unit);
                        mgr->sensor_hum.last_median = hum_median;

                        // Put values back into buffer
                        for (int i = 0; i < mgr->sensor_hum.window_len; i++) {
                            enqueue_value(
                                mgr->sensor_hum.values,
                                mgr->sensor_hum.buffer_len,
                                &mgr->sensor_hum.nelem,
                                &mgr->sensor_hum.tail,
                                &mgr->sensor_hum.head,
                                median_array[i]
                            );
                        }
                    }
                }

                free(median_array);
            }
        }
    } else {
        printf("[WARNING] Unable to extract humidity data\n");
    }
}

void process_instruction(StationManager *mgr, int instruction) {
    (void) mgr;
    if (instruction == -1)
        return;
    // TODO: Logic (USAC13-16)
}

void send_data_to_board(void *data) {
    (void) data;
    // TODO: Board output (USAC15)
}

void send_cmd_to_lightsigns(void *cmd) {
    (void) cmd;
    // TODO: LightSigns cmd (USAC14)
}

void record_log(User *user, int instruction) {
    (void) user;
    (void) instruction;
    // TODO: Logging (USAC12)
}

int main() {
    StationManager mgr;

    printf("A carregar configuracao...\n");
    if (!load_config("setup.txt", &mgr)) {
        return 1;
    }
    printf("Configuracao carregada. %d users.\n", mgr.num_users);

    // Inicializar Serial Port
    if (!init_serial_port(SERIAL_PORT)) {
	printf("[ERRO] Falha ao inicializar serial port!");
	return 1;
    }

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

    close_serial_port();

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
