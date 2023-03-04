package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp
public class FieldCentricTeleOp extends LinearOpMode {

    public static final double ticksPerMotorRev = 1120;
    public static final double gearReduction = 2;
    public static final double ratio = ticksPerMotorRev / gearReduction;
    public static final double ratio_to_top = ratio / 2;
    // public static final double ticksPerDriveInch = (ticksPerMotorRev * driveGearReduction) / (wheelDiameterInches * 3.14159265359);

    ElapsedTime runtime = new ElapsedTime();
    public boolean isAutoLiftActive = false;

    private DcMotor motorFrontLeft, motorBackLeft, motorFrontRight, motorBackRight;

    private CRServo grabber;
    private DcMotor lift1, lift2;

    public int currentJunctionHeight = 3;

    @Override
    public void runOpMode() throws InterruptedException {
        motorFrontLeft = hardwareMap.dcMotor.get("LF");
        motorBackLeft = hardwareMap.dcMotor.get("LB");
        motorFrontRight = hardwareMap.dcMotor.get("RF");
        motorBackRight = hardwareMap.dcMotor.get("RB");

        grabber = hardwareMap.crservo.get("grabber");
        lift1 = hardwareMap.dcMotor.get("lift1");
        lift2 = hardwareMap.dcMotor.get("lift2");

        // Reverse the right side motors
        // Reverse left motors if you are using NeveRests
        motorFrontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        motorBackRight.setDirection(DcMotorSimple.Direction.REVERSE);

        motorFrontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFrontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift1.setDirection(DcMotorSimple.Direction.REVERSE);

        // Retrieve the IMU from the hardware map
        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        // Technically this is the default, however specifying it is clearer
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        // Without this, data retrieving from the IMU throws an exception
        imu.initialize(parameters);

        lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            telemetry.addData("Lift 1 Pos:", lift1.getCurrentPosition());
            telemetry.addData("Lift 2 Pos:", lift2.getCurrentPosition());
            telemetry.update();
            /*
            telemetry.addData("Pick Up Cone Height", (int) Math.round(ratio_to_top * (5.0 / 26)));
            telemetry.addData("Low Junction Height", (int) Math.round(ratio_to_top * (13.25 / 26)));
            telemetry.update();
             */
            double y = -gamepad1.left_stick_y / 2; // Remember, this is reversed!
            double x = gamepad1.left_stick_x * 1.1 / 2; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x / 2;

            // Dead/safe zone so robot won't move when stick is only very slightly pressed
            // Only move at half power when stick is only pressed partway to allow for more precise movements
            // To be tested: different values for half power threshold
            /*
            if (Math.abs(y) < 0.05) y = 0;
            else if (Math.abs(y) < 0.5) y /= 2;

            if (Math.abs(x) < 0.05) x = 0;
            else if (Math.abs(x) < 0.5) x /= 2;

            if (Math.abs(rx) < 0.05) rx = 0;
            else if(Math.abs(rx) < 0.5) rx /= 2;
             */

            // Read inverse IMU heading, as the IMU heading is CW positive
            double botHeading = -imu.getAngularOrientation().firstAngle;

            double rotX = x * Math.cos(botHeading) - y * Math.sin(botHeading);
            double rotY = x * Math.sin(botHeading) + y * Math.cos(botHeading);

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio, but only when
            // at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (rotY + rotX + rx) / denominator;
            double backLeftPower = (rotY - rotX + rx) / denominator;
            double frontRightPower = (rotY - rotX - rx) / denominator;
            double backRightPower = (rotY + rotX - rx) / denominator;

            motorFrontLeft.setPower(frontLeftPower);
            motorBackLeft.setPower(backLeftPower);
            motorFrontRight.setPower(frontRightPower);
            motorBackRight.setPower(backRightPower);

            // lift controls
            double liftUp = 0.7;
            double liftDown = -0.1;

            if (gamepad2.right_bumper) {
                telemetry.addLine("right bumper pressed");
                isAutoLiftActive = false;
                // currentJunctionHeight = 3;
                runtime.reset();
                while (runtime.seconds() < 1) {
                    lift1.setPower(liftDown);
                    lift2.setPower(liftDown);
                }
                lift1.setPower(0);
                lift2.setPower(0);

            } else if (gamepad2.left_bumper) {
                telemetry.addLine("left bumper pressed");
                isAutoLiftActive = false;
                // currentJunctionHeight = 3;
                lift1.setPower(liftUp);
                lift2.setPower(liftUp);

            }

            if (isAutoLiftActive) {
                switch (currentJunctionHeight) {
                    case 0: lift1.setPower(0.1);
                            lift2.setPower(0.1);
                            telemetry.addData("Stabilizing Power working", lift1.getPower());
                            telemetry.addData("Stabilizing Power working", lift1.getPower());
                            telemetry.update();
                            break;
                    case 1: lift1.setPower(0.3);
                            lift2.setPower(0.3);
                            break;
                    case 2: lift1.setPower(0.4);
                            lift2.setPower(0.4);
                            break;
                    case 3: lift1.setPower(0);
                            lift2.setPower(0);
                            break;
                }
            }

            if (gamepad2.a) {
                autoLift(0);
            } else if (gamepad2.b) {
                autoLift(1);
            } else if (gamepad2.x) {
                autoLift(2);
            } else if (gamepad2.y) {
                autoLift(3);
            }

            // grabber controls
            if (gamepad2.right_trigger > 0) {
                telemetry.addLine("Right trigger");
                telemetry.update();
                grabber.setPower(1);

            } else if (gamepad2.left_trigger > 0) {
                telemetry.addLine("Left trigger");
                telemetry.update();
                grabber.setPower(-1);

            } else {
                grabber.setPower(0);
            }
        }

    }

    public void autoLift(int junctionHeight) {
        final int pickUpConeHeight = (int) Math.round(ratio_to_top * (5.0 / 26))-3;
        // final int groundJunctionHeight = (int) ratio / 7 - 10;
        final int lowJunctionHeight = (int) Math.round(ratio_to_top * (13.25 / 26));
        final int midJunctionHeight = (int) ratio / 3;
        final int highJunctionHeight = (int) ratio_to_top;

        isAutoLiftActive = true;

        int lift1Target = 0;
        int lift2Target = 0;
        int lift1Pos = lift1.getCurrentPosition();
        int lift2Pos = lift2.getCurrentPosition();

        telemetry.addData("Lift 1 Pos:", lift1Pos);
        telemetry.addData("Lift 2 Pos:", lift2Pos);
        telemetry.update();

        lift1.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        lift2.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        if (junctionHeight == 0) {
            lift1Target = pickUpConeHeight;
            lift2Target = pickUpConeHeight;
        } else if (junctionHeight == 1) {
            lift1Target = lowJunctionHeight;
            lift2Target = lowJunctionHeight;
        } else if (junctionHeight == 2) {
            lift1Target = midJunctionHeight;
            lift2Target = midJunctionHeight;
        } else if (junctionHeight == 3) {
            lift1Target = highJunctionHeight;
            lift2Target = highJunctionHeight;
        }

        telemetry.addData("Target Junction Height:", junctionHeight);
        telemetry.addData("Lift 1 Target:", lift1Target);
        telemetry.addData("Lift 2 Target:", lift2Target);
        telemetry.update();

        lift1.setTargetPosition(lift1Target);
        lift2.setTargetPosition(lift2Target);

        lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        runtime.reset();
        while (runtime.seconds() < 3 && (lift1.isBusy() && lift2.isBusy())) {
            if (lift1Pos < lift1Target && lift2Pos < lift2Target) {
                lift1.setPower(0.8);
                lift2.setPower(0.8);
            } else {
                lift1.setPower(0);
                lift2.setPower(0);
            }

            telemetry.addData("Target Junction Height:", junctionHeight);
            telemetry.addData("Lift 1 Target:", lift1Target);
            telemetry.addData("Lift 2 Target:", lift2Target);
            telemetry.addData("Lift 1 Current Pos:", lift1.getCurrentPosition());
            telemetry.addData("Lift 2 Current Pos:", lift2.getCurrentPosition());
            telemetry.addData("Lift 1 Power:", lift1.getPower());
            telemetry.addData("Lift 2 Power:", lift2.getPower());
            telemetry.update();
        }

        telemetry.addData("Lift 1 Power:", lift1.getPower());
        telemetry.addData("Lift 2 Power:", lift2.getPower());
        telemetry.update();

        // lift1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        // lift2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // in the future, maybe replace this with PID?
        switch (junctionHeight) {
            case 0: currentJunctionHeight = 0;
                    break;
            case 1: currentJunctionHeight = 1;
                    break;
            case 2: currentJunctionHeight = 2;
                    break;
            case 3: currentJunctionHeight = 3;
                    break;
        }
        // lift1.setPower(0);
        // lift2.setPower(0);
    }
}
