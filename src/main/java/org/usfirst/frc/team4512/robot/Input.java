package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
public class Input{
    /* Sensors */
    public static BuiltInAccelerometer gyro;
	public static DigitalInput sUp;
    public static DigitalInput sDown;
    public static Encoder dEncoderL;
	public static Encoder dEncoderR;
    public static Encoder liftEncoder;
    
    /* Controls */
	public static XboxController xbox; //object for controller --more buttons :)
	public static Debouncer uDebouncer; //d-pad doesn't return values lightning fast
	public static Debouncer dDebouncer; //define buttons to only return every period
    private static Hand KLEFT = GenericHID.Hand.kLeft; //constant referring to
    private static Hand KRIGHT = GenericHID.Hand.kRight;//the side of controller

    public Input(){
        /* Controls' assignment*/
		Input.xbox = new XboxController(0);
		Input.uDebouncer = new Debouncer(xbox, 0f, 0.25);
        Input.dDebouncer = new Debouncer(xbox, 180f, 0.25);
        
        /* Sensor assignment *///code matches electrical
		Input.dEncoderL = new Encoder(4, 5);
		Input.dEncoderR = new Encoder(2, 3);
		Input.liftEncoder = new Encoder(6, 7);
		Input.gyro = new BuiltInAccelerometer();
		Input.sUp = new DigitalInput(1);
        Input.sDown = new DigitalInput(8);        
    }

    public static void init(){
        liftEncoder.reset();
		dEncoderL.reset();
		dEncoderR.reset();
    }

    /** deadband ? percent, used on the gamepad */
	private static double deadband(double value) {
		double deadzone = 0.15;//smallest amount you can recognize from the controller
		
		/* Inside deadband */
		if ((value >= +deadzone)||(value <= -deadzone)) {
			return value;
		}else{/* Outside deadband */
			return 0;
		}
    }
    
    public static double getLeftY(){
        return deadband(xbox.getY(KLEFT));
    }
    public static double getLeftX(){
        return deadband(xbox.getX(KLEFT));
    }
    public static double getRightY(){
        return deadband(xbox.getY(KRIGHT));
    }
    public static double getRightX(){
        return deadband(xbox.getX(KRIGHT));
    }
    public static boolean getRightBumper(){
        return xbox.getBumper(KRIGHT);
    }
    public static boolean getLeftBumper(){
        return xbox.getBumper(KLEFT);
    }
    public static boolean getAButton(){
        return xbox.getAButton();
    }
    public static boolean getXButton(){
        return xbox.getXButton();
    }
    public static boolean getYButton(){
        return xbox.getYButton();
    }
    public static boolean getBButton(){
        return xbox.getBButton();
    }
    public static double getRightTrigger(){
        return xbox.getTriggerAxis(KRIGHT);
    }
    public static double getLeftTrigger(){
        return xbox.getTriggerAxis(KLEFT);
    }
    public static int getLift(){
        return liftEncoder.get();
    }
    public static int getLeftDrive(){
        return dEncoderL.get();
    }
    public static int getRightDrive(){
        return dEncoderR.get();
    }
}