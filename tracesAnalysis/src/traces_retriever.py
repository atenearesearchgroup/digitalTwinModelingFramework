import redis
import matplotlib.pyplot as plt
import numpy as np

execution_id = "1646935565"

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
    time = int(values['timestamp'.encode('utf-8')]) - int(execution_id)
    ax.scatter(x_pos, y_pos, time, marker='^')
    ax2.scatter(x_pos, y_pos, marker='o')

ax.set_xlabel('xPos')
ax.set_ylabel('yPos')
ax.set_zlabel('Time')

ax2.set_xlabel('xPos')
ax2.set_ylabel('yPos')

plt.show()
