# ---
# jupyter:
#   jupytext:
#     formats: ipynb,py:light
#     text_representation:
#       extension: .py
#       format_name: light
#       format_version: '1.5'
#       jupytext_version: 1.13.8
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
# Tracer points (`T` points) are stored at the center of the cell. Zonal velocities (`U` points) are stored on the eastern face, while meridional velocities (`V` points) are stored on the northern face. **U, V and T points have the same number of elements!**

# +
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Ellipse, Polygon
import matplotlib.ticker as ticker

xmax = 12
ymax = 8

fig = plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(0, xmax + 1), minor=True)
ax.set_yticks(np.arange(0, ymax + 1), minor=True)
plt.grid(True, which='minor', linewidth=1)
lt = [plt.plot([x + 0.5] , [y + 0.5], marker='o', color='r') for x in range(xmax) for y in range(ymax)]
lu = [plt.plot([x + 1] , [y + 0.5], marker='o', color='b') for x in range(xmax) for y in range(ymax)]
lv = [plt.plot([x + 0.5] , [y + 1], marker='o', color='g') for x in range(xmax) for y in range(ymax)]
# -

# ### Scale factors
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

xmax = 12
ymax = 8

fig = plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(0, xmax + 1), minor=True)
ax.set_yticks(np.arange(0, ymax + 1), minor=True)
plt.grid(True, which='minor', linewidth=1)

color = 'firebrick'
x0 = 0
p = ax.add_patch(Polygon([(x0, 0), (x0 + 1, 0), (x0 + 1, ymax), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color))
x0 = xmax - 2
p = ax.add_patch(Polygon([(x0, 0), (x0 + 1, 0), (x0 + 1, ymax), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color))

color = 'steelblue'
x0 = 1
p = ax.add_patch(Polygon([(x0, 0), (x0 + 1, 0), (x0 + 1, ymax ), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color))
x0 = xmax - 1
p = ax.add_patch(Polygon([(x0, 0), (x0 + 1, 0), (x0 + 1, ymax), (x0, ymax)], closed=True,
                                 hatch='\\\\', facecolor='none', edgecolor=color))
plt.title('Zonal cyclicity')
plt.savefig(os.path.join(outdir, 'zonal_cyclicity_nemo.svg'))
plt.savefig(os.path.join(outdir, 'zonal_cyclicity_nemo.pdf'))
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
xmax = 12
ymax = 8

plt.rcParams['lines.markersize'] = 2
markers = ['^', '>', '<', 'v']

def plot_points(ix, jy, color):
    cpt = 0
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='x', color=color, markersize=4)
    j = np.floor(jy - 0.5)
    i = np.floor(ix - 0.5)
    for ii in range(2):
        cox = 1 - abs(ix  - 0.5 - (i + ii))
        for jj in range(2):
            #print('x = ', i + ii + 0.5, 'y = ', j + jj + 0.5)
            coy = 1 - abs(jy - 0.5 - (j + jj))
            #print('dx = ', cox, 'dy = ', coy)
            plt.plot(i + ii + 0.5, j + jj + 0.5, marker=markers[cpt], color=color, markersize=4, linestyle='none')
            cpt += 1
            
fig = plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
l = [plt.plot([x + 0.5] , [y + 0.5], marker='o', color='k') for x in range(xmax) for y in range(ymax)]
ax.set_xticks(np.arange(0, xmax + 1))
ax.set_yticks(np.arange(0, ymax + 1))
plt.grid(True)

plot_points(2.7, 2.7, 'b')
plot_points(5.7, 6.1, 'r')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
#plot_points(11, 3, 'plum')
plt.title('Interpolation of T variables')
plt.savefig(os.path.join(outdir, 't_interpolation_nemo.svg'))
plt.savefig(os.path.join(outdir, 't_interpolation_nemo.pdf'))
# -

# #### Interpolation of U variables
#
# Interpolation of `U` variables is done as follows:
#
# - First, the `i` index of the `U` point right of the particle is found by using `round(x)`. 
# - Then, the `j` index of the `U` grid line below the particle is found. This is done by using `floor` on the `y` value
# - The box used to average the variable is therefore defined by the `[i - 1, i]` and `[j, j + 1]` squares.

# +
xmax = 12
ymax = 8

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='x', color=color, markersize=4)
    j = np.floor(jy - 0.5)
    i = np.floor(ix - 1)
    cpt = 0
    for ii in range(2):
        for jj in range(2):
            print(i - ii + 1, j + jj + 0.5)
            coy = 1 - abs(jy - 0.5 - (j + jj))
            cox = 1 - abs(ix - 1 - (i - ii))
            print(cox, coy)
            plt.plot(i + ii + 1, j + jj + 0.5, marker=markers[cpt], color=color, markersize=4, linestyle='none')
            cpt += 1
            
plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
[plt.plot([x + 1], [y + 0.5], marker='o', color='k') for x in range(xmax) for y in range(ymax)]
plt.grid(True, which='minor')
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plt.grid(True)
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
plt.title('Interpolation of U variables')
plt.savefig(os.path.join(outdir, 'u_interpolation_nemo.svg'))
plt.savefig(os.path.join(outdir, 'u_interpolation_nemo.pdf'))
# -

# #### Interpolation of V variables

# +
xmax = 12
ymax = 8

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='x', color=color, markersize=4)
    j = np.floor(jy - 1)
    i = np.floor(ix - 0.5)
    cpt = 0
    for ii in range(2):
        cox = 1 - abs(ix - 0.5 - (i + ii))
        for jj in range(2):
            print(i + ii + 0.5, j - jj + 1)
            coy = 1 - abs(jy - 1 - (j - jj + 0.5))
            print(cox, coy)
            plt.plot(i + ii + 0.5, j + jj + 1, marker=markers[cpt], color=color, markersize=4, linestyle='none')
            cpt += 1
            
