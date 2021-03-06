package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
public class MotorBase{
    /** Hardware */
	/* Right Talons */
	private static TalonSRX dRightF = new TalonSRX(1);//gearbox - 8.46:1
	private static TalonSRX dRightB = new TalonSRX(2);

	/* Left Talons */
	private static TalonSRX dLeftF = new TalonSRX(3);
	private static TalonSRX dLeftB = new TalonSRX(4);

	/* Lift Victors */
	private static VictorSP liftF = new VictorSP(0);
	private static VictorSP liftB = new VictorSP(1);

	/* Intake Victors */
	private static Spark armR = new Spark(4);
	private static Spark armL = new Spark(5);

    /* Constants */
	private static double dSpeed;//overall speed affecting robots actions
	private static double dSpeedL;
	private static double lHigh;//last non-zero lift power
	private static double dForwardH;//last non-zero FORWARD value
	private static double dTurnH;//last non-zero TURN value
	private static double driveK;//value affecting the slew of acceleration
	private static double liftK;//for lift
	private static double kTurnP = 0.02;//see autonomous
	private static double kTurnF = 0.35;
	private static int lState;//determine state for executing lift commands
	private static final int MAXLIFT = 4300;//top of the lift in counts(actual ~4400)
	    
    public static void driveInit(){
		/* Disable motor controllers */
		setDrive(0,0);
		setLift(0);
		
		/* Set Neutral mode *///motor behavior
		setNeutral(NeutralMode.Brake);

		/* Configure output direction */
		dRightF.setInverted(true);
		dRightB.setInverted(true);
		dLeftF.setInverted(false);
		dLeftB.setInverted(false);
		armR.setInverted(true);
		armL.setInverted(false);
		
		/* Constant assignment */
		dSpeed = dSpeedL = 0.3;
		driveK = 0.2;
		liftK=0.075;
		dForwardH=lState = 0;
		Input.reset();
		System.out.println("--Feed Forward Teleop--");
	}
	
	public static void driveDisable(){
		setDrive(0, 0);
		setLift(0);
		setNeutral(NeutralMode.Coast);
	}

    public static void drivePeriodic(){
		/* Drive Base */
		setDrive(Input.getLeftY(),Input.getRightX());

		/* Intake */
		double rTrigg = Input.getRightTrigger();
		double lTrigg = Input.getLeftTrigger();
		if(rTrigg > 0) setArms(rTrigg);
		else if(lTrigg > 0) setArms(-lTrigg);
		else setArms(0.25);
		
		/* Lift */ 
		//stop the lift if bumpers are not pressed
		if(lState!=1 && lState!=2 && lState != 5) lState=0;

		boolean up = Input.getUp();
		boolean down = Input.getDown();
		boolean rightB = Input.getRightBumper();
		boolean leftB = Input.getLeftBumper();

		//check input
		lState=(rightB)? 3:lState;
		lState=(leftB)? 4:lState;
		if(rightB && leftB) lState = 0;
		else if(rightB) lState = 3;
		else if(leftB) lState = 4;
		if(Input.getBackButton()) Input.toggleLight();
		if(Input.getStartButton()) Input.shiftPipe();
		if(Input.getRightStick()) {
			trackVision();
		} else if(Input.getRightStickReleased()){
			shift(dSpeedL);
			dSpeed=dSpeedL;
		}
		//reset if pressing switches
		if(down) onDown();//call methods when switches are pressed
		if(up) onUp();

		if(Input.uDebouncer.get()) lState = 1;//pressing d-pad will automatically
		if(Input.dDebouncer.get()) lState = 2;//move the lift to top or bottom
		if(Input.lDebouncer.get()||Input.rDebouncer.get()) lState = 5;//move to middle

		lState=(up&&(lState==1||lState==3||lState==5))? 0:lState;
		lState=(down&&(lState==2||lState==4))? 0:lState;
        
		double liftPercent = (Input.getLift()/(double)MAXLIFT)-0.025;//percentage of lift height
		double mathUp = encoderMath(liftPercent, 1);//speed based on height going up
		liftK=0.1+((1-mathUp)/3);
		double mathDown = -encoderMath(liftPercent,0.8)*interpolate(0.3,5,liftPercent);//down
		SmartDashboard.putNumber("LiftPercent", liftPercent);//let me see the math, borther
		switch(lState) {//different states dictate lift speed
		case 1://automatic up (d-pad)
			setLift(mathUp);
			break;
		case 2://automatic down (d-pad)
			setLift(mathDown);
			break;
		case 3://manual up (bumper)
			setLift(mathUp);
			break;
		case 4://manual down (bumper)
			setLift(mathDown);
			break;
		case 5://automatic middle (for switch auto)
			if(liftPercent < 0.15) setLift(mathUp*0.7);
			else if(liftPercent > 0.25) setLift(mathDown*0.6);
			else lState = 0;
			break;
		default://keep lift still
			if(!down) {
				setLift(0.11);//backpressure
				if(liftPercent < 0.15) lState = 2;//if the lift is low, auto push down
			} else setLift(0);//dont break things if not suspended
			break;
		}
		
		/* Drive <-> Lift */
		//change speed if buttons are pressed
		//dSpeed=(Input.getAButton())?0.2:dSpeed;
		dSpeed = (Input.getXButton()||Input.getAButton())? 0.3:dSpeed;
		dSpeed = (Input.getYButton())? 0.5:dSpeed;
		dSpeed = (Input.getBButton())? 1.0:dSpeed;
		if(liftPercent>0.4) {//slow speed when lift high
			dSpeed=interpolate(0.2,0.365,1-liftPercent);
		} else if(liftPercent<0.4) {
			dSpeed=(dSpeed<0.3)? 0.3:dSpeed;
		}
		dSpeed = Math.round(dSpeed*100)/100.0;
    }

	
	/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		//given a forward value and a turn value, will automatically do all the math and appropriately send signals
	public static void setDrive(double forward, double turn){
		forward = driveK*forward + (1-driveK)*dForwardH;
		dForwardH = forward;
		forward *= dSpeed;
		turn = driveK*turn + (1-driveK)*dTurnH;
		dTurnH = turn;
		turn *= dSpeed;
		SmartDashboard.putNumber("Forward", forward);
		SmartDashboard.putNumber("Turn", turn);
		dRightF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		dRightB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		dLeftF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
		dLeftB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
	}

