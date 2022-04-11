import numpy as np
import redis
import similaritymeasures
import os
import csv

from matplotlib import pyplot as plt


def retrieve_snapshots_from_db(execution_id):
    r = redis.Redis(host="localhost", port=6379)

    snapshots = r.zrange("PT:NXJCar:" + execution_id + ":EXECUTIONID_LIST", 0, -1, False)

    x = []
    y = []
    # times = []
    result = np.zeros((len(snapshots), 3))

    for value in snapshots:
        values = r.hgetall(value)
        # POSITION #
        x.append(float(values['xPos'.encode('utf-8')]))
        y.append(float(values['yPos'.encode(
            'utf-8')]))
        # TIME #
        # timestamp = int(values['timestamp'.encode('utf-8')])
        # times.append(timestamp - int(execution_id))

    result[:, 0] = x
    result[:, 1] = y
    # result[:, 2] = times

    return result


def create_output_files(path: str, execution_id_a: str, execution_id_b: str):
    filename_csv = path + execution_id_a + "-" + execution_id_b + '.csv'
    if os.path.exists(filename_csv):
        os.remove(filename_csv)
    file = open(filename_csv, 'w', newline='')
    writer = csv.writer(file, delimiter=";")
    fig = plt.figure()
    ax = fig.add_subplot()

    return file, writer, fig, ax


def main():
    execution_ids = ["1647265216", "1647267374", "1647341760", "1647265953", "1647368167", "1647441825",
                     "1647442107", "1647442384", "1647442606", "1647442809", "1647443106", "1647443475", "1647443878"]

    path = './output_frechet/'

    for i in range(len(execution_ids)):
        execution_id_a = execution_ids[i]
        snapshots_a = retrieve_snapshots_from_db(execution_id_a)
        for j in range(i + 1, len(execution_ids)):
            if i != j:
                execution_id_b = execution_ids[j]
                snapshots_b = retrieve_snapshots_from_db(execution_id_b)

                file, writer, fig, ax = create_output_files(path, execution_id_a, execution_id_b)

                ax.scatter(snapshots_a[:, 0], snapshots_a[:, 1], marker='^', color="gold")
                ax.scatter(snapshots_b[:, 0], snapshots_b[:, 1], marker='o', color="darkturquoise")

                frechet_euclidean = similaritymeasures.frechet_dist(snapshots_a, snapshots_b, 2)
                frechet_manhattan = similaritymeasures.frechet_dist(snapshots_a, snapshots_b, 1)
                writer.writerow([-1, frechet_euclidean, frechet_manhattan])

                len_a = len(snapshots_a)
                len_b = len(snapshots_b)
                for k in range(len_a) if len_a < len_b else range(len_b):
                    a = list(snapshots_a[:k]) + list(snapshots_a[(k + 1):])
                    b = list(snapshots_b[:k]) + list(snapshots_b[(k + 1):])
                    frechet_euclidean_removed = similaritymeasures.frechet_dist(a, b, 2)
                    frechet_manhattan_removed = similaritymeasures.frechet_dist(a, b, 1)
                    writer.writerow([k, frechet_euclidean_removed, frechet_manhattan_removed])

                    if frechet_euclidean > frechet_euclidean_removed or frechet_manhattan > frechet_manhattan_removed:
                        print(execution_id_a + " == " + execution_id_b + " -- " + str(
                            k) + " -- Frechet with euclidean distance: " + str(frechet_euclidean_removed))
                        print(execution_id_a + " == " + execution_id_b + " -- " + str(
                            k) + " -- Frechet with Manhattan distance: " + str(frechet_manhattan_removed))
                        ax.scatter(snapshots_a[k, 0], snapshots_a[k, 1], marker='<', color="red")
                        ax.annotate(k, (snapshots_a[k, 0], snapshots_a[k, 1]))
                        ax.scatter(snapshots_b[k, 0], snapshots_b[k, 1], marker='>', color="red")
                        ax.annotate(k, (snapshots_b[k, 0], snapshots_b[k, 1]))

                        filename_2d_graph = path + execution_id_a + "-" + execution_id_b + "-" + str(
                            k) + "-" + 'Fig2D.png'
                        if os.path.exists(filename_2d_graph):
                            os.remove(filename_2d_graph)
                            print("[INFO] The files have been deleted successfully")

                        ax.set_xlabel('xPos')
                        ax.set_ylabel('yPos')
                        fig.savefig(filename_2d_graph)

                        plt.show()

                plt.close(fig)
                file.close()


if __name__ == "__main__":
    main()
    print("Process completed successfully")
