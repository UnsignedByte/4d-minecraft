//#include <Servo.h>
//
//Servo hot_servo;

void setup() {
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Native USB only
  }
//  hot_servo.attach(10);
  pinMode(10, OUTPUT); 
  pinMode(2, OUTPUT);
  
//  hot_servo.write(90);
}

const int FALLTICKS = 1000; // ticks to display fall alarm (@ 40 tps)
int GLOBALTIMER = 0;
const int OSCINTERVAL = 400;
boolean hot = false;

const int SAUCE_DOWN = 1500;
const int SAUCE_MID = 1100;
const int SAUCE_UP = 1100;
int current_position = SAUCE_UP;


void loop() {
  GLOBALTIMER++;
  GLOBALTIMER %= OSCINTERVAL;

  if (hot) {
    if (GLOBALTIMER % OSCINTERVAL < OSCINTERVAL*0.6f) {
      current_position = SAUCE_MID;
    } else {
      current_position = SAUCE_DOWN;
    }
  } else {
    current_position = SAUCE_UP;
  }

  digitalWrite(10, HIGH);
  delayMicroseconds(current_position);
  digitalWrite(10, LOW);

  if (Serial.available()) {
    switch (Serial.read()) {
      case 0: //water on
        digitalWrite(2, HIGH);
        break;
      case 1: //water off
        digitalWrite(2, LOW);
        break;
      case 2: //hot on
        hot = true;
//        analogWrite(180);
        break;
      case 3: //hot off
        hot = false;
//        analogWrite(0);
        break;
      case 7: //ready
        Serial.write(7);
      default: //default !!!!!
        break;
    }
  }
  delayMicroseconds(20000-current_position);
//  }
//  Serial.flush();
}
