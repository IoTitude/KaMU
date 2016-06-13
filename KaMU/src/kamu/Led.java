package kamu;

 

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Led {
    static final GpioController gpio = GpioFactory.getInstance();
    static final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);
    
        public static void ledoff() {
            gpio.shutdown();
        }
        public static void ledon(){           
            pin.high();
        }
        

        
        public static void ledtoggle(long interval){
            try {
                pin.blink(interval);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }     
        }
  
}
  