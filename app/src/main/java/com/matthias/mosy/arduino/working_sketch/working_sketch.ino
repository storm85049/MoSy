/*
    war nur als testSketch gedacht, nachdem das BLuetoothModul das erste mal funktioniert hat.
*/

#include <SoftwareSerial.h>
#define BTSerial Serial1
#define BAUD 115200
String c;
String ON;
String OFF;

void setup() {
  Serial.begin(BAUD);
  BTSerial.begin(BAUD);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  ON = "49"; // ByteCode, der vom Android Handy vom ursprünglichen Input Wert 1 zum UTF_8 Wert 49 umgewandelt wurde.
  OFF = "48"; // ByteCode, der vom Android Handy vom ursprünglichen Input Wert 0 zum UTF_8 Wert 48 umgewandelt wurde.
}

void loop() {
  // Wenn eine Nachricht vom HM-10 Modul kommt, printe es im seriellen Monitor
  if (BTSerial.available()) {
    c = BTSerial.read();
    Serial.print(c);
    
    if (c.equals(ON)){
      digitalWrite(LED_BUILTIN, HIGH);
    }
    
    else if (c.equals(OFF)){
      digitalWrite(LED_BUILTIN, LOW);
    }
  }
}