	public static void setLift(double power){
		//motor_value = constant*joystick_reading + (1-constant)*motor_value --constrain the acceleration (constant = slew)
		power = liftK*power + (1-liftK)*lHigh;
		lHigh = power;
		SmartDashboard.putNumber("LiftSpeed", power);
		liftF.set(power);
		liftB.set(power);
    }
	
	public static void trackVision(){
		double tx = Input.getTx();
		double angleError = 0;
		if(tx>0.3){
			angleError = kTurnP*(tx) + kTurnF;
		} else if(tx<-0.3){
			angleError = kTurnP*(tx) - kTurnF;
		}
		shift(0.5);
		setDrive(Input.getLeftY(), angleError);
	}

    public static void setArms(double power){
        armR.set(power);
        armL.set(power);
	}
	
	private static double encoderMath(double x, double n) {//give a lift speed based on how high the lift is
		double k = 0.25;//minimum speed when lift at bottom/top
		double y = -(1000*(1-k))*0.25*n*Math.pow((x-0.5), 8)+((1-k)*n)+k;//big equation(slow down on bottom/top of lift) https://www.desmos.com/calculator/mqlbagskqz
		y = Math.max(y, k); //negatives are bad kids
		return y;
	}

	private static double interpolate(double a, double b, double x){//given x as a fraction between a and b
		double math = a+(x*(b-a));
		if(a>b) {
			double hold = a;
			a = b;
			b = hold;
		}
		math = limit(limit(-1,1,a),limit(-1,1,b),math);
		return math;
	}

	private static double limit(double min, double max, double x){//limit
		return (x>max)? max:Math.max(x,min);
	}

	//whenever lift switches are pressed, run these methods
	public static void onDown() {
		Input.resetLift();//make sure bottoming out sets the encoder to 0
	}
	public static void onUp() {
		//MAXLIFT = Input.getLift()-150;
		//System.out.println("Top triggered on: "+Input.getLift());
	}

	public static void setNeutral(NeutralMode n){
		dRightF.setNeutralMode(n);
		dRightB.setNeutralMode(n);
		dLeftF.setNeutralMode(n);
		dLeftB.setNeutralMode(n);
	}

	public static void shift(double speed){
		dSpeed = speed;
	}

	public static void displayStats(){
		SmartDashboard.putNumber("DriveSpeed", MotorBase.dSpeed);
		SmartDashboard.putNumber("LeftSpeed", MotorBase.dLeftF.getMotorOutputPercent());
		SmartDashboard.putNumber("RightSpeed", MotorBase.dRightF.getMotorOutputPercent());
		SmartDashboard.putNumber("ArmRSpeed", MotorBase.armR.get());
		SmartDashboard.putNumber("ArmLSpeed", MotorBase.armL.get());
		SmartDashboard.putNumber("MaxLift", MotorBase.MAXLIFT);
	}
}