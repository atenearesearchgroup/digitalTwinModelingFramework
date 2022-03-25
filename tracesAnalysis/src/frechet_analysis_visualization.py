import csv
import glob
import os

import numpy as np
from matplotlib import pyplot as plt


def retrieve_csv_files(path: str):
    filenames = []
    for file in os.listdir(path):
        if file.endswith(".csv"):
            filenames.append(file)
    return filenames


def get_reader(filepath: str):
    file = open(filepath, 'r', newline='')
    reader = csv.reader(file, delimiter=";")
    return reader


def retrieve_frechet_values(filepath: str):
    rows = get_reader(filepath)
    pos = []
    euclidean = []
    manhattan = []
    for row in rows:
        pos.append(int(row[0]))
        euclidean.append(float(row[1]))
        manhattan.append(float(row[2]))
    return pos, euclidean, manhattan


def main():
    path = './output_frechet/'
    output_path = "./output_frechet_analysis/"
    frechet_csv_files = retrieve_csv_files(path)

    for csv_file in frechet_csv_files:
        pos, euclidean, manhattan = retrieve_frechet_values(path + csv_file)
        fig, ax = plt.subplots()
        ax.plot(euclidean)
        ax.set_xlabel("Position of the snapshot removed")
        ax.set_ylabel("Frechet distance value")
        fig.savefig(output_path + csv_file.replace(".csv", "") + "Fig2D.png")
        plt.show()


if __name__ == "__main__":
    main()
