package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp
public class FieldCentricTeleOpButOnlyA extends LinearOpMode {

    // encoder calculations
    public static final double ticksPerMotorRev = 1120;
    public static final double gearReduction = 2;
    public static final double ratio = ticksPerMotorRev / gearReduction;
    public static final double ratio_to_top = ratio / 2;

    // drivetrain motors
    private DcMotor motorFrontLeft, motorBackLeft, motorFrontRight, motorBackRight;

    // grabber
    private CRServo grabber;

    // lift
    private DcMotor lift1, lift2;
    boolean keepLiftStopped = false;

    // runtime
    ElapsedTime runtime = new ElapsedTime();


    @Override
    public void runOpMode() throws InterruptedException {
        // Declare our motors
        // Make sure your ID's match your configuration
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

        // Set zero power behavior
        motorFrontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFrontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reverse one side of lift
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
            double y = -gamepad1.left_stick_y / 2; // Remember, this is reversed!
            double x = gamepad1.left_stick_x * 1.1 / 2; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x / 2;

            double botHeading = -imu.getAngularOrientation().firstAngle;

            // Rotate the movement direction counter to the bot's rotation
            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio, but only when
            // at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1);
            double frontLeftPower = (rotY + rotX + rx) / denominator;
            double backLeftPower = (rotY - rotX + rx) / denominator;
            double frontRightPower = (rotY - rotX - rx) / denominator;
            double backRightPower = (rotY + rotX - rx) / denominator;

            motorFrontLeft.setPower(frontLeftPower);
            motorBackLeft.setPower(backLeftPower);
            motorFrontRight.setPower(frontRightPower);
            motorBackRight.setPower(backRightPower);

            // grabber controls
            if (gamepad2.right_trigger > 0) {
                telemetry.addLine("Right Trigger");
                telemetry.update();
                grabber.setPower(1);

            } else if (gamepad2.left_trigger > 0) {
                telemetry.addLine("Left Trigger");
                telemetry.update();
                grabber.setPower(-1);

            } else {
                grabber.setPower(0);
            }

            // lift manual controls
            if (gamepad2.left_bumper) {
                keepLiftStopped = false;
                lift1.setPower(0.8);
                lift2.setPower(0.8);
            } else if (gamepad2.right_bumper) {
                keepLiftStopped = false;
                runtime.reset();
                while (runtime.seconds() <= 1) {
                    lift1.setPower(-0.1);
                    lift2.setPower(-0.1);
                }
                lift1.setPower(0);
                lift2.setPower(0);

            } else if (!keepLiftStopped) {
                lift1.setPower(0);
                lift2.setPower(0);
            }

            // auto lift raise
            if (gamepad2.a) {
                autoLift();
            }

            if (keepLiftStopped) {
                lift1.setPower(0.1);
                lift2.setPower(0.1);
            }
        }
    }

    public void autoLift() {
        final int targetHeight = (int) Math.round(ratio_to_top * (5.0 / 26))-3;

        keepLiftStopped = true;

        lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        lift1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lift2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        lift1.setTargetPosition(targetHeight);
        lift2.setTargetPosition(targetHeight);

        lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        runtime.reset();
        while (runtime.seconds() < 3 && (lift1.isBusy() && lift2.isBusy())) {
            lift1.setPower(0.8);
            lift2.setPower(0.8);

            // useful information
            telemetry.addData("Lift 1 Target:", targetHeight);
            telemetry.addData("Lift 2 Target:", targetHeight);
            telemetry.addData("Lift 1 Current Pos:", lift1.getCurrentPosition());
            telemetry.addData("Lift 2 Current Pos:", lift2.getCurrentPosition());
            telemetry.addData("Lift 1 Power:", lift1.getPower());
            telemetry.addData("Lift 2 Power:", lift2.getPower());
            telemetry.update();
        }

        lift1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        lift2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }
}