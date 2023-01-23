
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

@TeleOp
public class TeleOpCurrent extends OpMode {
    private DcMotor lf;
    private DcMotor lb;
    private DcMotor rf;
    private DcMotor rb;

    private DcMotor lift1;
    private CRServo lift2;

    private CRServo grabber;

    private float leftPower, rightPower, xValue, yValue;

    @Override
    public void init() {
        lf = hardwareMap.dcMotor.get("LF");
        lb = hardwareMap.dcMotor.get("LB");
        rf = hardwareMap.dcMotor.get("RF");
        rb = hardwareMap.dcMotor.get("RB");

        lift1 = hardwareMap.dcMotor.get("lift1");
        lift2 = hardwareMap.crservo.get("lift2");
        grabber = hardwareMap.crservo.get("grabber");

        lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // lift2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        lf.setDirection(DcMotorSimple.Direction.FORWARD);
        lb.setDirection(DcMotorSimple.Direction.FORWARD);
        rf.setDirection(DcMotorSimple.Direction.REVERSE);
        rb.setDirection(DcMotorSimple.Direction.REVERSE);

    }

    @Override
    public void loop() {
        yValue = gamepad1.right_stick_y * -1;
        xValue = gamepad1.right_stick_x * -1;

        leftPower =  yValue - xValue;
        rightPower = yValue + xValue;

        lf.setPower(Range.clip(leftPower, -1.0, 1.0));
        lb.setPower(Range.clip(leftPower, -1.0, 1.0));
        rf.setPower(Range.clip(rightPower, -1.0, 1.0));
        rb.setPower(Range.clip(rightPower, -1.0, 1.0));

        // strafe
        if (gamepad1.right_trigger > 0) {
            lf.setPower(0.75);
            lb.setPower(-0.75);
            rf.setPower(-0.75);
            rb.setPower(0.75);

        } else if (gamepad1.left_trigger > 0) {
            lf.setPower(-0.75);
            lb.setPower(0.75);
            rf.setPower(0.75);
            rb.setPower(-0.75);

        } else if (gamepad1.right_trigger < 0.05 && gamepad1.left_trigger < 0.05 && Math.abs(gamepad1.right_stick_x) < 0.05 && Math.abs(gamepad1.right_stick_y) < 0.05) {
            lf.setPower(0);
            lb.setPower(0);
            rf.setPower(0);
            rb.setPower(0);
        }

        // scissor lift, controlled by gamepad 2
        // right bumper to raise, left bumper to lower
        if (gamepad2.right_bumper) {
            lift1.setPower(1);
            lift2.setPower(-1);

            telemetry.addData("Going up", String.valueOf(lift1.getPower()), lift2.getPower());

        } else if (gamepad2.left_bumper) {
            lift1.setPower(1);
            lift2.setPower(-1);

        } else {
            lift1.setPower(0);
            lift2.setPower(0);
        }

        // grabber, also gamepad 2
        // right trigger to open, left trigger to close? (could be other way around)
        if (gamepad2.right_trigger > 0) {
            grabber.setPower(0.5);

        } else if (gamepad2.left_trigger > 0) {
            grabber.setPower(-0.5);

        } else {
            grabber.setPower(0);
        }
    }
}
