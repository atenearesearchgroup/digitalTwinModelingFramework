from pyniryo import *
import redis
import math

def main():
    robot = connect_arm("192.168.0.1")
    robot.move_joints([math.pi/2, math.pi/2, math.pi/2, math.pi/2, math.pi/2, math.pi/2])
    disconnect_arm()


def connect_arm(ip_address):
    # Connect to robot & calibrate
    robot = NiryoRobot(ip_address)
    print("[PT-INFO] Robot connected successfully.")
    robot.calibrate_auto()
    print("[PT-INFO] Robot calibrated successfully.")
    robot.update_tool()
    print("[PT-INFO] Tool updated successfully.")
    return robot


def disconnect_arm(robot):
    robot.go_to_sleep()
    robot.close_connection()
    print("[PT-INFO] Robot disconnected successfully.")


if __name__ == "__main__":
    main()
    print("Process completed successfully")