plt.figure()
ax = plt.gca()
ax.set_xlim(0, xmax)
ax.set_ylim(0, ymax)
ax.set_xticks(np.arange(xmax + 1))
ax.set_yticks(np.arange(ymax + 1))
[plt.plot([x + 0.5], [y + 1], marker='o', color='k') for x in range(xmax + 1) for y in range(ymax + 1)]
plt.grid(True)
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
plt.title('Interpolation of V variables')
plt.savefig(os.path.join(outdir, 'v_interpolation_nemo.svg'))
plt.savefig(os.path.join(outdir, 'v_interpolation_nemo.pdf'))
# -

# ### Land sea-mask

# +
xmax = 12
ymax = 8

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.round(jy - 0.5)
    i = np.round(ix - 0.5)
    points = []
    
    iout = [i + 0.5 - 0.5, i + 0.5 - 0.5 + 1, i + 0.5 - 0.5 + 1, i + 0.5 - 0.5]
    jout = [j + 0.5 - 0.5, j + 0.5 - 0.5, j + 0.5 - 0.5 + 1, j + 0.5 - 0.5 + 1]
    points = [(ii, jj) for ii, jj in zip(iout, jout)]
    p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))
    plt.plot(i + 0.5, j + 0.5, marker='o', color=color)
            
plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(0, xmax + 1))
ax.set_yticks(np.arange(0, ymax + 1))
[plt.plot([x + 0.5], [y + 0.5], marker='o', color='k') for x in range(xmax + 1) for y in range(ymax + 1)]
plt.grid(True)
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
plt.title('Land-sea mask')
plt.savefig(os.path.join(outdir, 'landsea_mask_nemo.svg'))
plt.savefig(os.path.join(outdir, 'landsea_mask_nemo.pdf'))
# -

# ### Close to coast

# +
xmax = 12
ymax = 8

plt.rcParams['lines.markersize'] = 2

