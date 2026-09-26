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

import os
import matplotlib.ticker as ticker
working_directory = os.getcwd()
working_directory
if working_directory.endswith('_static'):
    outdir = './'
else:
    outdir = os.path.join('grid', '_static')
print("++++++++++++++++++++++++++++++++++++++++++ ", outdir)

# # NEMO grid
#
# In this section, the main features of the NEMO grid and the implications in Ichthyop are summarized.
#
# ## Horizontal
#
# ### Indexing
#
# The horizontal layout of the NEMO grid is as follows:
#
# <img src="https://www.nemo-ocean.eu/doc/img360.png">
#
# Tracer points (`T` points) are stored at the center of the cell. Zonal velocities (`U` points) are stored on the eastern face, while meridional velocities (`V` points) are stored on the northern face. **Contrary to ROMS, U, V and T points have the same number of elements on the NEMO grid!**

# +
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Ellipse, Polygon
import matplotlib.ticker as ticker
bbox=dict(boxstyle="round,pad=0.5", fc="lightgray", ec="k", lw=2)

xmax = 8 # number of rho points along x
ymax = 5 # number of rho points along y

plt.figure(figsize=(12, 8))
plt.title('Ichthyop layout of NEMO grid')
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

# Plotting U points
bbox['fc'] = 'firebrick'
bbox['alpha'] = 1
for x in np.arange(xmax):
    for y in range(ymax):
        #lu = plt.plot(x - 0.5, y, marker='.', color='r')
        lu = plt.text(x + 0.5, y, '%d,%d' %(x,y), bbox=bbox, ha='center', va='center', color='w', zorder=100)

# Plotting V points
bbox['fc'] = 'powderblue'
for x in np.arange(0, xmax):
    for y in range(ymax):
        #lv = plt.plot(x, y - 0.5, marker='.', color='b')
        lv = plt.text(x, y + 0.5, '%d,%d' %(x,y), bbox=bbox, ha='center', va='center', color='k')

plt.plot([0.5, 7, 7, 0.5, 0.5], [0.5, 0.5, 4, 4, 0.5], lw=6)


# +
def plot_grid_layout():
    ax = plt.gca()
    plt.plot([0, 7, 7, 0, 0], [0, 0, 4, 4, 0], lw=3, color='gray')
    ax.set_xticks(np.arange(xmax + 1) - 0.5, minor=False)
    ax.set_yticks(np.arange(ymax + 1) - 0.5, minor=False)
    plt.xlim(0 - 0.5, xmax - 0.5)
    plt.ylim(0 - 0.5, ymax -0.5)
    plt.grid(True, linewidth=1, color='gray', ls='--')
    plt.plot([0.5, 7, 7, 0.5, 0.5], [0.5, 0.5, 4, 4, 0.5], lw=2, ls='--', color='red')


# -

# ## Scale factors
#
# In NEMO, the zonal and meridional length of the cells are stored in the `e1x` and `e2x` variables, with `x` equals to `t`, `u` or `v` depending on the point considered.
#
# ### Zonal cyclicity
#
# For global NEMO simulations, which runs on the `ORCA` grid, the zonal cyclicity is as follows (indexes are provided for `T` points):

# + nbsphinx="hidden"
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Ellipse, Polygon
import matplotlib.ticker as ticker

fig = plt.figure()
ax = plt.gca()
plot_grid_layout()

