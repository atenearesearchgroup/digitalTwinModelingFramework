import os

import redis
import matplotlib.pyplot as plt
import numpy as np
import csv

execution_id = "1647443878"
path = './output_data/'
filename_csv = path + execution_id + '.csv'
filename_3d_graph = path + execution_id + 'Fig3D.png'
filename_2d_graph = path + execution_id + 'Fig2D.png'

if os.path.exists(filename_csv):
    os.remove(filename_csv)
    os.remove(filename_3d_graph)
    os.remove(filename_2d_graph)
    print("[INFO] The files have been deleted successfully")

f = open(filename_csv, 'w', newline='')
writer = csv.writer(f, delimiter=";")

r = redis.Redis(host="localhost", port=6379)
snapshots = r.zrange("PT:NXJCar:" + execution_id + ":EXECUTIONID_LIST", 0, -1, False)

fig = plt.figure(1)
ax = fig.add_subplot(projection='3d')
fig2 = plt.figure(2)
ax2 = fig2.add_subplot()

for snp in snapshots:
    values = r.hgetall(snp)
    x_pos = float(values['xPos'.encode('utf-8')])
    y_pos = float(values['yPos'.encode('utf-8')])
    timestamp = int(values['timestamp'.encode('utf-8')])
    time = timestamp - int(execution_id)
    action = str(values['action'.encode('utf-8')]).replace('b\'', '').replace('\'', '')
    writer.writerow([x_pos, y_pos])
    ax.scatter(x_pos, y_pos, time, marker='^')
    ax2.scatter(x_pos, y_pos, marker='o')

ax.set_xlabel('xPos')
ax.set_ylabel('yPos')
ax.set_zlabel('Time')

ax2.set_xlabel('xPos')
ax2.set_ylabel('yPos')

fig.savefig(filename_3d_graph)
fig2.savefig(filename_2d_graph)

plt.show()
plt.close(fig)
plt.close(fig2)

f.close()
