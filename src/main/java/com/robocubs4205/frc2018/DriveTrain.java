package com.robocubs4205.frc2018;

import com.ctre.phoenix.drive.DriveMode;
import com.ctre.phoenix.drive.MecanumDrive;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Subsystem;

@SuppressWarnings("FieldCanBeLocal")
class DriveTrain extends Subsystem {
    private TalonSRX frontLeft = new TalonSRX(13);
    private TalonSRX frontRight = new TalonSRX(11);
    private TalonSRX rearLeft = new TalonSRX(12);
    private TalonSRX rearRight = new TalonSRX(10);

    private final int CPR = 4096;

    private final double wheelDiameter = 0.5;
    private final double wheelCircumference = wheelDiameter*Math.PI;
    private final int CPF = (int) (CPR/wheelCircumference);

    private final MecanumDrive drive = new MecanumDrive(frontLeft, rearLeft, frontRight, rearRight);

    private final double proportionalLateralSpeed = 1;
    private final double proportionalTurnSpeed = 1;

    DriveTrain() {
        rearLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
        rearRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
    }

    @Override
    protected void initDefaultCommand() {
        setDefaultCommand(new Stop());
    }

    class Stop extends InstantCommand {

        Stop() {
            requires(DriveTrain.this);
        }

        @Override
        protected void initialize() {
            drive.set(DriveMode.PercentOutput, 0, 0);
        }
    }

    class Mecanum extends Command {
        private final double forward;
        private final double turn;
        private final double strafe;

        Mecanum(double forward, double turn, double strafe) {
            this.forward = forward;
            this.turn = turn;
            this.strafe = strafe;
            requires(DriveTrain.this);
        }

        public Mecanum(double forward, double turn) {
            this(forward, turn, 0);
        }

        @Override
        protected void execute() {
            drive.set(DriveMode.PercentOutput,
                    forward * proportionalLateralSpeed,
                    turn * proportionalTurnSpeed,
                    strafe * proportionalLateralSpeed);
        }

        @Override
        protected boolean isFinished() {
            return false;
        }
    }

    class DriveEncoder extends PerpetualCommand {
        private final double distance;
        private final double speed;

        /**
         * Drive forward a specific distance
         * @param distance the distance in feet
         */
        DriveEncoder(double distance){
            this(distance,0.5);
        }

        /**
         * Drive forward a specific distance
         * @param distance the distance in feet
         * @param speed the speed on the range (0,1]
         */
        DriveEncoder(double distance, double speed){
            this.distance = distance;
            this.speed = speed;
        }

        @Override
        protected void initialize(){
            rearLeft.setSelectedSensorPosition(0,0,10);
            rearRight.setSelectedSensorPosition(0,0,10);
            rearLeft.config_kP(0,0.125,10);
            rearRight.config_kP(0,0.125,10);
            rearLeft.configPeakOutputForward(speed,10);
            rearRight.configPeakOutputForward(speed,10);
            rearLeft.configPeakOutputReverse(-speed,10);
            rearRight.configPeakOutputReverse(-speed,10);

            frontLeft.follow(rearLeft);
            frontRight.follow(rearRight);
        }

        @Override
        protected void execute(){
            rearLeft.set(ControlMode.Position,CPF*distance);
            rearRight.set(ControlMode.Position,CPF*distance);
        }
    }
}