color = 'firebrick'
x0 = -0.5
alpha = 0.3
p = ax.add_patch(Polygon([(x0, 0 - 0.5), (x0 + 1, 0  - 0.5), (x0 + 1, ymax), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color, alpha=alpha))
x0 = xmax - 2
p = ax.add_patch(Polygon([(x0, 0 - 0.5), (x0 + 1, 0  - 0.5), (x0 + 1, ymax), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color, alpha=alpha))

color = 'steelblue'
x0 = 0.5
p = ax.add_patch(Polygon([(x0, 0  - 0.5), (x0 + 1, 0  - 0.5), (x0 + 1, ymax ), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color, alpha=alpha))
x0 = xmax - 1
p = ax.add_patch(Polygon([(x0, 0  - 0.5), (x0 + 1, 0  - 0.5), (x0 + 1, ymax), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color, alpha=alpha))
plt.title('Zonal cyclicity')
plt.savefig(os.path.join(outdir, 'zonal_cyclicity_nemo.jpg'))
# -

# ### Interpolation
#
# #### Interpolation of T variable
#
# Given a given position index of a particle with the `T` grid, the determination of the interpolation is done as follows:
#
# - First, the `i` index of the `T` grid column left of the particle is found. This is done by using `floor` on the `x` value
# - Then, the `j` index of the `T` grid line below the particle is found. This is done by using `floor` on the `y` value
# - The area to consider is defined by the `[i, i + 1]` and `[j, j + 1]` squares.
#
# An illustration is given below
#

# +
plt.rcParams['lines.markersize'] = 2
markers = ['^', '>', '<', 'v']

def plot_points(ix, jy, color):
    cpt = 0
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='x', color=color, markersize=4)
    j = np.floor(jy)
    i = np.floor(ix)
    for ii in range(2):
        cox = 1 - abs(ix  - (i + ii))
        for jj in range(2):
            coy = 1 - abs(jy - (j + jj))
            print('-----')
            print(f'ix={ix}, i={i+ii}, cox={cox:.2f}')
            print(f'jy={jy}, j={j+jj}, coy={coy:.2f}')
            print(f'ti={i + ii}, tj={j + jj}')
            #print('dx = ', cox, 'dy = ', coy)
            plt.plot(i + ii, j + jj, marker=markers[cpt], color=color, markersize=4, linestyle='none')
            cpt += 1

fig = plt.figure()
ax = plt.gca()
plot_grid_layout()
[plt.plot([x], [y], marker='o', color='k', markersize=4) for x in range(xmax) for y in range(ymax)]
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(0.6, 0.6, 'g')
plot_points(6.3, 1.7, 'orange')
#plot_points(11, 3, 'plum')
plt.title('Interpolation of T variables')
plt.savefig(os.path.join(outdir, 't_interpolation_nemo.jpg'))
# -

# #### Interpolation of U variables
#
# Interpolation of `U` variables is done as follows:
#
# - First, the `i` index of the `U` point right of the particle is found by using `round(x)`.
# - Then, the `j` index of the `U` grid line below the particle is found. This is done by using `floor` on the `y` value
# - The box used to average the variable is therefore defined by the `[i - 1, i]` and `[j, j + 1]` squares.

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)

    plt.plot(ix, jy, marker='x', color=color, markersize=4)
    j = np.floor(jy)

    # ix is in the reference of T points.
    # to extract the index, we need to move its value to the
    # U reference by removing 0.5
    i = np.floor(ix - 0.5)
    cpt = 0
    for ii in range(2):
        for jj in range(2):
            coy = 1 - abs(jy - (j + jj))
                
            # here, i is the index of the U points on
            # the U referentiel. We need to move it back
            # to the T points by adding 0.5
            cox = 1 - abs(ix - (i + 0.5 + ii))
            print('-----')
            print(f'ix={ix}, i={i+0.5+ii}, cox={cox:.2f}')
            print(f'jy={jy}, j={j+jj}, coy={coy:.2f}')
            print(f'ui={i + ii}, uj={j + jj}')
            plt.plot(i + ii + 0.5, j + jj, marker=markers[cpt], color=color, markersize=4, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plot_grid_layout()
[plt.plot([x + 0.5], [y], marker='o', color='k', markersize=3) for x in range(xmax) for y in range(ymax)]
plt.grid(True)
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(0.6, 0.6, 'g')
plot_points(6.3, 1.7, 'orange')
plt.title('Interpolation of U variables')
plt.savefig(os.path.join(outdir, 'u_interpolation_nemo.jpg'))
# -

# #### Interpolation of V variables

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='x', color=color, markersize=4)
    j = np.floor(jy - 0.5)
    i = np.floor(ix)
    cpt = 0
    for ii in range(2):
        cox = 1 - abs(ix - (i + ii))
        for jj in range(2):
            coy = 1 - abs(jy - (j + 0.5 + jj ))
            print('-----')
            print(f'ix={ix}, i={i+ii}, cox={cox:.2f}')
            print(f'jy={jy}, j={j+0.5 + jj}, coy={coy:.2f}')
            print(f'vi={i + ii}, vj={j + jj}')
            plt.plot(i + ii, j + 0.5 + jj, marker=markers[cpt], color=color, markersize=4, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plot_grid_layout()
[plt.plot([x], [y + 0.5], marker='o', color='k') for x in range(xmax) for y in range(ymax)]
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(0.6, 0.6, 'g')
plot_points(6.3, 1.7, 'orange')
plt.title('Interpolation of V variables')
plt.savefig(os.path.join(outdir, 'v_interpolation_nemo.jpg'))


# -

# ### Land sea-mask

# +

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.round(jy)
    i = np.round(ix)
    points = []

    iout = [i - 0.5, i + 0.5, i + 0.5, i - 0.5, i - 0.5]
    jout = [j - 0.5, j - 0.5, j + 0.5, j + 0.5, j - 0.5]
    points = [(ii, jj) for ii, jj in zip(iout, jout)]
    p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))
    plt.plot(i, j, marker='o', color=color)

plt.figure()
ax = plt.gca()
plot_grid_layout()
[plt.plot([x], [y], marker='o', color='k') for x in range(xmax) for y in range(ymax)]
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(6.2, 2.1, 'g')
plot_points(0.6, 2.7, 'orange')
plt.title('Land-sea mask')
plt.savefig(os.path.join(outdir, 'landsea_mask_nemo.jpg'))


# -

# ### Close to coast

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
[plt.plot([x], [y], marker='o', color='k') for x in range(xmax) for y in range(ymax)]
plot_points(5.7, 6.1, 'r')
plot_points(4.7, 2.7, 'b')
plot_points(2.8, 1.1, 'g')
plot_points(1.3, 3.2, 'orange')
plt.title('Close to Coast')
plt.savefig(os.path.join(outdir, 'close_to_coast_nemo.jpg'))
# -

# ### Is On Edge

# On NEMO, the is on edge value is true when the particle is at a location when the zonal, meridional or temperature cannot be interpolated

# +

plt.figure()
ax = plt.gca()
plot_grid_layout()

color = 'green'
iout = [xmax, xmax + 0.5, xmax + 0.5, xmax, xmax]
jout = [-0.5, -0.5, ymax + 0.5, ymax + 0.5, -0.5]
print(iout)
points = [(iii - 1, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))
plt.plot(iout, jout, marker='o', markersize=5)

color = 'blue'
iout = [-0.5, 0.5, 0.5, -0.5]
jout = [-0.5, -0.5, ymax, ymax]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'firebrick'
iout = [0.5, 7,7, 0.5]
jout = [-0.5, -0.5, 0.5, 0.5]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color='orange'
iout = [0.5, 7,7, 0.5]
jout = [4, 4, 4.5, 4.5]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

plt.title('Is On Edge')
plt.savefig(os.path.join(outdir, 'is_on_edge_nemo.jpg'))
# -

# ## Vertical
#
# ### Indexing
#
# The vertical layout of the NEMO grid is as follows:
#
# <img src=https://www.nemo-ocean.eu/doc/img362.png>
#
# The `W` points are located above the `T` points.
#
# In NEMO, there is as many `W` points as `T` points. In order to be consistent with what is done with other models (ROMS for instance), i.e have one more `W` point, only $n_z - 1$ `T` points are extracted, but $n_z$ `W` points. In this case, we have:

# +
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Ellipse, Polygon
bbox=dict(boxstyle="round,pad=0.3", fc="lightgray", ec="k", lw=1)

zmax = 6
x0 = 0

cmap = plt.cm.jet

fig = plt.figure()
plt.subplots_adjust(wspace=0.3)
ax = plt.subplot(1, 2, 1)
#ax = plt.gca()
plt.title('Original NEMO layout\n')
plt.xlim(-0.5, 0.5)
plt.ylim(zmax + 0.5, -0.54)
ax.set_yticks(np.arange(-0.5, zmax + 0.5), minor=True)
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.grid(True, which='minor')
for v in np.arange(-0.5, zmax + 0.5):
    plt.axhline(v, color='k', linestyle='--', linewidth=2)
    plt.text(0, v, '%d' %(v + 0.5), bbox=bbox)

for v in np.arange(0, zmax):
    color = cmap((v) / zmax)
    print(color)
    p = ax.add_patch(Polygon([(x0 - 0.5, v -0.5), (x0 + 0.5, v-0.5), (x0 + 0.5, v + 0.5), (x0 -0.5, v + 0.5)], closed=True,
                            facecolor=color, alpha=0.5, fill=True))

p = ax.add_patch(Polygon([(x0 - 0.5, zmax -0.5), (x0 + 0.5, zmax-0.5), (x0 + 0.5, zmax + 0.5), (x0 -0.5, zmax + 0.5)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor='k'))
ax.get_xaxis().set_visible(False)  # removes xlabels
plt.xlim(-0.5, 0.5)
plt.ylim(zmax + 0.5, -0.52)

zmax -= 1
ax = plt.subplot(1, 2, 2)
plt.title('Without last cell\n')
ax.set_yticks(np.arange(-0.5, zmax + 0.5), minor=True)
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.grid(True, which='minor')
for v in np.arange(-0.5, zmax + 1 + 0.5):
    plt.axhline(v, color='k', linestyle='--', linewidth=2)
    plt.text(0, v, '%d' %(v + 0.5), bbox=bbox)

for v in np.arange(0, zmax + 1):
    color = cmap((v) / (zmax + 1))
    print((v) / (zmax + 1))
    p = ax.add_patch(Polygon([(x0 - 0.5, v -0.5), (x0 + 0.5, v-0.5), (x0 + 0.5, v + 0.5), (x0 -0.5, v + 0.5)], closed=True,
                            facecolor=color, alpha=0.5))

ax.get_xaxis().set_visible(False)  # removes xlabel
plt.xlim(-0.5, 0.5)
plt.ylim(zmax + 0.5, -0.52)
plt.savefig(os.path.join(outdir, 'vertical_indexing_nemo.jpg'))
# -

# In this case, for a `T` point at the `k` index, the corresponding `W` point is located above.
#
# ```
# T(k = 0) ----> W(k = 0) = T(k = 0 + 0.5)
# T(k = 2) ----> W(k = 2) = T(k = 2 + 0.5)
# ```
#
# However, since in NEMO the surface is located at `k=0`, the arrays are vertically flipped so that `k=0` corresponds to the last ocean cell. In this flipped layout, the `W` points **are located below**, that is:
#
# ```
# T(k = 0) ----> W(k = 0) = T(k = 0 - 0.5)
# T(k = 2) ----> W(k = 2) = T(k = 2 - 0.5)
# ```

# +
fig = plt.figure()
ax = plt.subplot(1, 2, 2)
plt.title('Ichthyop NEMO layout\n')
ax.set_yticks(np.arange(-0.5, zmax + 0.5), minor=True)
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.grid(True, which='minor')
for v in np.arange(-0.5, zmax + 1 + 0.5):
    plt.axhline(v, color='k', linestyle='--', linewidth=2)
    plt.text(0, v, '%d' %(v + 0.5), bbox=bbox)
ax.get_xaxis().set_visible(False)  # removes xlabel

for v in np.arange(0, zmax + 1):
    color = cmap(1 -  (v + 1) / (zmax + 1))
    print(1 - (v) / (zmax + 1))
    p = ax.add_patch(Polygon([(x0 - 0.5, v -0.5), (x0 + 0.5, v-0.5), (x0 + 0.5, v + 0.5), (x0 -0.5, v + 0.5)], closed=True,
                            facecolor=color, edgecolor=color, alpha=0.5))

plt.xlim(-0.5, 0.5)
plt.ylim(-0.52, zmax + 0.5)
plt.savefig(os.path.join(outdir, 'corrected_vertical_indexing_nemo.jpg'), bbox_inches='tight')
# -

# ### Scale factors.
#
# In NEMO, the vertical extent of the cells is stored in the `e3x` variable, with `x` equalts to `t`, `u` or `v` depending on the point considered. However, there are many possibilities.
#
# #### Full step
#
# In full step mode (mainly for idealized configurations), the same profile of layer is used over the entire domain:
#
# $e3t = e3u = e3v = e3t_{1d}$
#
# **This case is no more handled by Ichthyop, and users must adapth this scenario to the one below**
#
# #### Partial step
#
# In partial steps, the scale factors varies over space and depth. The deepest ocean cell indeed has a variable thickness, to better fit the local bathymetry.
#
# $e3t(k, j, i) = e3t_{1d}(k)\ if\ k < mbathy$
#
# $e3t(k, j, i) = e3t_{ps}(j,i)\ if\ k = mbathy$
#
# Generally, the 3D scale factor is provided as a `e3x_0` variable. **If only `e3x_1d` and `e3x_ps` are provided, the user will need to recontruct `e3x_0`.**
#
# #### VVL
#
# In recent NEMO configurations, scale factors vary in both time and space. They are stored as `e3x(t,k,j,k)` variables. **Not managed yet**.
#
#
# ### Interpolation
#
# #### T variable

# +
markers = ['^', 'v']
def plot_point(x, kz, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@ ', kz)
    k = np.floor(kz - 0.5)
    plt.plot(x, kz, marker='o', color=color, markersize=6)

    for kk in range(2):
        print(k + kk)
        plt.plot(0, k + kk + 0.5, marker=markers[kk], color=color, markersize=12)
        coz = 1 - abs(kz - 0.5 - (k + kk))
        print(coz)

zmax = 6  # number of z levels (T points)
plt.figure()
ax = plt.subplot(121)
plt.title('NEMO T interpolation')
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.grid(True, color='k', linewidth=2)
for v in np.arange(0, zmax):
    plt.plot(0, v + 0.5, color='k', marker='o', markersize=4)
ax.get_xaxis().set_visible(False)  # removes xlabel
plt.xlim(-0.5, 0.5)
plt.ylim(0, zmax)
plot_point(-0.3, 2.7, 'red')
plot_point(0.2, 5.3, 'blue')
plt.savefig(os.path.join(outdir, 'vertical_t_interpolation_nemo.jpg'), bbox_inches='tight')
# -

# #### Interpolation of W variable

# +
markers = ['^', 'v']
def plot_point(x, kz, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@ ', kz)
    k = np.floor(kz)
    plt.plot(x, kz, marker='o', color=color, markersize=6)

    for kk in range(2):
        print(k + kk)
        coz = 1 - abs(kz - (k + kk - 0.5))
        print(coz)
        plt.plot(0, k + kk, marker=markers[kk], color=color, markersize=12, linestyle='none')


zmax = 6
plt.figure()
ax = plt.subplot(121)
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.title('NEMO W interpolation')
plt.grid(True, color='k', linewidth=2)
for v in np.arange(0, zmax + 1):
    plt.plot(0, v, color='k', marker='o', markersize=5)
ax.get_xaxis().set_visible(False)  # removes xlabel
plt.xlim(-0.5, 0.5)
plt.ylim(-0.1, zmax + 0.1)
plot_point(-0.3, 2.7, 'red')
plot_point(0.2, 5.3, 'blue')
plt.savefig(os.path.join(outdir, 'vertical_w_interpolation_nemo.jpg'), bbox_inches='tight')
# +
def plot_point(x, kz, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@ ', kz)
    k = np.round(kz - 0.5)
    v = k + 0.5
    x0 = 0
    plt.plot(x, kz, marker='o', color=color, markersize=6)
    p = ax.add_patch(Polygon([(x0 - 0.5, v -0.5), (x0 + 0.5, v-0.5), (x0 + 0.5, v + 0.5), (x0 -0.5, v + 0.5)], closed=True,
                            facecolor=color, edgecolor=None, alpha=0.3))


#     for kk in range(2):
#         print(k + kk)
#         coz = 1 - abs(kz - (k + kk - 0.5))
#         print(coz)
#         plt.plot(0, k + kk, marker=markers[kk], color=color, markersize=12, linestyle='none')### Land sea mask

zmax = 6
plt.figure()
ax = plt.subplot(121)
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.title('NEMO Land-Sea mask')
plt.grid(True, color='k', linewidth=2)
#for v in np.arange(0, zmax + 1):
#    plt.plot(0, v, color='k', marker='o', markersize=5)
ax.get_xaxis().set_visible(False)  # removes xlabel
plt.xlim(-0.5, 0.5)
plt.ylim(0, zmax)
plot_point(-0.3, 2.7, 'red')
plot_point(0.2, 5.3, 'blue')
plt.savefig(os.path.join(outdir, 'vertical_landsea_mask_nemo.jpg'), bbox_inches='tight')
