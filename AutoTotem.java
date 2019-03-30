package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.subsystems.Drive;
import org.firstinspires.ftc.teamcode.subsystems.Scaler;
import org.firstinspires.ftc.teamcode.subsystems.Totem;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.FRONT;

@Autonomous(name = "AutoTotem", group = "Autonomus")
//@Disabled
public class AutoTotem extends LinearOpMode {

    private Drive drive;

    private Totem totem;

    private Scaler scaler;

    private boolean targetPosition;

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    private ElapsedTime runtime = new ElapsedTime();

    private static final double     COUNTS_PER_MOTOR_REV    = 560 ;    // eg: TETRIX Motor Encoder
    private static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    private static final double     WHEEL_DIAMETER_INCHES   = 9 / 2.54 ;     // For figuring circumference
    private static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    private static final double     DRIVE_SPEED             = 0.35;
    private static final double     TURN_SPEED              = 0.25;

    //-- Vuforia stuff --//
    private static final String VUFORIA_KEY = "AQpZA03/////AAABmZU2/AujO06TsVydUszxtaBVFHu2K5Y06QD6eyGRoHakHUOVMjNXytikcMY+BrgdgXQS6dfgSId4fZvCnGlIyclru8BIec8w1vr6axIOuqhoukiFpov5vn+EJe2erXLqipum50TTnGbjVWG66pzi9g1TzRcFgC2qSiSm/vAtubyx5LSNgQITM6l96buHNfCXhUeQq7x6JW9u79ctyjTuF2oKiotf8+/8sbLc5Yau3Qg4U6/1bsKDJtc7KVd5oHw0Lvpq/hNhDIuYIrcqaQwPWvGS0U099ZdBUu0jFgsQ/Mgi9+ieM//R9/MytK9ZMmxZ/tGyW12ZwdpcLBGhiuXUlXXZF3Zl9bmBn0r3rn7qshSH";

    // Select which camera you want use.  The FRONT camera is the one on the same side as the screen.
    // Valid choices are:  BACK or FRONT
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = FRONT;

    /*
    private OpenGLMatrix lastLocation = null;
    private boolean targetVisible = false;
    */

    VuforiaTrackables targetsRoverRuckus;

    VuforiaLocalizer vuforia;

    private TFObjectDetector tfod;

    @Override
    public void runOpMode(){
        initVuforia();

        List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(targetsRoverRuckus);

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        drive = new Drive(hardwareMap);
        totem = new Totem(hardwareMap);

        telemetry.addData("Path0",  "Starting at %7f :%7f", (float)drive.getLeftDistance(), (float)drive.getRightDistance());
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            if (tfod != null) {
                tfod.activate();
            }
            //targetsRoverRuckus.activate(); // VUFORIA STUFF
            // checkPosition(allTrackables);

            moveMinerals();

            encoderDrive(DRIVE_SPEED, 12, -12, 4);
            encoderDrive(DRIVE_SPEED, 40,40,6);
            encoderDrive(DRIVE_SPEED, -19.5, 19.5, 2);
            encoderDrive(DRIVE_SPEED, 45, 45, 5);

            totem.set(0.25);
            sleep(1000);
            totem.init();

            encoderDrive(DRIVE_SPEED, -67, -67, 10);

            //sleep(5000);     // pause for servos to move

            //telemetry.addData("Path", "Complete");
            telemetry.update();
        }
    }

    private void moveMinerals(){
        int x = checkMineral();
        if(x>0){
            encoderDrive(DRIVE_SPEED,27,27,4);
            encoderDrive(DRIVE_SPEED,-15,-15,2);
        }
        else{
            encoderDrive(DRIVE_SPEED, 2,-2,1);
            x = checkMineral();
            encoderDrive(DRIVE_SPEED, -2,2,2);
            if(x>0){
                encoderDrive(DRIVE_SPEED,12,12,5);
                encoderDrive(DRIVE_SPEED,8.5,-8.5,2);
                encoderDrive(DRIVE_SPEED, 23,23,5);
                encoderDrive(DRIVE_SPEED, -23,-23,5);
                encoderDrive(DRIVE_SPEED,-8.5,8.5,2);
            }
            else{
                encoderDrive(DRIVE_SPEED,12,12,5);
                encoderDrive(DRIVE_SPEED,-8.5,8.5,2);
                encoderDrive(DRIVE_SPEED,23,23,5);
                encoderDrive(DRIVE_SPEED, -23,-23,5);
                encoderDrive(DRIVE_SPEED,8.5,-8.5,2);
            }
        }
    }

    private void encoderDrive(double speed, double leftInches, double rightInches, double timeoutS) {
        int newLeftTarget;
        int newRightTarget;

        if (opModeIsActive()) {
            newLeftTarget = drive.getLeftDistance() + (int) (leftInches * COUNTS_PER_INCH);
            newRightTarget = drive.getRightDistance() + (int) (rightInches * COUNTS_PER_INCH);
            drive.setLeftTargetPosition(newLeftTarget);
            drive.setRightTargetPosition(newRightTarget);

            drive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();

            drive.setLeftPower(Math.abs(speed));
            drive.setRightPower(Math.abs(speed));

            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (drive.getLeftDrive().isBusy() && drive.getRightDrive().isBusy())) {

                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d", drive.getLeftDistance(), drive.getRightDistance());
                telemetry.update();
            }

            drive.setLeftPower(0);
            drive.setRightPower(0);

            drive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }

    private void checkPosition(List<VuforiaTrackable> allTrackables){
        boolean targetIsVisible = false;
        while (opModeIsActive() && !targetIsVisible) {
            for (VuforiaTrackable trackable : allTrackables) {
                if (((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()) {
                    telemetry.addData("Visible Target", trackable.getName());
                    targetIsVisible = true;
                    break;
                }
            }
            if (!targetIsVisible) {
                telemetry.addData("Visible Target", "none");
            }
        }
        telemetry.update();
    }

    private int checkMineral(){
        targetPosition = false;
        runtime.reset();
        int goldMineralX = -1;
        while (opModeIsActive() && !targetPosition && runtime.time() < 2) {
            if (tfod != null) {
                // getUpdatedRecognitions() will return null if no new information is available since
                // the last time that call was made.
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    if (updatedRecognitions.size() >= 1) {
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                goldMineralX = (int) recognition.getLeft();
                                targetPosition = true;
                            }
                        }
                        telemetry.addData("Position:",goldMineralX);
                        telemetry.update();
                    }
                }
            }
        }
        return goldMineralX;
    }

    private void initVuforia() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY ;
        parameters.cameraDirection   = CAMERA_CHOICE;

        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Load the data sets that for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        targetsRoverRuckus = this.vuforia.loadTrackablesFromAsset("RoverRuckus");
        VuforiaTrackable blueRover = targetsRoverRuckus.get(0);
        blueRover.setName("Blue-Rover");
        VuforiaTrackable redFootprint = targetsRoverRuckus.get(1);
        redFootprint.setName("Red-Footprint");
        VuforiaTrackable frontCraters = targetsRoverRuckus.get(2);
        frontCraters.setName("Front-Craters");
        VuforiaTrackable backSpace = targetsRoverRuckus.get(3);
        backSpace.setName("Back-Space");
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

}
