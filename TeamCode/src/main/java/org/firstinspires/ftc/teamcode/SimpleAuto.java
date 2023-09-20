package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

public class SimpleAuto extends LinearOpMode {

    private DcMotor lf, lb, rf, rb;
    private ElapsedTime runtime = new ElapsedTime();
    private int motorSpeed = 1;

    @Override
    public void runOpMode() throws InterruptedException {
        lf = hardwareMap.get(DcMotor.class, "LF");
        lb = hardwareMap.get(DcMotor.class, "LB");
        rf = hardwareMap.get(DcMotor.class, "RF");
        rb = hardwareMap.get(DcMotor.class, "RB");

        rf.setDirection(DcMotorSimple.Direction.REVERSE);
        rb.setDirection(DcMotorSimple.Direction.REVERSE);

        waitForStart();

        while(opModeIsActive()) {
            runtime.reset();
            // move forward
            while(runtime.seconds() < 1) {
                lf.setPower(motorSpeed);
                lb.setPower(motorSpeed);
                rf.setPower(motorSpeed);
                rb.setPower(motorSpeed);
            }

            runtime.reset();
            // strafe
            while(runtime.seconds() < 0.5) {
                lf.setPower(motorSpeed);
                lb.setPower(-motorSpeed);
                rf.setPower(-motorSpeed);
                rb.setPower(motorSpeed);
            }

            telemetry.addData("Left motor power", motorSpeed);
            telemetry.addLine("HI");
            telemetry.update();
        }
    }

    void moveForward(int motorSpeed) {
        lf.setPower(motorSpeed);
        lb.setPower(motorSpeed);
        rf.setPower(motorSpeed);
        rb.setPower(motorSpeed);
    }
}

