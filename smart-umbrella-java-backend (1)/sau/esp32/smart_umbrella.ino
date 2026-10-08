/*
 * Smart Assistant Umbrella - ESP32 + SIM800L (GSM) + NEO-6M (GPS) sketch (starter).
 * Sends GPS every 60 s and an SOS when the handle button is pressed.
 * Libraries: TinyGSM, ArduinoHttpClient, TinyGPSPlus.
 */
#define TINY_GSM_MODEM_SIM800
#include <TinyGsmClient.h>
#include <ArduinoHttpClient.h>
#include <TinyGPSPlus.h>

const char APN[]        = "internet";             // your SIM operator's APN
const char SERVER[]     = "your-backend-host.com"; // Java backend or the Lovable app domain
const int  PORT         = 80;
const char DEVICE_ID[]  = "SAU-1001";
const char DEVICE_KEY[] = "sau_paste_key_from_my_umbrella_page";
// Java backend paths: /api/device/...   Lovable app paths: /api/public/device/...
const char BASE[]       = "/api/device";

#define SOS_PIN 4
#define MOTOR_PIN 5

HardwareSerial gsmSerial(1), gpsSerial(2);
TinyGsm modem(gsmSerial);
TinyGsmClient client(modem);
HttpClient http(client, SERVER, PORT);
TinyGPSPlus gps;
unsigned long lastSend = 0;

void post(const String& path, const String& body) {
  http.beginRequest();
  http.post(String(BASE) + path);
  http.sendHeader("Content-Type", "application/json");
  http.sendHeader("X-Device-Id", DEVICE_ID);
  http.sendHeader("X-Device-Key", DEVICE_KEY);
  http.sendHeader("Content-Length", body.length());
  http.beginBody(); http.print(body); http.endRequest();
  Serial.printf("%s -> %d\n", path.c_str(), http.responseStatusCode());
  http.stop();
}

String coords() {
  if (!gps.location.isValid()) return "";
  return "\"latitude\":" + String(gps.location.lat(), 6) + ",\"longitude\":" + String(gps.location.lng(), 6);
}

void vibrate(int pulses, int ms) {
  for (int i = 0; i < pulses; i++) { digitalWrite(MOTOR_PIN, HIGH); delay(ms); digitalWrite(MOTOR_PIN, LOW); delay(150); }
}

void setup() {
  Serial.begin(115200);
  pinMode(SOS_PIN, INPUT_PULLUP); pinMode(MOTOR_PIN, OUTPUT);
  gsmSerial.begin(9600, SERIAL_8N1, 26, 27);
  gpsSerial.begin(9600, SERIAL_8N1, 16, 17);
  modem.restart();
  modem.waitForNetwork();
  modem.gprsConnect(APN);
}

void loop() {
  while (gpsSerial.available()) gps.encode(gpsSerial.read());

  if (digitalRead(SOS_PIN) == LOW) {             // SOS button pressed
    vibrate(3, 400);
    post("/sos", "{" + coords() + "}");
    delay(3000);
  }
  if (millis() - lastSend > 60000 && gps.location.isValid()) {
    post("/location", "{" + coords() + ",\"accuracy\":" + String(gps.hdop.hdop() * 5, 1) + "}");
    lastSend = millis();
  }
  // Example: obstacle sensor -> vibrate and report
  // if (distanceCm() < 80) { vibrate(4, 120); post("/haptic", "{\"type\":\"obstacle\"}"); }
}
