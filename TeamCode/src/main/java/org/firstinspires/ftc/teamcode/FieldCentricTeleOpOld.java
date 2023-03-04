package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class FieldCentricTeleOpOld extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        // Declare our motors
        // Make sure your ID's match your configuration
        DcMotor motorFrontLeft = hardwareMap.dcMotor.get("LF");
        DcMotor motorBackLeft = hardwareMap.dcMotor.get("LB");
        DcMotor motorFrontRight = hardwareMap.dcMotor.get("RF");
        DcMotor motorBackRight = hardwareMap.dcMotor.get("RB");

        CRServo grabber = hardwareMap.crservo.get("grabber");
        DcMotor lift1 = hardwareMap.dcMotor.get("lift1");
        DcMotor lift2 = hardwareMap.dcMotor.get("lift2");

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

        // Retrieve the IMU from the hardware map
        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        // Technically this is the default, however specifying it is clearer
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        // Without this, data retrieving from the IMU throws an exception
        imu.initialize(parameters);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y; // Remember, this is reversed!
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Dead/safe zone so robot won't move when stick is only very slightly pressed
            // Only move at half power when stick is only pressed partway to allow for more precise movements
            // To be tested: different values for half power threshold
            if (Math.abs(y) < 0.05) y = 0;
            else if (Math.abs(y) < 0.25) y /= 2;

            if (Math.abs(x) < 0.05) x = 0;
            else if (Math.abs(x) < 0.25) x /= 2;

            if (Math.abs(rx) < 0.05) rx = 0;
            else if(Math.abs(rx) < 0.25) rx /= 2;

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
            double liftDown = 1;

            if (gamepad2.right_bumper) {
                lift1.setPower(liftDown);
                lift2.setPower(-liftDown);

            } else if (gamepad2.left_bumper) {
                lift1.setPower(-liftUp);
                lift2.setPower(liftUp);

            } else {
                lift1.setPower(0);
                lift2.setPower(0);
            }

            // grabber controls
            if (gamepad2.right_trigger > 0) {
                grabber.setPower(0.5);

            } else if (gamepad2.left_trigger > 0) {
                grabber.setPower(-0.5);

            } else {
                grabber.setPower(0);
            }


        }
    }
}
