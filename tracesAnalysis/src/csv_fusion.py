import os

import redis
import matplotlib.pyplot as plt
import numpy as np
import csv

execution_ids = ["1647265216", "1647267374", "1647341760", "1647265953", "1647368167", "1647441825",
                 "1647442107", "1647442384", "1647442606", "1647442809", "1647443106", "1647443475", "1647443878"]
letras = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"]

path = './output_data/'
output_file_csv = path + 'all_traces.csv'

if os.path.exists(output_file_csv):
    os.remove(output_file_csv)
    print("[INFO] The files have been deleted successfully")

f = open(output_file_csv, 'w', newline='')
writer = csv.writer(f, delimiter=";")

for execution_id in execution_ids:
    filename_csv = path + execution_id + '.csv'
    fread = open(filename_csv, 'r', newline='')
    reader = csv.reader(fread, delimiter=";")

    for row in reader:
        writer.writerow([row[0], row[1], letras[execution_ids.index(execution_id)]])

    fread.close()

f.close()
