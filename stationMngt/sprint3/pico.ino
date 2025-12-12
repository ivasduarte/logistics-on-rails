#include "DHT.h"
#include <arduino-timer.h>

// --- 1. CONFIGURAÇÃO DOS PINOS ---

// Sensor
#define DHTPIN 28
#define DHTTYPE DHT11

// Track 1 (Sua nova configuração)
#define T1_VERDE    15
#define T1_AMARELO  13  // Atenção: Pulou o GP14
#define T1_VERMELHO 12

// Track 2 (Configuração anterior)
#define T2_VERDE    16
#define T2_AMARELO  18
#define T2_VERMELHO 19

// --- 2. INICIALIZAÇÃO DE OBJETOS ---
DHT dht(DHTPIN, DHTTYPE);
auto timer = timer_create_default();

// --- 3. FUNÇÃO DO SENSOR (Executa a cada 3 segundos) ---
bool lerSensor(void *) {
  float temp = dht.readTemperature();
  float humidity = dht.readHumidity();

  if (isnan(temp) || isnan(humidity)) {
    Serial.println("Erro ao ler sensor DHT!");
    return true; 
  }

  Serial.print("[SENSOR] Temp: ");
  Serial.print(temp);
  Serial.print(" C | Umidade: ");
  Serial.print(humidity);
  Serial.println(" %");

  return true; // Mantém o timer ativo
}

// --- 4. FUNÇÃO AUXILIAR DE TEMPO ---
// Substitui o delay() para não travar o sensor
void esperar_com_sensor(unsigned long ms) {
  unsigned long inicio = millis();
  while (millis() - inicio < ms) {
    timer.tick(); // Verifica o sensor enquanto o tempo passa
  }
}

// Função para apagar tudo (segurança na troca de sinal)
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
  Serial.begin(115200);
  dht.begin();

  // Configura os pinos como SAÍDA
  pinMode(T1_VERDE, OUTPUT);
  pinMode(T1_AMARELO, OUTPUT);
  pinMode(T1_VERMELHO, OUTPUT);
  
  pinMode(T2_VERDE, OUTPUT);
  pinMode(T2_AMARELO, OUTPUT);
  pinMode(T2_VERMELHO, OUTPUT);

  // Define que o sensor será lido a cada 3000ms (3 segundos)
  timer.every(3000, lerSensor);
  
  Serial.println("Sistema de Semáforos Duplo Iniciado!");
}

// --- 6. LOOP PRINCIPAL ---
void loop() {
  
  // FASE 1: Track 1 ABERTO (Verde) | Track 2 FECHADO (Vermelho)
  tudoDesligado();
  digitalWrite(T1_VERDE, HIGH);
  digitalWrite(T2_VERMELHO, HIGH);
  esperar_com_sensor(5000); // Espera 5 segundos lendo o sensor

  // FASE 2: Track 1 ATENÇÃO (Amarelo) | Track 2 FECHADO (Vermelho)
  digitalWrite(T1_VERDE, LOW);
  digitalWrite(T1_AMARELO, HIGH);
  // Track 2 continua vermelho
  esperar_com_sensor(2000); // Espera 2 segundos lendo o sensor

  // FASE 3: Track 1 FECHADO (Vermelho) | Track 2 ABERTO (Verde)
  tudoDesligado();
  digitalWrite(T1_VERMELHO, HIGH);
  digitalWrite(T2_VERDE, HIGH);
  esperar_com_sensor(5000);

  // FASE 4: Track 1 FECHADO (Vermelho) | Track 2 ATENÇÃO (Amarelo)
  digitalWrite(T2_VERDE, LOW);
  digitalWrite(T2_AMARELO, HIGH);
  // Track 1 continua vermelho
  esperar_com_sensor(2000);
}