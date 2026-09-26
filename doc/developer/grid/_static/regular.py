# ---
# jupyter:
#   jupytext:
#     formats: ipynb,py:light
#     text_representation:
#       extension: .py
#       format_name: light
#       format_version: '1.5'
#       jupytext_version: 1.15.1
#   kernelspec:
#     display_name: Python 3 (ipykernel)
#     language: python
#     name: python3
# ---

# # Regular grid
#
# In this section, we describe the layout of regular grid, which are used when using data from CMEMS.
#
# ## Horizontal
#
# Generally, on regular grid, all the data (tracer and velocities) are located at the center of the cells (Arakawa A grid layout).

# +
import os
working_directory = os.getcwd()
working_directory
if working_directory.endswith('_static'):
    outdir = './'
else:
    outdir = os.path.join('grid', '_static')
print("++++++++++++++++++++++++++++++++++++++++++ ", outdir)

def savefig(figname, bbox=None):
    plt.savefig(os.path.join(outdir, figname + '.svg'), bbox_inches=bbox)
    plt.savefig(os.path.join(outdir, figname + '.jpg'), bbox_inches=bbox)


# +
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Ellipse, Polygon
bbox=dict(boxstyle="round,pad=0.5", fc="lightgray", ec="k", lw=2)

xmax = 8 # number of rho points along x
ymax = 5 # number of rho points along y

plt.figure(figsize=(12, 8))
plt.title('Ichthyop layout of ROMS grid')
ax = plt.gca()
ax.set_aspect('equal', 'box')
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(0, xmax + 1) - 0.5, minor=False)
ax.set_yticks(np.arange(0, ymax + 1) - 0.5, minor=False)
plt.grid(True, linewidth=2, linestyle='--', color='k')
bbox['fc'] = 'orange'
for x in range(0, xmax):
    for y in range(0, ymax):
        #lt = plt.plot(x + 0.5, y + 0.5, marker='.', color='g')
        lt = plt.text(x, y, '%d,%d' %(x, y), bbox=bbox, ha='center', va='center', color='k', zorder=100)

# # Plotting U points
# bbox['fc'] = 'firebrick'
# bbox['alpha'] = 1
# for x in np.arange(xmax + 1):
#     for y in range(ymax):
#         #lu = plt.plot(x - 0.5, y, marker='.', color='r')
#         lu = plt.text(x - 0.5, y, '%d,%d' %(x,y), bbox=bbox, ha='center', va='center', color='w', zorder=100)

# # Plotting V points
# bbox['fc'] = 'powderblue'
# for x in np.arange(0, xmax):
#     for y in range(ymax + 1):
#         #lv = plt.plot(x, y - 0.5, marker='.', color='b')
#         lv = plt.text(x, y - 0.5, '%d,%d' %(x,y), bbox=bbox, ha='center', va='center', color='k')

plt.plot([0, 7, 7, 0, 0], [0, 0, 4, 4, 0], lw=6)

#l = plt.legend([lt, lu, lv], ['T', 'U', 'V'], ncol=1, loc='right', bbox_to_anchor=(1.2, 0.6))
savefig('ichthyop_grid_regular')


# -
def plot_grid_layout():
    ax = plt.gca()
    plt.plot([0, 7, 7, 0, 0], [0, 0, 4, 4, 0], lw=3, color='gray')
    ax.set_xticks(np.arange(xmax + 1) - 0.5, minor=False)
    ax.set_yticks(np.arange(ymax + 1) - 0.5, minor=False)
    plt.xlim(0 - 0.5, xmax - 0.5)
    plt.ylim(0 - 0.5, ymax -0.5)
    plt.grid(True, linewidth=1, color='gray', ls='--')


# ### Interpolation
#
# #### T, U and V points
#
# Since all the points are located at the same location, it is the same interpolation method for T, U and V points.

