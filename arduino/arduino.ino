void setup() {
  Serial.begin(9600);
}

bool connected = false;
const int FALLTICKS = 200; // ticks to display fall alarm (@ 40 tps)
int timer = 0;

void loop() {
  timer--;
  if (timer == 0) {
    digitalWrite(9, LOW); // turn off fall alarm
  }
  
  if (Serial.available()) {
    byte b = Serial.read();
    if (!connected) {
      if (b == 7) Serial.write("ready");
    } else {
      switch (b) {
        case 0:
          digitalWrite(5, HIGH);
          break;
        case 1:
          digitalWrite(5, LOW);
          break;
        case 2:
          digitalWrite(6, HIGH);
          break;
        case 3:
          digitalWrite(6, LOW);
          break;
        case 4:
          break;
        case 5:
          digitalWrite(9, HIGH);
          timer = FALLTICKS;
          break;
        case 7:
        default:
          break;
      }
    }
  }
  delay(25); // 40 tps, twice as fast as mc
}
