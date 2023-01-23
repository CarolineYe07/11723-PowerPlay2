package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

@TeleOp
public class justinCode extends OpMode{
    private DcMotor leftBack;
    private DcMotor leftFront;
    private DcMotor rightBack;
    private DcMotor rightFront;
    private DcMotor fourBar;
    private CRServo clawIntake;

    private float leftPower, rightPower, xValue, yValue;
    @Override
    public void init(){
        leftFront = hardwareMap.dcMotor.get("leftFront");
        leftBack = hardwareMap.dcMotor.get("leftBack");
        rightFront = hardwareMap.dcMotor.get("rightFront");
        rightBack = hardwareMap.dcMotor.get("rightBack");

        //fourBar.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void loop(){
        yValue = gamepad1.left_stick_y * -1;
        xValue = gamepad1.left_stick_x * -1;

        leftPower =  yValue - xValue;
        rightPower = yValue + xValue;

        if (gamepad1.right_trigger > 0) {
            leftFront.setPower(1);
            leftBack.setPower(-1);
            rightFront.setPower(-1);
            rightBack.setPower(1);
        }
        if (gamepad1.left_trigger > 0) {
            leftFront.setPower(-1);
            leftBack.setPower(1);
            rightFront.setPower(1);
            rightBack.setPower(-1);
        }

        if(gamepad2.a){
            clawIntake.setPower(.5);
        } else if(gamepad2.b){
            clawIntake.setPower(-.5);
        } else if(!gamepad2.a && !gamepad2.b){
            clawIntake.setPower(0.0);
        }

        if(gamepad2.left_bumper){
            fourBar.setPower(.25);
        }else if(gamepad2.right_bumper){
            fourBar.setPower(-.75);
        }
    }
}