# +
import matplotlib.pyplot as plt
import numpy as np
markers = ['^', '>', '<', 'v']

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=6)
    j = np.floor(jy)
    i = np.floor(ix)
    cpt = 0
    for ii in range(2):
        cox = 1 - abs(ix - (i + ii))
        # print('cox ', cox, ix, i + ii)
        for jj in range(2):
            coy = 1 - abs(jy - (j + jj))
            print('-----')
            print(f'ix={ix}, i={i+ii}, cox={cox:.2f}')
            print(f'jy={jy}, j={j+jj}, coy={coy:.2f}')
            print(f'ti={i + ii}, tj={j + jj}')
            plt.plot(i + ii, j + jj, marker=markers[cpt], color=color, markersize=6, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plot_grid_layout()
[plt.plot([x], [y], marker='o', color='k', markersize=4) for x in range(xmax) for y in range(ymax)]
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 3.1, 'g')
# plt.axis('equal')
ax.set_aspect('equal', adjustable='box')
plt.title('Interpolation of T, U, V variables')
savefig('interpolation_t_regular')
# -

# ### Is on edge

# On regular grids, the particle is considered to be out of the domain when its
# location is out of the bounding box of the T points.

# +
plt.rcParams['lines.markersize'] = 2

plt.figure()
ax = plt.gca()

plot_grid_layout()

color = 'firebrick'
iout = [0 - 0.5, xmax + 0.5, xmax + 0.5, 0 - 0.5]
jout = [0 - 0.5, 0 - 0.5, 0, 0]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'blue'
iout = [0 - 0.5, xmax + 0.5, xmax + 0.5, 0 - 0.5]
jout = [ymax - 1, ymax - 1, ymax, ymax]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'green'
jout = [0, 0, ymax - 1, ymax - 1]
iout = [xmax - 1, xmax, xmax, xmax - 1]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'orange'
jout = [0, 0, ymax - 1, ymax - 1]
iout = [-0.5, 0, 0, -0.5]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

plt.title('Is On Edge')
savefig('is_on_edge_regular', bbox='tight')

# -

# ### Land-sea mask

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(x, y, color):

    print('+++++++++++++ ', x, y)
    plt.plot(x, y, color=color, marker='o', markersize=6)
    i = np.round(x)
    j = np.round(y)
    print(f"mask([{j}, {i}]")

    # i -= 0.5
    # j -= 0.5

    jout = [j - 0.5, j - 0.5, j + 0.5, j + 0.5]
    iout = [i - 0.5, i + 0.5, i + 0.5, i - 0.5]
    points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
    p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

plt.figure()
ax = plt.gca()
plot_grid_layout()
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 3.1, 'g')
plot_points(0, 0, 'c')
plt.title('Land-sea mask')
savefig('land_sea_mask_roms', bbox='tight')


# -

# ### Is close to coast

# +
def plot_poly(i, j, color):

    iout = [i + 0.5 - 0.5, i + 0.5 - 0.5 + 1, i + 0.5 - 0.5 + 1, i + 0.5 - 0.5]
    jout = [j + 0.5 - 0.5, j + 0.5 - 0.5, j + 0.5 - 0.5 + 1, j + 0.5 - 0.5 + 1]
    iout = [i - 0.5, i + 0.5, i + 0.5, i - 0.5, i - 0.5]
    jout = [j - 0.5, j - 0.5, j + 0.5, j + 0.5, j - 0.5]
    points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
    p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))
    plt.plot(i, j, marker='o', color=color)
    return None

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.round(jy)
    i = np.round(ix)
    print(j)
    points = []

    if(jy > j):
        jj = 1
    else:
        jj = -1

    if(ix > i):
        ii = 1
    else:
        ii = -1
    print("---------")
    print(f'y={jy}, x={ix}')
    print(f"grid({j+jj}, {i + ii})")
    plot_poly(i + ii, j + jj, color)

    print(f"grid({j+jj}, {i})")
    plot_poly(i, j + jj, color)

    print(f"grid({j}, {i+ii})")
    plot_poly(i + ii, j, color)

plt.figure()
ax = plt.gca()
plot_grid_layout()

for i in range(xmax):
    for j in range(ymax):
        plt.plot(i, j, marker='o', markersize=3, color='k')

plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 3.1, 'g')
plt.title('Close to Coast')
#savefig('close_to_coast_roms')
