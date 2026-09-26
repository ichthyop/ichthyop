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

# # ROMS grid
#
# ## Horizontal
#
# The horizontal grid layout of the ROMS model is shown below:
#
# <img src=https://www.myroms.org/wiki/images/0/0f/staggered_grid_rho_cells.png>
#
# In the ROMS output files, T points have dimensions of $( N_{Y_T}, N_{X_T})$. U points have dimensions of $(N_{Y_T}, N_{X_U})$, with $N_{X_U} = N_{X_T} - 1$. 
# V points have dimensions of $(N_{Y_V}, N_{X_T})$, with $N_{Y_V} = N_{Y_T} - 1$. In Ichthyop, the tracer and velocity fields will be extracted on the **inner** domain (the gray area in the above). As a consequence, the data extraction will be as follows:
#
# - For T points: `start = [1, 1]`, `count = [ny - 2, nx -2]`, i.e. we remove the T points outside of the inner domain
# - For U points: `start = [1, 0]`, `count = [ny - 2, nx -1]`, i.e. we remove the U points outside of the inner domain (northernmost and southernmost cells), while keeping velocity at the westernmost and easternmost edges of the inner domain.
# - For V points: `start = [1, 1]`, `count = [ny - 2, nx -2]`, i.e. we remove the V points outside of the inner domain (northernmost and southernmost cells), while keeping velocity at the northernmost and southernmost edges of the inner domain.
#
# Consequently, at the end of this data extraction, from an Ichthyop perspective, there will be an extra U point on the X dimension, and one extra V point on the Y dimension. The final layout will therefore be as follows.
#
# <!-- Contrary to NEMO and MARS, the `U` points are located on the *western* face, while velocities are located on the *southern* faces. However, while the `T` interior domain has $(N_y, N_x)$ dimensions, the `U` domain is $(N_y, N_x - 1)$ while the `V` domain is $(N_y - 1, N_x)$. Indeed, the first elements are discarded. The grid as saved by ROMS is as follows: -->

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

# Plotting U points
bbox['fc'] = 'firebrick'
bbox['alpha'] = 1
for x in np.arange(xmax + 1):
    for y in range(ymax):
        #lu = plt.plot(x - 0.5, y, marker='.', color='r')
        lu = plt.text(x - 0.5, y, '%d,%d' %(x,y), bbox=bbox, ha='center', va='center', color='w', zorder=100)

# Plotting V points
bbox['fc'] = 'powderblue'
for x in np.arange(0, xmax):
    for y in range(ymax + 1):
        #lv = plt.plot(x, y - 0.5, marker='.', color='b')
        lv = plt.text(x, y - 0.5, '%d,%d' %(x,y), bbox=bbox, ha='center', va='center', color='k')

plt.plot([0, 7, 7, 0, 0], [0, 0, 4, 4, 0], lw=6)

#l = plt.legend([lt, lu, lv], ['T', 'U', 'V'], ncol=1, loc='right', bbox_to_anchor=(1.2, 0.6))
savefig('ichthyop_grid_roms')


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
# #### T points

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
plt.title('Interpolation of T variables')
savefig('interpolation_t_roms')
# -

# ### Interpolation of U files

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=6)
    j = np.floor(jy)

    # i is the index of the u points to select
    # from U points of view, x is shifted by 0.5
    # we therefore move ix to the U referential 
    # therefore, i is the index from the U point of vue.
    # i = 0 is the outer left U Point
    i = np.floor(ix + 0.5)  
    cpt = 0
    for ii in range(2):
        # For distance computation, we need to move the i index back to 
        # the T grid reference in which ix is defined. Hence we shift by 0.5
        cox = 1 - abs((ix - (i - 0.5 + ii)))
        for jj in range(2):
            coy = 1 - abs((jy - (j + jj)))
            print('-----')
            print(f'ix={ix}, i={i+ii - 0.5}, cox={cox:.2f}')
            print(f'jy={jy}, j={j+jj}, coy={coy:.2f}')
            print(f'ui={i + ii}, uj={j + jj}')
            plt.plot(i + ii - 0.5, j + jj , marker=markers[cpt], color=color, markersize=6, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plot_grid_layout()
[plt.plot([x - 0.5], [y], marker='o', color='k', markersize=4) for x in range(xmax + 1 ) for y in range(ymax)]
plt.grid(True, which='minor')
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 3.1, 'g')
plot_points(0.2, 3.1, 'g')

plt.title('Interpolation of U variables')
savefig('interpolation_u_roms')
# -

# ### Interpolation of V files

# +
plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)

    # from a V perspective, we need to move
    # jy to the V grid layout by shifting by 0.5
    j = np.floor(jy + 0.5)  # (j, i) is the grid cell index of V point to select (low left)
    i = np.floor(ix)
    cpt = 0
    for ii in range(2):
        cox = 1 - abs(ix - (i + ii))
        for jj in range(2):
            # For distance computation, we need to move the j index back to 
            # the T grid reference in which jy is defined. Hence we shift by 0.5
            print('-----')
            coy = 1 - abs(jy - (j - 0.5 + jj))
            print(f'ix={ix}, i={i+ii}, cox={cox:.2f}')
            print(f'jy={jy}, j={j+jj}, coy={coy:.2f}')
            print(f'vi={i + ii}, vj={j + jj}')
            plt.plot(i + ii, j + jj - 0.5, marker=markers[cpt], color=color, markersize=6, linestyle='none')
            cpt += 1

plt.figure()
ax = plt.gca()
plot_grid_layout()
[plt.plot(x, [y - 0.5], marker='o', color='k', markersize=4) for x in range(xmax) for y in range(ymax + 1)]
plot_points(5.7, 3.1, 'r')
plot_points(2.7, 1.7, 'b')
plot_points(1.2, 1.1, 'g')
plot_points(1.2, 0.1, 'c')
plt.title('Interpolation of V variables')
savefig('interpolation_v_roms')
# -

# ### Is on edge

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
savefig('is_on_edge_roms', bbox='tight')

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
