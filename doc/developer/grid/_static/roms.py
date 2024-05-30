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

# # ROMS grid
#
# ## Horizontal
#
# The horizontal grid layout of the ROMS model is shown below:
#
# <img src=https://www.myroms.org/wiki/images/0/0f/staggered_grid_rho_cells.png>
#
# Contrary to NEMO and MARS, the `U` points are located on the *western* face, while velocities are located on the *southern* faces. However, while the `T` interior domain has $(N_y, N_x)$ dimensions, the `U` domain is $(N_y, N_x - 1)$ while the `V` domain is $(N_y - 1, N_x)$. Indeed, the first elements are discarded. The grid as saved by ROMS is as follows:

# +
import os
working_directory = os.getcwd()
working_directory
if working_directory.endswith('_static'):
    outdir = './'
else:
    outdir = os.path.join('developer', 'grid', '_static')
print("++++++++++++++++++++++++++++++++++++++++++ ", outdir)

def savefig(figname, bbox=None):
    plt.savefig(os.path.join(outdir, figname + '.svg'), bbox_inches=bbox)
    plt.savefig(os.path.join(outdir, figname + '.pdf'), bbox_inches=bbox)


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
ax.set_xticks(np.arange(0, xmax + 1), minor=False)
ax.set_yticks(np.arange(0, ymax + 1), minor=False)
plt.grid(True, linewidth=2, linestyle='--', color='k')
bbox['fc'] = 'orange'
for x in range(0, xmax):
    for y in range(0, ymax):
        #lt = plt.plot(x + 0.5, y + 0.5, marker='.', color='g')
        lt = plt.text(x + 0.5, y + 0.5, '%d,%d' %(x, y), bbox=bbox, ha='center', va='center', color='k', zorder=100)

bbox['fc'] = 'firebrick'
bbox['alpha'] = 1
for x in np.arange(xmax - 1):
    for y in range(ymax):
        #lu = plt.plot(x - 0.5, y, marker='.', color='r')
        lu = plt.text(x + 1, y + 0.5, '%d,%d' %(x,y), bbox=bbox, ha='center', va='center', color='w', zorder=100)

bbox['fc'] = 'powderblue'
for x in np.arange(0, xmax):
    for y in range(ymax - 1):
        #lv = plt.plot(x, y - 0.5, marker='.', color='b')
        lv = plt.text(x + 0.5, y + 1, '%d,%d' %(x,y), bbopenox=bbox, ha='center', va='center', color='k')

#l = plt.legend([lt, lu, lv], ['T', 'U', 'V'], ncol=1, loc='right', bbox_to_anchor=(1.2, 0.6))
savefig('ichthyop_grid_roms')
# -
# However, since Ichthyop reads the `U` and `V` files starting from index `0`, every looks as follows:

# Consequently, everything is as such the `U` and `V` points are on the northern and eastern faces. Except that the easternmost `T` point has no `U` point on the right, while the northernmost `T` point has no `V` point.

# ### Interpolation
#
# #### T points

# +
import matplotlib.pyplot as plt
import numpy as np
markers = ['^', '>', '<', 'v']

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=6)
    j = np.floor(jy - 0.5)
    i = np.floor(ix - 0.5)
    cpt = 0
    for ii in range(2):
        cox = 1 - abs(ix - 0.5 - (i + ii))
        for jj in range(2):
            print(i + ii + 0.5, j + jj + 0.5)
            coy = 1 - abs(jy - 0.5 - (j + jj))
            print(cox, coy)
            plt.plot(i + ii + 0.5, j + jj + 0.5, marker=markers[cpt], color=color, markersize=6, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(xmax + 1), minor=False)
ax.set_yticks(np.arange(ymax + 1), minor=False)
plt.grid(True, linewidth=2, color='gray')
[plt.plot([x + 0.5], [y + 0.5], marker='o', color='k', markersize=4) for x in range(xmax) for y in range(ymax)]
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 4.1, 'g')
plt.title('Interpolation of T variables')
savefig('interpolation_t_roms')
# -

# ### Interpolation of U files

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=6)
    j = np.floor(jy - 0.5)
    i = np.floor(ix - 1)
    cpt = 0
    for ii in range(2):
        cox = 1 - abs(ix - 1 - (i - ii))
        for jj in range(2):
            print(i + ii + 1, j + jj + 0.5)
            coy = 1 - abs(jy - 0.5 - (j + jj))
            print(cox, coy)
            plt.plot(i + ii + 1, j + jj + 0.5, marker=markers[cpt], color=color, markersize=6, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(xmax + 1), minor=False)
ax.set_yticks(np.arange(ymax + 1), minor=False)
plt.grid(True, linewidth=2, color='gray')
[plt.plot([x + 1], [y + 0.5], marker='o', color='k', markersize=4) for x in range(xmax - 1) for y in range(ymax)]
plt.grid(True, which='minor')
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 4.1, 'g')
plt.title('Interpolation of U variables')
savefig('interpolation_u_roms')
# -

# ### Interpolation of V files

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.floor(jy - 1)
    i = np.floor(ix - 0.5)
    cpt = 0
    for ii in range(2):
        cox = 1 - abs(ix - 0.5 - (i + ii))
        for jj in range(2):
            print(i + ii + 0.5, j + jj + 1)
            coy = 1 - abs(jy - 1 - (j + jj))
            print(cox, coy)
            plt.plot(i + ii + 0.5, j + jj + 1, marker=markers[cpt], color=color, markersize=6, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(xmax + 1), minor=False)
