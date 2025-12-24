#include "DHT.h"
#include <arduino-timer.h>

// --- 1. CONFIGURAÇÃO DOS PINOS ---

// Sensor
#define DHTPIN 28
#define DHTTYPE DHT11

// Track 1 (Sua nova configuração)
#define T1_VERDE 15
#define T1_AMARELO 13  // Atenção: Pulou o GP14
#define T1_VERMELHO 12

// Track 2 (Configuração anterior)
#define T2_VERDE 16
#define T2_AMARELO 18
#define T2_VERMELHO 19

// --- NOVAS VARIÁVEIS PARA O RB ---
bool track1_blinking = false;
bool track2_blinking = false;
unsigned long last_blink_time = 0;
bool blink_state = LOW;

// --- 2. CONFIGURAÇÃO DA MOVING MEDIAN ---
#define MEDIAN_WINDOW 5

// --- 3. INICIALIZAÇÃO DE OBJETOS ---
DHT dht(DHTPIN, DHTTYPE);

// Buffers para armazenar últimas leituras
int temp_buffer[MEDIAN_WINDOW];
int hum_buffer[MEDIAN_WINDOW];
int buffer_index = 0;
int readings_count = 0;

// --- 4. FUNÇÕES AUXILIARES ---

// Ordena array
void sortArray(int *arr, int length) {
  for (int i = 0; i < length - 1; i++) {
    for (int j = 0; j < length - i - 1; j++) {
      if (arr[j] > arr[j + 1]) {
        int temp = arr[j];
        arr[j] = arr[j + 1];
        arr[j + 1] = temp;
      }
    }
  }
}

// Cálculo da mediana
int calculateMedian(int *arr, int length) {
  // Criar cópia temporária para não alterar o buffer original
  int temp_arr[MEDIAN_WINDOW];
  for (int i = 0; i < length; i++) {
    temp_arr[i] = arr[i];
  }

  sortArray(temp_arr, length);

  if (length % 2 == 0) {
    // Par: média dos dois valores centrais
    return (temp_arr[length / 2 - 1] + temp_arr[length / 2]) / 2;
  } else {
    // Ímpar: valor central
    return temp_arr[length / 2];
  }
}

// Adiciona leituras ao buffer e calcula Moving Median
int addReadingAndGetMedian(int *buffer, int value) {
  // Adicionar valor ao buffer circular
  buffer[buffer_index] = value;
  buffer_index = (buffer_index + 1) % MEDIAN_WINDOW;

  // Incrementar contador até preencher o buffer
  if (readings_count < MEDIAN_WINDOW) {
    readings_count++;
  }

  // Calcular mediana com os valores disponíveis
  return calculateMedian(buffer, readings_count);
}

// Lê sensores e responde com formato especificado (USAC13)
void sendSensorData() {
  // Coletar múltiplas leituras para o filtro de mediana
  int temp_readings[MEDIAN_WINDOW];
  int hum_readings[MEDIAN_WINDOW];

  for (int i = 0; i < MEDIAN_WINDOW; i++) {
    float temp = dht.readTemperature();
    float humidity = dht.readHumidity();

    // Verificar se a leitura é válida
    if (isnan(temp) || isnan(humidity)) {
      Serial.println("[ERRO] Falha ao ler sensor DHT11");
      return;
    }

    // Converter para inteiro e armazenar
    temp_readings[i] = (int)temp;
    hum_readings[i] = (int)humidity;

    delay(50);  // Pequeno delay entre leituras
  }

  // Calcular medianas
  int temp_median = calculateMedian(temp_readings, MEDIAN_WINDOW);
  int hum_median = calculateMedian(hum_readings, MEDIAN_WINDOW);

  // Armazenar nos buffers principais (histórico)
  addReadingAndGetMedian(temp_buffer, temp_median);
  addReadingAndGetMedian(hum_buffer, hum_median);

  // Formatar resposta no formato especificado
  Serial.print("TEMP&unit:celsius&value:");
  Serial.print(temp_median);
  Serial.print("#HUM&unit:percentage&value:");
  Serial.println(hum_median);
}

