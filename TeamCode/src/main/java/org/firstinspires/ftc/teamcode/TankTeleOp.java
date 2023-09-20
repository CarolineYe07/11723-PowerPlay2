package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class TankTeleOp extends OpMode {

    private DcMotor lf, lb, rf, rb;

    @Override
    public void init() {
        lf = hardwareMap.get(DcMotor.class, "LF");
        lb = hardwareMap.get(DcMotor.class, "LB");
        rf = hardwareMap.get(DcMotor.class, "RF");
        rb = hardwareMap.get(DcMotor.class, "RB");

        rf.setDirection(DcMotorSimple.Direction.REVERSE);
        rb.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void loop() {
        double leftPower = gamepad1.left_stick_y;
        double rightPower = gamepad1.right_stick_y;

        lf.setPower(leftPower);
        rf.setPower(rightPower);
    }
}