ax.set_yticks(np.arange(ymax + 1), minor=False)
plt.grid(True, linewidth=2, color='gray')
[plt.plot([x + 0.5], [y + 1], marker='o', color='k', markersize=6) for x in range(xmax) for y in range(ymax - 1)]
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 1.1, 'g')
plt.title('Interpolation of V variables')
savefig('interpolation_v_roms')
# -

# ### Is on edge

# +
plt.rcParams['lines.markersize'] = 2

plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(xmax + 1), minor=False)
ax.set_yticks(np.arange(ymax + 1), minor=False)
plt.grid(True, linewidth=2, color='gray')

color = 'firebrick'
iout = [0, xmax, xmax, 0]
jout = [0, 0, 1, 1]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'firebrick'
iout = [0, xmax, xmax, 0]
jout = [ymax - 1, ymax - 1, ymax, ymax]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'firebrick'
jout = [1, 1, ymax - 1, ymax - 1]
iout = [xmax - 1, xmax, xmax, xmax - 1]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

color = 'firebrick'
jout = [1, 1, ymax - 1, ymax - 1]
iout = [0, 1, 1, 0]
points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

plt.title('Is On Edge')
savefig('is_on_edge_roms', bbox='tight')
# -

# ### Land-sea mask

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(x, y, color):

    print('+++++++++++++ ', x, y)
    plt.plot(x, y, color=color, marker='o', markersize=6)
    i = np.round(x - 0.5)
    j = np.round(y - 0.5)
    print(i, j)

    i -= 0.5
    j -= 0.5

    jout = [j + 0.5, j + 0.5, j + 1.5, j + 1.5]
    iout = [i + 0.5, i + 1.5, i + 1.5, i + 0.5]
    points = [(iii, jjj) for iii, jjj in zip(iout, jout)]
    p = ax.add_patch(Polygon(points, closed=True, facecolor=color, edgecolor=color, alpha = 0.1))

plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(xmax + 1), minor=False)
ax.set_yticks(np.arange(ymax + 1), minor=False)
plt.grid(True, linewidth=2, color='gray')
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 4.1, 'g')
plt.title('Land-sea mask')
savefig('land_sea_mask_roms', bbox='tight')


# -

# ### Is close to coast

# +
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
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 4.1, 'g')
plt.title('Close to Coast')
#savefig('close_to_coast_roms')
# -

# ## Vertical
#
# ### Indexing
#
# The vertical indexing of the ROMS model is shown below:
#
# <img src=https://www.myroms.org/wiki/images/4/41/vertical_grid.png>
#
# The ocean bottom is located at `k=0`. `W` points are located above the `T` points. However, since there is one more `W` point that `T` points, everything is as such the `W` points are located below the `T` points.
#
# ### Transform
#
# The vertical coordinate system of ROMS is discussed on [WikiRoms](https://www.myroms.org/wiki/Vertical_S-coordinate)
#
# The vertical coordinate in ROMS is $\sigma$, which varies between $-1$ (ocean bottom) and 0 (ocean surface). There are two possibilities to move from $\sigma$ to $z$/
#
# The first transform available is available in ROMS since 1999 and is given by:
#
# $z(x,y,\sigma,t) = S(x,y,\sigma) + \zeta(x,y,t) \left[1 + \dfrac{S(x,y,\sigma)}{h(x,y)}\right]$
#
# with
#
# $S(x,y,\sigma) = h_c \, \sigma + \left[h(x,y) - h_c\right] \, C(\sigma)$

# The second transform, called UCLA-ROMS, is given by:
#
# $z(x,y,\sigma,t) = \zeta(x,y,t) + \left[\zeta(x,y,t) + h(x,y)\right] \, S(x,y,\sigma)$
#
# with
#
# $S(x,y,\sigma) = \dfrac{h_c \, \sigma + h(x,y)\, C(\sigma)}{h_c + h(x,y)}$

# It can be rewritten in the same form as the original one.
#
# $z(x,y,\sigma,t) = h(x,y) S(x,y,\sigma) + \zeta(x,y,t) + \zeta(x,y,t) S(x,y,\sigma)$
#
# $z(x,y,\sigma,t) = h(x,y) S(x,y,\sigma) + \zeta(x,y,t) \left[1 + S(x,y,\sigma)\right]$
#
# $z(x,y,\sigma,t) = h(x,y) S(x,y,\sigma) + \zeta(x,y,t) \left[1 + \dfrac{h(x, y)S(x,y,\sigma)}{h(x, y)}\right]$

# In this form, both formulations can be expressed as:
#
# $z(x,y,\sigma,t) = H_0(x, y, \sigma) + \zeta(x,y,t) \left[1 + \dfrac{H_0(x, y, \sigma)}{h(x, y)}\right]$
#
# with $H_0$ which is constant overt time, and which varies between the classical and the UCLA formulations. For the classical formulation:
#
# $H_0(x, y, \sigma) = S(x, y, \sigma)$
#
# For the UCLa formulation:
#
# $H_0(x, y, \sigma) = h(x, y) S(x, y, \sigma) $
