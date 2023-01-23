package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

@Autonomous
public class VuforiaConesV2 extends LinearOpMode {
    private DcMotor lf;
    private DcMotor lb;
    private DcMotor rf;
    private DcMotor rb;

    private WebcamName webcam;

    static final double     COUNTS_PER_MOTOR_REV    = 537.7 ;   // eg: GoBILDA 312 RPM Yellow Jacket
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // No External Gearing.
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);

    private OpenGLMatrix lastLocation   = null;
    private VuforiaLocalizer vuforia    = null;
    private VuforiaTrackables targets   = null;

    private static final String VUFORIA_KEY = "ASe2iwz/////AAABmeHBsAW2uUcNt5jOrRunotQpfDI92yTXWsUz3Hr4vE4HA5uZHCa8wHoHvgflwDmAvbiprx9okp4Hubfx6GGKvMoF1QjZXsQemk6tta2n2SoC3s7aUx++tyy+866va7KsJFKMbQ6Zy73N5TA7OwvbJzwp1wfB/CoaiRccCQWGfN3DipfdAuu0namlhr++Ui/p90Fvp2ErUiM0zgSF/5IlBnTEu2nzGEbz3Y66VqK/pqVY5bp7+3opF1IlpTcv9oXW7ihjkiwk2KqboGRnEB2Hq2m2N6YEi2G+6HrV8lWAei1NEhwCy43/v9uDpB2L487S22B48B+3PEumiT4GBoaMGrlXp7A9AUVo3RxZSQM8chCN";

    @Override
    public void runOpMode() throws InterruptedException {
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

        lf = hardwareMap.dcMotor.get("LF");
        lb = hardwareMap.dcMotor.get("LB");
        rf = hardwareMap.dcMotor.get("RF");
        rb = hardwareMap.dcMotor.get("RB");

        waitForStart();

        VuforiaTrackableDefaultListener gears = (VuforiaTrackableDefaultListener) targets.get(0).getListener();
        VuforiaTrackableDefaultListener flowers = (VuforiaTrackableDefaultListener) targets.get(1).getListener();
        VuforiaTrackableDefaultListener stars = (VuforiaTrackableDefaultListener) targets.get(2).getListener();


        while (opModeIsActive()) {
            if (stars.isVisible()) {
                telemetry.addLine("stars");
            } else if (gears.isVisible()) {
                telemetry.addLine("gears");
            } else if (flowers.isVisible()) {
                telemetry.addLine("flowers");
            } else {
                telemetry.addLine("No signals seen");
            }
            telemetry.update();
        }
    }
}
