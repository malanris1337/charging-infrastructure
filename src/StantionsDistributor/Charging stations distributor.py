from skimage.feature import peak_local_max
import numpy as np
import pandas as pd

slow = ['apartments']
middle = ['office', 'university']
fast = ['road', 'commercial']
values = [3.5, 50, 150]
S_max = 70_000
infrastructure_filename, distribution_filename = 'new - l.csv', 'result - l.csv'

df = pd.read_csv(distribution_filename, header=None)
w, h = df.shape[1] - 1, df.shape[0]
arr = [['' for x in range(w)] for y in range(h)]
coordinates = [[0 for x in range(w)] for y in range(h)]

for i in range(h):
    for j in range(w):
        arr[i][j] = float(df[j][i].split(";")[0])

peaks = list(peak_local_max(np.array(arr), min_distance=2, exclude_border=False))

for i in range(h):
    for j in range(w):
        coordinates[i][j] = [float(df[j][i].split(";")[1]), float(df[j][i].split(";")[2])]

centers_coords = []
for el in peaks:
    print(str(coordinates[el[0]][el[1]]) + ',')
    centers_coords.append(coordinates[el[0]][el[1]])

centers_coords = np.array(centers_coords)
infrastructure = pd.read_csv(infrastructure_filename)

obj_class = []
# разделим всю инфраструктуру исходя из центров
for index, row in infrastructure.iterrows():
    coord = np.array([row['width'], row['longitude']])
    c = np.argmin(np.linalg.norm(centers_coords - coord, axis=-1))
    obj_class.append(c)
infrastructure['class'] = obj_class

centers_n = len(centers_coords)
charges_types = np.zeros((centers_n, 3))
people_by_centers = []
for c in range(centers_n):
    peoples = 0
    for index, row in infrastructure[infrastructure['class'] == c].iterrows():
        peoples += row['average_people_value']
        if row['type'] in slow:
            charges_types[c, 0] += 1
        elif row['type'] in middle:
            charges_types[c, 1] += 1
        elif row['type'] in fast:
            charges_types[c, 2] += 1
    people_by_centers.append(peoples)
    if np.sum(charges_types[c]) != 0:
        charges_types[c] = charges_types[c] / np.sum(charges_types[c])
S_by_centers = (35 * 70) * np.array(people_by_centers) / sum(people_by_centers)
print('-------')
for c in range(centers_n):
    charges_nums = np.append(np.ceil(charges_types[c] * S_by_centers[c] / values), S_by_centers[c])
    print(str(list(charges_nums)) + ',')
