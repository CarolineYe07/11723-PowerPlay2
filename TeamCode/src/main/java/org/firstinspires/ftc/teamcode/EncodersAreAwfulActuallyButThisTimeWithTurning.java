package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.ZYX;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

@Autonomous
public class EncodersAreAwfulActuallyButThisTimeWithTurning extends LinearOpMode {
    // This is the current auto I think we're going to end up using

    private DcMotor lf, rf, lb, rb;

    public static final double ticksPerMotorRev = 383.6;
    public static final double driveGearReduction = 0.5;
    public static final double wheelDiameterInches = 4;
    public static final double ticksPerInch = (ticksPerMotorRev * driveGearReduction) / (wheelDiameterInches * 3.14159265359);

    BNO055IMU imu;
    private Orientation angles;

    public static final double kP = 0.005;
    public static final double kD = 0.01;
    public static final double kI = 0.00008;

    private ElapsedTime runtime = new ElapsedTime();

    private WebcamName webcam;

    private OpenGLMatrix lastLocation = null;
    private VuforiaLocalizer vuforia = null;
    private VuforiaTrackables targets = null;

    private static final String VUFORIA_KEY = "ASe2iwz/////AAABmeHBsAW2uUcNt5jOrRunotQpfDI92yTXWsUz3Hr4vE4HA5uZHCa8wHoHvgflwDmAvbiprx9okp4Hubfx6GGKvMoF1QjZXsQemk6tta2n2SoC3s7aUx++tyy+866va7KsJFKMbQ6Zy73N5TA7OwvbJzwp1wfB/CoaiRccCQWGfN3DipfdAuu0namlhr++Ui/p90Fvp2ErUiM0zgSF/5IlBnTEu2nzGEbz3Y66VqK/pqVY5bp7+3opF1IlpTcv9oXW7ihjkiwk2KqboGRnEB2Hq2m2N6YEi2G+6HrV8lWAei1NEhwCy43/v9uDpB2L487S22B48B+3PEumiT4GBoaMGrlXp7A9AUVo3RxZSQM8chCN";

    @Override
    public void runOpMode() throws InterruptedException {
        // vuforia stuff
        webcam = hardwareMap.get(WebcamName.class, "Webcam 1");

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = webcam;

        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        targets = this.vuforia.loadTrackablesFromAsset("PowerPlay2");
        targets.get(0).setName("gears");
        targets.get(1).setName("flowers");
        targets.get(2).setName("stars");

        targets.activate();

        // hardware
        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        imu.initialize(params);

        telemetry.addLine("IMU Initialized");
        telemetry.update();

        lf = hardwareMap.dcMotor.get("LF");
        lb = hardwareMap.dcMotor.get("LB");
        rf = hardwareMap.dcMotor.get("RF");
        rb = hardwareMap.dcMotor.get("RB");

        rf.setDirection(DcMotorSimple.Direction.REVERSE);
        rb.setDirection(DcMotorSimple.Direction.REVERSE);

        // brake motors for more precision?
        lf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lb.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rb.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        lf.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        lb.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rf.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rb.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        telemetry.addLine("Hardware Initialized");
        telemetry.update();

        waitForStart();

        // set up vuforia targets
        VuforiaTrackableDefaultListener gears = (VuforiaTrackableDefaultListener) targets.get(0).getListener();
        VuforiaTrackableDefaultListener flowers = (VuforiaTrackableDefaultListener) targets.get(1).getListener();
        VuforiaTrackableDefaultListener stars = (VuforiaTrackableDefaultListener) targets.get(2).getListener();

        telemetry.addLine("Vuforia Initialized");
        telemetry.update();

        sleep(1000);

        int forward_dist = 50;
        int strafe_dist = 18;
        double speed = 0.75;
        int timeoutS = 300;

        if (flowers.isVisible()) {
            // location 1
            telemetry.addLine("flowers");
            telemetry.update();

            encoderDrive(speed, strafe_dist, timeoutS, true);
            encoderDrive(speed, forward_dist, timeoutS, false);

        } else if (gears.isVisible()) {
            // location 2
            telemetry.addLine("gears");
            telemetry.update();

            encoderDrive(speed, forward_dist, timeoutS, false);


        } else if (stars.isVisible()) {
            // location 3

            telemetry.addLine("stars");
            telemetry.update();

            encoderDrive(speed, -strafe_dist, timeoutS, true);
            encoderDrive(speed, forward_dist, timeoutS, false);


        } else {
            telemetry.addLine("No signals seen");
            telemetry.update();


        }


    }

    public void encoderDrive(double speed, double inches, double timeoutS, boolean strafe) {
        int newlfTarget, newlbTarget, newrfTarget, newrbTarget;

        int lfPos = lf.getCurrentPosition();
        int rfPos = rf.getCurrentPosition();
        int lbPos = lb.getCurrentPosition();
        int rbPos = lb.getCurrentPosition();

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, ZYX, AngleUnit.RADIANS); // try changing to radians?
        double startAngle = angles.firstAngle;

        if (strafe) {
            newlfTarget = lfPos + (int) (inches * ticksPerInch);
            newrfTarget = rfPos - (int) (inches * ticksPerInch);
            newlbTarget = lbPos - (int) (inches * ticksPerInch);
            newrbTarget = rbPos + (int) (inches * ticksPerInch);
        } else {
            newlfTarget = lfPos + (int) (inches * ticksPerInch);
            newrfTarget = rfPos + (int) (inches * ticksPerInch);
            newlbTarget = lbPos + (int) (inches * ticksPerInch);
            newrbTarget = rbPos + (int) (inches * ticksPerInch);
        }

        lf.setTargetPosition(newlfTarget);
        lb.setTargetPosition(newlbTarget);
        rf.setTargetPosition(newrfTarget);
        rb.setTargetPosition(newrbTarget);

        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        runtime.reset();


        while (runtime.seconds() < timeoutS && (lf.isBusy() && rf.isBusy() && lb.isBusy() && rb.isBusy())) {
            lf.setPower(Math.abs(speed));
            lb.setPower(Math.abs(speed));
            rf.setPower(Math.abs(speed));
            rb.setPower(Math.abs(speed));

            telemetry.addData("LF Power", lf.getPower());
            telemetry.addData("LB Power", lb.getPower());
            telemetry.addData("RF Power", rf.getPower());
            telemetry.addData("RB Power", rb.getPower());

            telemetry.addData("LF Position", lf.getCurrentPosition());
            telemetry.addData("LB Position", lb.getCurrentPosition());
            telemetry.addData("RF Position", rf.getCurrentPosition());
            telemetry.addData("RB Position", rb.getCurrentPosition());
            telemetry.update();
        }


        lf.setPower(0);
        lb.setPower(0);
        rf.setPower(0);
        rb.setPower(0);

        lf.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        lb.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rf.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rb.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        sleep(100);

    }
}

