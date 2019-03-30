package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.subsystems.Arm;
import org.firstinspires.ftc.teamcode.subsystems.Drive;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Scaler;
import org.firstinspires.ftc.teamcode.subsystems.Totem;

@TeleOp(name="OpMode", group="Iterative Opmode")
//@Disabled
public class Robot extends OpMode
{
    private ElapsedTime runtime = new ElapsedTime();
    private Drive drive;
    private Arm arm;
    private Intake intake;
    private Scaler scaler;
    private Totem totem;
    private boolean flag;
    private double power;
    @Override
    public void init(){
        telemetry.addData("Status", "Initializing");
        drive = new Drive(hardwareMap);
        arm = new Arm(hardwareMap);
        intake = new Intake(hardwareMap);
        totem = new Totem(hardwareMap);
        scaler = new Scaler(hardwareMap);
        telemetry.addData("Status", "Initialized");
        flag=false;
        power = 1.0;
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        runtime.reset();
        totem.init();
    }

    @Override
    public void loop(){
        power = (gamepad1.left_bumper ? 0.4 : 1.0) * 1;

        drive.tweenedDrive(gamepad1.left_stick_y * power, -gamepad1.right_stick_x * 0.75 * power);
        arm.set(gamepad1.y ? 1.0 : gamepad1.a ? -1 : 0);
        intake.set(gamepad1.right_bumper ? 1.0 : gamepad1.x ? -1.0 : flag ? 0.2 : -0.2);
        flag = !flag;
        scaler.set(gamepad1.left_trigger > 0.5 ? 1.0 : gamepad1.right_trigger > 0.5 ? -1.0 : 0.0);
        /*
        if(gamepad1.right_bumper){
            intake.set(1.0);
        }
        else if(gamepad1.x){
            intake.set(-1.0);
        }
        else{
            intake.set(0.2);
            intake.set(-0.1);
        }
        */
        /*
        if(gamepad1.left_trigger > 0.1){
            scaler.set(gamepad1.left_trigger);
        }
        else if(gamepad1.right_trigger > 0.1){
            scaler.set(-gamepad1.right_trigger);
        }
        else{
            scaler.set(0);
        }
        */

        telemetry.addData("Status", "Run Time: " + runtime.toString());
        //telemetry.addData("Motors", "left (%.2f), right (%.2f)", (float)drive.getLeftSpeed(), (float)drive.getRightSpeed());
        //telemetry.addData("Distance", "leftD (%.2f), rightD (%.2f)", (float)drive.getLeftDistance(), (float)drive.getRightDistance());
        //telemetry.addData("ArmMotors", "left (%.2f), right (%.2f)", (float)arm.getLeftSpeed(), (float)arm.getRightSpeed());
        //telemetry.addData("ArmDistance", "leftD (%.2f), rightD (%.2f)"|, (float)arm.getLeftDistance(), (float)arm.getRightDistance());
        telemetry.update();
    }

    @Override
    public void stop() {
    }
}