def plot_poly(i, j, color):
        
    iout = [i + 0.5 - 0.5, i + 0.5 - 0.5 + 1, i + 0.5 - 0.5 + 1, i + 0.5 - 0.5]
    jout = [j + 0.5 - 0.5, j + 0.5 - 0.5, j + 0.5 - 0.5 + 1, j + 0.5 - 0.5 + 1]
    points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
    p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))
    plt.plot(i + 0.5, j + 0.5, marker='o', color=color)

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.round(jy - 0.5)
    i = np.round(ix - 0.5)
    points = []
    
    if(j == np.floor(jy - 0.5)):
        jj = 1
    else:
        jj = -1
    
    if(i == np.floor(ix - 0.5)):
        ii = 1
    else:
        ii = -1
    plot_poly(i + ii, j + jj, color)
    plot_poly(i, j + jj, color)
    plot_poly(i + ii, j, color)
            
plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(0, xmax + 1))
ax.set_yticks(np.arange(0, ymax + 1))
[plt.plot([x + 0.5], [y + 0.5], marker='o', color='k') for x in range(xmax + 1) for y in range(ymax + 1)]
plt.grid(True)
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
plt.title('Close to Coast')
plt.savefig(os.path.join(outdir, 'close_to_coast_nemo.svg'))
plt.savefig(os.path.join(outdir, 'close_to_coast_nemo.pdf'))
# -

# ### Is On Edge

# +
xmax = 12
ymax = 8

plt.rcParams['lines.markersize'] = 2
            
plt.figure()
ax = plt.gca()
ax.set_xticks(np.arange(0, xmax))
ax.set_yticks(np.arange(0, ymax))
#[plt.plot([x], [y], marker='.', color='k') for x in range(xmax + 1) for y in range(ymax + 1)]
plt.grid(True)
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plt.xlim(0, xmax)
plt.ylim(0, ymax)

color = 'steelblue'
#iout = [xmax - 0.5 + 1, xmax + 0.5 + 1, xmax + 0.5+ 1, xmax  - 0.5+ 1]
iout = [xmax - 0.5, xmax + 1 - 0.5, xmax + 1 - 0.5, xmax - 0.5]
jout = [0, 0, ymax, ymax]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

iout = [0, 1, 1, 0]
jout = [0, 0, ymax, ymax]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'firebrick'
iout = [0, xmax, xmax, 0]
jout = [0, 0, 1, 1]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

iout = [0, xmax, xmax, 0]
jout = [ymax - 0.5, ymax - 0.5, ymax - 0.5 +0.5, ymax - 0.5 + 0.5]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

plt.title('Is On Edge')
plt.savefig(os.path.join(outdir, 'is_on_edge_nemo.svg'))
plt.savefig(os.path.join(outdir, 'is_on_edge_nemo.pdf'))
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
plt.savefig(os.path.join(outdir, 'vertical_indexing_nemo.svg'))
plt.savefig(os.path.join(outdir, 'vertical_indexing_nemo.pdf'))
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
plt.savefig(os.path.join(outdir, 'corrected_vertical_indexing_nemo.svg'), bbox_inches='tight')
plt.savefig(os.path.join(outdir, 'corrected_vertical_indexing_nemo.pdf'), bbox_inches='tight')
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
plt.savefig(os.path.join(outdir, 'vertical_t_interpolation_nemo.svg'), bbox_inches='tight')
plt.savefig(os.path.join(outdir, 'vertical_t_interpolation_nemo.pdf'), bbox_inches='tight')
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
plt.savefig(os.path.join(outdir, 'vertical_w_interpolation_nemo.svg'), bbox_inches='tight')
plt.savefig(os.path.join(outdir, 'vertical_w_interpolation_nemo.pdf'), bbox_inches='tight')
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
plt.savefig(os.path.join(outdir, 'vertical_landsea_mask_nemo.svg'), bbox_inches='tight')
plt.savefig(os.path.join(outdir, 'vertical_landsea_mask_nemo.pdf'), bbox_inches='tight')
# -