// Processa comandos via Serial
void processCommand(String cmd) {
  cmd.trim();
  cmd.toUpperCase();

  if (cmd == "GTH") {
    sendSensorData();
  } else if (cmd.startsWith("RE,")) {
    int track = cmd.substring(3).toInt();
    setTrackLight(track, 'R');
  } else if (cmd.startsWith("YE,")) {
    int track = cmd.substring(3).toInt();
    setTrackLight(track, 'Y');
  } else if (cmd.startsWith("GE,")) {
    int track = cmd.substring(3).toInt();
    setTrackLight(track, 'G');
  } else if (cmd.startsWith("RB,")) {
    int track = cmd.substring(3).toInt();
    setTrackLight(cmd.substring(3).toInt(), 'B');
  } else {
    Serial.print("[AVISO] Comando desconhecido: ");
    Serial.println(cmd);
  }
}

// Controla LEDs de uma track específica
void setTrackLight(int track, char color) {
  int pin_v, pin_a, pin_r;

  // Ao mudar de cor, desligamos sempre o modo blinking daquela track
  if (track == 1) track1_blinking = false;
  if (track == 2) track2_blinking = false;

  if (track == 1) {
    pin_v = T1_VERDE; pin_a = T1_AMARELO; pin_r = T1_VERMELHO;
  } else if (track == 2) {
    pin_v = T2_VERDE; pin_a = T2_AMARELO; pin_r = T2_VERMELHO;
  } else return;

  // Desliga tudo antes de aplicar a nova cor
  digitalWrite(pin_v, LOW);
  digitalWrite(pin_a, LOW);
  digitalWrite(pin_r, LOW);

  switch (color) {
    case 'G': digitalWrite(pin_v, HIGH); break;
    case 'Y': digitalWrite(pin_a, HIGH); break;
    case 'R': digitalWrite(pin_r, HIGH); break;
    case 'B':
      if (track == 1) track1_blinking = true;
      if (track == 2) track2_blinking = true;
      Serial.print("[LED] Track "); Serial.print(track); Serial.println(" - RB Ativado");
      break;
  }
}

// Apagar todos os LEDs
void tudoDesligado() {
  digitalWrite(T1_VERDE, LOW);
  digitalWrite(T1_AMARELO, LOW);
  digitalWrite(T1_VERMELHO, LOW);
  digitalWrite(T2_VERDE, LOW);
  digitalWrite(T2_AMARELO, LOW);
  digitalWrite(T2_VERMELHO, LOW);
}

// --- 5. SETUP (Configuração Inicial) ---
void setup() {
  // Inicializar Serial com baud rate de 115200
  Serial.begin(115200);

  // Aguardar conexão serial
  while (!Serial) {
    delay(10);
  }

  // Inicializar sensor DHT
  dht.begin();

  // Configurar pinos como SAÍDA
  pinMode(T1_VERDE, OUTPUT);
  pinMode(T1_AMARELO, OUTPUT);
  pinMode(T1_VERMELHO, OUTPUT);

  pinMode(T2_VERDE, OUTPUT);
  pinMode(T2_AMARELO, OUTPUT);
  pinMode(T2_VERMELHO, OUTPUT);

  // Desligar todos os LEDs inicialmente
  tudoDesligado();

  // Inicializar buffers
  for (int i = 0; i < MEDIAN_WINDOW; i++) {
    temp_buffer[i] = 0;
    hum_buffer[i] = 0;
  }

  Serial.println("[SYSTEM] Raspberry Pi Pico - Station Management System");
  Serial.println("[SYSTEM] Aguardando comandos...");
  Serial.println("[SYSTEM] Comandos disponíveis:");
  Serial.println("  GTH        - Obter temperatura e humidade");
  Serial.println("  RE,XX      - LED Vermelho (track XX)");
  Serial.println("  YE,XX      - LED Amarelo (track XX)");
  Serial.println("  GE,XX      - LED Verde (track XX)");
  Serial.println("  RB,XX      - LED Vermelho Piscante (track XX)");
}

// --- 6. LOOP PRINCIPAL ---
void loop() {
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    processCommand(cmd);
  }

  // LÓGICA DE PISCAR
  if (millis() - last_blink_time >= 500) {
    last_blink_time = millis();
    blink_state = !blink_state;

    if (track1_blinking) digitalWrite(T1_VERMELHO, blink_state);
    if (track2_blinking) digitalWrite(T2_VERMELHO, blink_state);
  }
}