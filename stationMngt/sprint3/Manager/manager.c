#include "ui.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <errno.h>
#include <time.h>

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

void initialize_track_lights(StationManager *mgr);

void manage_railway_tracks(StationManager *mgr);

void process_instruction(StationManager *mgr, int instruction);

void send_data_to_board(void *data);

void send_cmd_to_lightsigns(void *cmd);

void record_log(User *user, int instruction);

void interactive_lightsign_control(void);

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

void wait_for_data_from_sensors(char *buffer) {
    if (serial_fd == -1) {
        printf("[ERROR] Serial port is not opened...\n");
        buffer[0] = '\0';
        return;
    }

    // Format and send GTH command (USAC04)
    char cmd[20];
    char op_buffer[20];
    strcpy(op_buffer, "gTh"); // copia a constante para um array na stack
    int res = format_command(op_buffer, 0, cmd);

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

void update_sensors_data(StationManager *mgr, char *data) {
    if (!data || strlen(data) == 0) {
        printf("[WARNING] Sensors data is empty\n");
        return;
    }

    // Temp variables for extraction
    char unit[20];
    int value;

    // ===== TEMPERATURE =====
    int temp_result = extract_data(data, "TEMP", unit, &value);

    if (temp_result == 1) {
        printf("[INFO] Extracted temperature: %d %s\n", value, unit);

        // Add to circular buffer (USAC05)
        int buffer_full = enqueue_value(
            mgr->sensor_temp.values,
            mgr->sensor_temp.buffer_len,
            &mgr->sensor_temp.nelem,
            &mgr->sensor_temp.tail,
            &mgr->sensor_temp.head,
            value
        );

        // Calculate moving median if we have enough samples
        if (mgr->sensor_temp.nelem >= mgr->sensor_temp.window_len) {
            printf("[INFO] Calculating median with %d samples...\n", mgr->sensor_temp.window_len);

            // Create array with last window_len values
            int *temp_array = (int *) malloc(mgr->sensor_temp.window_len * sizeof(int));

            if (temp_array) {
                // Calculate starting index for the window
                // If we have 10 slots and want last 5 values, and head is at 7:
                // We want indices: 2, 3, 4, 5, 6 (the 5 values before position 7)
                int start_idx;
                if (mgr->sensor_temp.nelem < mgr->sensor_temp.buffer_len) {
                    // Buffer not yet full, values are from 0 to head-1
                    start_idx = mgr->sensor_temp.head - mgr->sensor_temp.window_len;
                    if (start_idx < 0) start_idx = 0; // Safety check
                } else {
                    // Buffer is full, use circular indexing
                    start_idx = (mgr->sensor_temp.head - mgr->sensor_temp.window_len + mgr->sensor_temp.buffer_len) %
                                mgr->sensor_temp.buffer_len;
                }

                // Copy values
                for (int i = 0; i < mgr->sensor_temp.window_len; i++) {
                    int idx = (start_idx + i) % mgr->sensor_temp.buffer_len;
                    temp_array[i] = mgr->sensor_temp.values[idx];
                }

                // Calculate median
                int temp_median;
                int median_result = median(temp_array, mgr->sensor_temp.window_len, &temp_median);

                if (median_result) {
                    printf("[INFO] Temperature median: %d %s\n", temp_median, unit);
                    mgr->sensor_temp.last_median = temp_median;
                } else {
                    printf("[ERROR] Median calculation failed!\n");
                }

                free(temp_array);
            } else {
                printf("[ERROR] Failed to allocate memory for median calculation\n");
            }
        } else {
            printf("[INFO] Need %d more samples for median (%d/%d)\n",
                   mgr->sensor_temp.window_len - mgr->sensor_temp.nelem,
                   mgr->sensor_temp.nelem,
                   mgr->sensor_temp.window_len);
        }
    } else {
        printf("[WARNING] Unable to extract temperature data\n");
    }

    // ===== HUMIDITY =====
    int hum_result = extract_data(data, "HUM", unit, &value);

    if (hum_result == 1) {
        printf("[INFO] Extracted humidity: %d %s\n", value, unit);

        // Add to circular buffer
        int buffer_full = enqueue_value(
            mgr->sensor_hum.values,
            mgr->sensor_hum.buffer_len,
            &mgr->sensor_hum.nelem,
            &mgr->sensor_hum.tail,
            &mgr->sensor_hum.head,
            value
        );

        // Calculate moving median
        if (mgr->sensor_hum.nelem >= mgr->sensor_hum.window_len) {
            printf("[INFO] Calculating median with %d samples...\n", mgr->sensor_hum.window_len);

            int *temp_array = (int *) malloc(mgr->sensor_hum.window_len * sizeof(int));

            if (temp_array) {
                int start_idx;
                if (mgr->sensor_hum.nelem < mgr->sensor_hum.buffer_len) {
                    start_idx = mgr->sensor_hum.head - mgr->sensor_hum.window_len;
                    if (start_idx < 0) start_idx = 0;
                } else {
                    start_idx = (mgr->sensor_hum.head - mgr->sensor_hum.window_len + mgr->sensor_hum.buffer_len) % mgr->
                                sensor_hum.buffer_len;
                }

                for (int i = 0; i < mgr->sensor_hum.window_len; i++) {
                    int idx = (start_idx + i) % mgr->sensor_hum.buffer_len;
                    temp_array[i] = mgr->sensor_hum.values[idx];
                }

                int hum_median;
                int median_result = median(temp_array, mgr->sensor_hum.window_len, &hum_median);

                if (median_result) {
                    printf("[INFO] Humidity median: %d %s\n", hum_median, unit);
                    mgr->sensor_hum.last_median = hum_median;
                } else {
                    printf("[ERROR] Median calculation failed!\n");
                }

                free(temp_array);
            } else {
                printf("[ERROR] Failed to allocate memory for median calculation\n");
            }
        } else {
            printf("[INFO] Need %d more samples for median (%d/%d)\n",
                   mgr->sensor_hum.window_len - mgr->sensor_hum.nelem,
                   mgr->sensor_hum.nelem,
                   mgr->sensor_hum.window_len);
        }
    } else {
        printf("[WARNING] Unable to extract humidity data\n");
    }
}

void send_cmd_to_lightsigns(void *cmd) {
    if (!cmd) {
        printf("[ERRO] Comando nulo recebido!\n");
        return;
    }

    // Verificar se a porta serial está aberta
    if (serial_fd == -1) {
        printf("[ERRO] Porta serial não está aberta!\n");
        return;
    }

    char *cmd_str = (char *)cmd;
    printf("[LIGHTSIGNS] Processando comando: %s\n", cmd_str);

    // Fazer parse ao comando para extrair a cor e qual a track
    char operation[10];
    int track_number = -1;

    if (sscanf(cmd_str, "%[^,],%d", operation, &track_number) != 2) {
        printf("[ERRO] Formato de comando inválido! Use: CMD,XX\n");
        printf("[EXEMPLO] RE,01 ou GE,02\n");
        return;
    }

    // Validar número da track
    if (track_number < 1 || track_number > 2) {
        printf("[ERRO] Track inválida! Use apenas 1 ou 2.\n");
        return;
    }

    // Formatar comando usando a função Assembly (USAC04)
    char formatted_cmd[20];
    int result = format_command(operation, track_number, formatted_cmd);

    if (!result) {
        printf("[ERRO] Falha ao formatar comando!\n");
        printf("[INFO] Operação: %s | Via: %d\n", operation, track_number);
        return;
    }

    printf("[LIGHTSIGNS] Comando formatado: %s\n", formatted_cmd);

    // Adicionar newline ao comando (Arduino espera por um \n)
    strcat(formatted_cmd, "\n");

    // Enviar comando via Serial
    ssize_t bytes_written = write(serial_fd, formatted_cmd, strlen(formatted_cmd));

    if (bytes_written < 0) {
        printf("[ERRO] Não foi possível enviar comando: %s\n", strerror(errno));
        return;
    }

    printf("[SUCESSO] Comando enviado para Arduino: %s", formatted_cmd);
    usleep(100000); // 100ms
}

void interactive_lightsign_control(void) {
    char operation[10];
    int track_number;
    char cmd_buffer[20];

    printf("\n=== CONTROLO DE SINAIS LUMINOSOS ===\n");
    printf("Comandos disponíveis:\n");
    printf("  RE - LED Vermelho\n");
    printf("  YE - LED Amarelo\n");
    printf("  GE - LED Verde\n");
    printf("  RB - LED Vermelho A Piscar\n");
    printf("\n");

    printf("Operação (RE/YE/GE/RB): ");
    if (scanf("%9s", operation) != 1) {
        printf("[ERRO] Falha na leitura da operação!\n");
        return;
    }

    printf("Número da Track (1-2): ");
    if (scanf("%d", &track_number) != 1) {
        printf("[ERRO] Falha na leitura do número da track!\n");
        return;
    }

    // Limpar buffer do stdin
    int c;
    while ((c = getchar()) != '\n' && c != EOF);

    // Construir comando
    snprintf(cmd_buffer, sizeof(cmd_buffer), "%s,%02d", operation, track_number);

    // Enviar comando
    send_cmd_to_lightsigns(cmd_buffer);
}

void initialize_track_lights(StationManager *mgr) {
    char cmd_buffer[20];

    printf("[INFO] Initializing track lights...\n");

    for (int i = 0; i < mgr->num_tracks; i++) {
        // Todas as tracks começam FREE (estado 0), então LED Verde
        snprintf(cmd_buffer, sizeof(cmd_buffer), "GE,%02d", mgr->tracks[i].id);
        send_cmd_to_lightsigns(cmd_buffer);

        printf("[INFO] Track %d set to GREEN (FREE)\n", mgr->tracks[i].id);

        // Pequeno delay entre comandos para o Arduino processar
        usleep(200000); // 200ms
    }

    printf("[INFO] All track lights initialized!\n\n");
}

void manage_railway_tracks(StationManager *mgr) {
    int option = 0;
    int track_id = 0;
    int train_id = 0;
    char cmd_buffer[20];

    printf("\n=== GERIR VIAS FERREAS (USAC16) ===\n");
    printf("1. Atribuir via a comboio (Chegada)\n");
    printf("2. Definir via como NAO OPERACIONAL\n");
    printf("3. Definir via como LIVRE\n");
    printf("4. Dar ordem de partida\n");
    printf("5. Confirmar chegada do comboio\n");
    printf("0. Voltar\n");
    printf("Opcao: ");

    if (!read_int_robust(&option)) return;
    if (option == 0) return;

    switch (option) {
        case 1: // Assign Track (Arrival)
            printf("ID do Comboio a chegar: ");
            if (read_int_robust(&train_id)) {
                int found = 0;
                for (int i = 0; i < mgr->num_tracks; i++) {
                    // Procura via LIVRE (Estado 0)
                    if (mgr->tracks[i].state == 0) {
                        mgr->tracks[i].state = 2; // 2 = ASSIGNED (Amarelo no teu Board atual)
                        mgr->tracks[i].train_id = train_id;

                        // Hardware: Vermelho (RE) porque vai ficar ocupada
                        snprintf(cmd_buffer, sizeof(cmd_buffer), "YE,%02d", mgr->tracks[i].id);
                        send_cmd_to_lightsigns(cmd_buffer);

                        printf("[SUCESSO] Comboio %d atribuido a Via %d.\n", train_id, mgr->tracks[i].id);
                        found = 1;
                        break;
                    }
                }

                if (!found) {
                    printf("[ALERTA] !!! PARAGEM DE EMERGENCIA !!! Nao ha vias disponiveis!\n");
                    // Hardware: Piscar Vermelho em TODAS as vias
                    for(int k=0; k < mgr->num_tracks; k++) {
                         snprintf(cmd_buffer, sizeof(cmd_buffer), "RB,%02d", mgr->tracks[k].id);
                         send_cmd_to_lightsigns(cmd_buffer);
                    }
                }
            }
            break;

        case 2: // Set Non-Operational
            printf("ID da Via a fechar: ");
            if (read_int_robust(&track_id)) {
                int found = 0;
                for (int i = 0; i < mgr->num_tracks; i++) {
                    if (mgr->tracks[i].id == track_id) {
                        found = 1;
                        mgr->tracks[i].state = 3; // 3 = INOPERATIVE

                        // Hardware: Piscar Vermelho (RB)
                        snprintf(cmd_buffer, sizeof(cmd_buffer), "RB,%02d", track_id);
                        send_cmd_to_lightsigns(cmd_buffer);

                        printf("[INFO] Via %d marcada como INOPERATIVA.\n", track_id);
                        break;  // ADICIONAR break aqui!
                    }
                }

                if (!found) {
                    printf("[ERRO] Via %d nao existe!\n", track_id);
                }
            }
            break;

        case 3: // Set Free
            printf("ID da Via a libertar: ");
            if (read_int_robust(&track_id)) {
                int found = 0;
                for (int i = 0; i < mgr->num_tracks; i++) {
                    if (mgr->tracks[i].id == track_id) {
                        found = 1;
                        mgr->tracks[i].state = 0; // 0 = FREE
                        mgr->tracks[i].train_id = -1;

                        // Hardware: Verde (GE) - via livre
                        snprintf(cmd_buffer, sizeof(cmd_buffer), "GE,%02d", track_id);
                        send_cmd_to_lightsigns(cmd_buffer);

                        printf("[INFO] Via %d esta agora LIVRE.\n", track_id);
                        break;  // ADICIONAR break aqui!
                    }
                }

                if (!found) {
                    printf("[ERRO] Via %d nao existe!\n", track_id);
                }
            }
            break;

        case 4: // Departure Order
            printf("ID da Via para partida: ");
            if (read_int_robust(&track_id)) {
                int found = 0;
                for (int i = 0; i < mgr->num_tracks; i++) {
                    if (mgr->tracks[i].id == track_id) {
                        found = 1;

                        if (mgr->tracks[i].train_id == -1) {
                            printf("[ERRO] Nao ha comboio na via %d.\n", track_id);
                        } else if (mgr->tracks[i].state != 1) {
                            printf("[AVISO] Via %d nao esta OCUPADA. Estado atual: %d\n",
                                   track_id, mgr->tracks[i].state);
                        } else {
                            printf("[INFO] Ordem de partida dada ao comboio %d na Via %d.\n",
                                   mgr->tracks[i].train_id, track_id);

                            // Hardware: Verde (GE) para o comboio sair
                            snprintf(cmd_buffer, sizeof(cmd_buffer), "GE,%02d", track_id);
                            send_cmd_to_lightsigns(cmd_buffer);

                            // Liberta a via (Estado 0)
                            mgr->tracks[i].state = 0;
                            mgr->tracks[i].train_id = -1;
                        }
                        break;
                    }
                }

                if (!found) {
                    printf("[ERRO] Via %d nao existe!\n", track_id);
                }
            }
            break;

        case 5: // Confirm train arrival (ASSIGNED → BUSY)
            printf("ID da Via onde o comboio chegou: ");
            if (read_int_robust(&track_id)) {
                int found = 0;
                for (int i = 0; i < mgr->num_tracks; i++) {
                    if (mgr->tracks[i].id == track_id) {
                        found = 1;

                        if (mgr->tracks[i].state != 2) {
                            printf("[ERRO] A via %d nao tem comboio ATRIBUIDO (estado atual: %d).\n",
                                   track_id, mgr->tracks[i].state);
                            break;
                        }

                        mgr->tracks[i].state = 1; // BUSY

                        // Hardware: Vermelho (RE) - via ocupada
                        snprintf(cmd_buffer, sizeof(cmd_buffer), "RE,%02d", track_id);
                        send_cmd_to_lightsigns(cmd_buffer);

                        printf("[INFO] Comboio %d chegou a Via %d (OCUPADA).\n",
                               mgr->tracks[i].train_id, track_id);
                        break;
                    }
                }

                if (!found) {
                    printf("[ERRO] Via %d nao existe!\n", track_id);
                }
            }
            break;

        default:
            printf("Opcao invalida.\n");
    }

    send_data_to_board(mgr);
}

void process_instruction(StationManager *mgr, int instruction) {
    if (instruction == -1) return;

    switch (instruction) {
        case 1: // USAC13
            printf("\n========================================\n");
            printf("       SENSOR DATA (USAC13)\n");
            printf("========================================\n");

            // Temperature
            printf("\n--- Temperature Readings ---\n");
            if (mgr->sensor_temp.nelem > 0) {
                printf("Total samples collected: %d/%d\n",
                       mgr->sensor_temp.nelem,
                       mgr->sensor_temp.buffer_len);

                printf("\nAll values in buffer:\n");
                for (int i = 0; i < mgr->sensor_temp.nelem && i < mgr->sensor_temp.buffer_len; i++) {
                    printf("  [%d] = %d C\n", i, mgr->sensor_temp.values[i]);
                }

                if (mgr->sensor_temp.nelem >= mgr->sensor_temp.window_len) {
                    printf("\n** Moving Median: %d C **\n", mgr->sensor_temp.last_median);
                } else {
                    printf("\n(Need %d samples for median, have %d)\n",
                           mgr->sensor_temp.window_len,
                           mgr->sensor_temp.nelem);
                }
            } else {
                printf("No temperature data available.\n");
            }

            // Humidity
            printf("\n--- Humidity Readings ---\n");
            if (mgr->sensor_hum.nelem > 0) {
                printf("Total samples collected: %d/%d\n",
                       mgr->sensor_hum.nelem,
                       mgr->sensor_hum.buffer_len);

                printf("\nAll values in buffer:\n");
                for (int i = 0; i < mgr->sensor_hum.nelem && i < mgr->sensor_hum.buffer_len; i++) {
                    printf("  [%d] = %d %%\n", i, mgr->sensor_hum.values[i]);
                }

                if (mgr->sensor_hum.nelem >= mgr->sensor_hum.window_len) {
                    printf("\n** Moving Median: %d %% **\n", mgr->sensor_hum.last_median);
                } else {
                    printf("\n(Need %d samples for median, have %d)\n",
                           mgr->sensor_hum.window_len,
                           mgr->sensor_hum.nelem);
                }
            } else {
                printf("No humidity data available.\n");
            }

            printf("\n========================================\n");
            break;

        case 2:
            interactive_lightsign_control();
            break;

        case 3:
            printf("[USAC15] Displaying synoptic board...\n");
            send_data_to_board(mgr);
            break;

        case 4:
            printf("\n");
            manage_railway_tracks(mgr);
            break;

        default:
            printf("Unknown instruction!\n");
            break;
    }
}

void send_data_to_board(void *data) {
    StationManager *mgr = (StationManager *) data;

    if (!mgr) {
        printf("[ERROR] No data to display on board\n");
        return;
    }

    // Clear screen effect (print some newlines for separation)
    printf("\n\n");

    // Top border
    printf("╔══════════════════════════════════════════════════════════════════╗\n");
    printf("║             == RAILWAY STATION SYNOPTIC BOARD ==                 ║\n");
    printf("╠══════════════════════════════════════════════════════════════════╣\n");

    // Sensor Data Section
    printf("║                        SENSOR READINGS                           ║\n");
    printf("╠══════════════════════════════════════════════════════════════════╣\n");

    // Temperature display with visual bar
    // Temperature display
    printf("║ TEMPERATURE:                                                     ║\n");
    if (mgr->sensor_temp.nelem > 0) {
        // Get most recent value
        int last_idx;
        if (mgr->sensor_temp.nelem < mgr->sensor_temp.buffer_len) {
            // Buffer not full - last value is at (head - 1), but head might be 0
            last_idx = (mgr->sensor_temp.head == 0) ? 0 : (mgr->sensor_temp.head - 1);
        } else {
            // Buffer is full - use circular indexing
            last_idx = (mgr->sensor_temp.head - 1 + mgr->sensor_temp.buffer_len) % mgr->sensor_temp.buffer_len;
        }
        int last_temp = mgr->sensor_temp.values[last_idx];

        printf("║ ->   Current: %2d C  ", last_temp);

        // Visual temperature bar (scaled 0-50°C)
        int bar_len = (last_temp * 20) / 50;
        if (bar_len < 0) bar_len = 0;
        if (bar_len > 20) bar_len = 20;

        printf("[");
        for (int i = 0; i < bar_len; i++) printf("█");
        for (int i = bar_len; i < 20; i++) printf("░");
        printf("]");
        printf("                       ║\n");

        if (mgr->sensor_temp.nelem >= mgr->sensor_temp.window_len) {
            printf("║      Median:  %2d C  (last %d samples)                            ║\n",
                   mgr->sensor_temp.last_median,
                   mgr->sensor_temp.window_len);
        } else {
            printf("║      Median:  N/A   (need %d samples)                             ║\n",
                   mgr->sensor_temp.window_len);
        }
    } else {
        printf("║      Current: -- C  [No data available]                        ║\n");
        printf("║      Median:  -- C                                             ║\n");
    }

    printf("║                                                                  ║\n");

    // Humidity display with visual bar
    // Humidity display
    printf("║  ->  HUMIDITY:                                                   ║\n");
    if (mgr->sensor_hum.nelem > 0) {
        // Get most recent value
        int last_idx;
        if (mgr->sensor_hum.nelem < mgr->sensor_hum.buffer_len) {
            // Buffer not full - last value is at (head - 1), but head might be 0
            last_idx = (mgr->sensor_hum.head == 0) ? 0 : (mgr->sensor_hum.head - 1);
        } else {
            // Buffer is full - use circular indexing
            last_idx = (mgr->sensor_hum.head - 1 + mgr->sensor_hum.buffer_len) % mgr->sensor_hum.buffer_len;
        }
        int last_hum = mgr->sensor_hum.values[last_idx];

        printf("║      Current: %2d%%   ", last_hum);

        // Visual humidity bar (scaled 0-100%)
        int bar_len = (last_hum * 20) / 100;
        if (bar_len < 0) bar_len = 0;
        if (bar_len > 20) bar_len = 20;

        printf("[");
        for (int i = 0; i < bar_len; i++) printf("█");
        for (int i = bar_len; i < 20; i++) printf("░");
        printf("]");
        printf("                       ║\n");

        if (mgr->sensor_hum.nelem >= mgr->sensor_hum.window_len) {
            printf("║      Median:  %2d%%   (last %d samples)                           ║\n",
                   mgr->sensor_hum.last_median,
                   mgr->sensor_hum.window_len);
        } else {
            printf("║      Median:  N/A   (need %d samples)                             ║\n",
                   mgr->sensor_hum.window_len);
        }
    } else {
        printf("║      Current: --%%   [No data available]                        ║\n");
        printf("║      Median:  --%%                                              ║\n");
    }

    printf("╠══════════════════════════════════════════════════════════════════╣\n");

    // Track Status Section
    printf("║                        TRACK STATUS                              ║\n");
    printf("╠══════════════════════════════════════════════════════════════════╣\n");

    if (mgr->num_tracks > 0) {
        for (int i = 0; i < mgr->num_tracks; i++) {
            printf("║  Track %d: ", mgr->tracks[i].id);

            switch (mgr->tracks[i].state) {
                case 0: // Free
                    printf("🟢 FREE         ");
                    break;
                case 1: // Busy
                    printf("🔴 BUSY         ");
                    break;
                case 2: // Assigned
                    printf("🟡 ASSIGNED     ");
                    break;
                case 3: // Inoperative
                    printf("⚫ INOPERATIVE  ");
                    break;
                default:
                    printf("⚪ UNKNOWN      ");
                    break;
            }

            if (mgr->tracks[i].train_id >= 0) {
                printf("Train: %d", mgr->tracks[i].train_id);
                // Padding
                int train_id_len = 0;
                int temp_id = mgr->tracks[i].train_id;
                do {
                    train_id_len++;
                    temp_id /= 10;
                } while (temp_id > 0);
                for (int j = 0; j < (20 - train_id_len); j++) printf(" ");
                printf("║\n");
            } else {
                printf("Train: ---                             ║\n");
            }
        }
    } else {
        printf("║      No tracks configured                                       ║\n");
    }

    printf("╠══════════════════════════════════════════════════════════════════╣\n");

    // System Info
    printf("║  ℹ️  Buffer Status: Temp: %d/%d  |  Hum: %d/%d                      ║\n",
           mgr->sensor_temp.nelem, mgr->sensor_temp.buffer_len,
           mgr->sensor_hum.nelem, mgr->sensor_hum.buffer_len);

    // Bottom border
    printf("╚══════════════════════════════════════════════════════════════════╝\n");
    printf("\n");
}

void record_log(User *user, int instruction) {
    if (user == NULL || instruction <= 0) return;

    // Mapear o número da instrução para uma descrição legível
    char *action_desc;
    switch (instruction) {
        case 1: action_desc = "CONSULTAR_DADOS_SENSORES (USAC13)"; break;
        case 2: action_desc = "CONTROLAR_SINAIS_LUMINOSOS (USAC14)"; break;
        case 3: action_desc = "ENVIAR_MENSAGEM_BOARD (USAC15)"; break;
        case 4: action_desc = "GERIR_VIAS_FERREAS (USAC16)"; break;
        default: action_desc = "ACAO_NAO_IDENTIFICADA"; break;
    }

    // Obter o tempo atual do sistema
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    char timestamp[20];
    // Formato: AAAA-MM-DD HH:MM:SS
    strftime(timestamp, sizeof(timestamp), "%Y-%m-%d %H:%M:%S", t);

    // Escrever no ficheiro .txt (Modo append para adicionar ao fim)
    FILE *f = fopen("log_atividades.txt", "a");
    if (f == NULL) {
        printf("[ERRO] Falha ao abrir ficheiro log_atividades.txt para escrita.\n");
        return;
    }

    // Estrutura: [Data/Hora] User: nome (username) - Ação: Descrição
    fprintf(f, "[%s] User: %s (%s) - Ação: %s\n",
            timestamp, user->name, user->username, action_desc);

    fclose(f);
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

    // Inicializar LEDs a verde
    initialize_track_lights(&mgr);

    // 1. LOGIN
    User *active_user = perform_login(&mgr);

    // 2. MAIN LOOP
    char sensor_buffer[100];
    int first_read = 1; // Flag to do initial read

    while (1) {
        // A. Wait for Sensors (only on first iteration or when needed)
        if (first_read) {
            printf("\n[INFO] Performing initial sensor reading...\n");
            wait_for_data_from_sensors(sensor_buffer);

            if (strlen(sensor_buffer) > 0) {
                update_sensors_data(&mgr, sensor_buffer);
            } else {
                printf("[WARNING] Initial sensor read failed\n");
            }

            first_read = 0;
        }

        // C. Wait for UI Instruction
        int inst = ui_get_instruction();

        if (inst == 0)
            break; // Sair

        // D. Process instruction
        process_instruction(&mgr, inst);

        // E. Log (removed board call here - it's called inside process_instruction)
        record_log(active_user, inst);

        // Read sensors again for next iteration (after user sees results)
        printf("\n[INFO] Reading sensors for next cycle...\n");
        wait_for_data_from_sensors(sensor_buffer);

        if (strlen(sensor_buffer) > 0) {
            update_sensors_data(&mgr, sensor_buffer);
        } else {
            printf("[WARNING] Sensor read returned no data\n");
        }
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
