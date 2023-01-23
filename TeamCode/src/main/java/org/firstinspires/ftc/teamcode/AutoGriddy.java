package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@Disabled
public class AutoGriddy extends LinearOpMode {
    private DcMotor lf;
    private DcMotor lb;
    private DcMotor rf;
    private DcMotor rb;

    private ElapsedTime runtime = new ElapsedTime();
    @Override
    public void runOpMode() throws InterruptedException {
        lf = hardwareMap.dcMotor.get("LF");
        lb = hardwareMap.dcMotor.get("LB");
        rf = hardwareMap.dcMotor.get("RF");
        rb = hardwareMap.dcMotor.get("RB");

        waitForStart();

        runtime.reset();
        while (opModeIsActive() && runtime.seconds() < 1) {
            lf.setPower(0.5);
        }
    }
}
