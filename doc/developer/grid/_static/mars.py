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

# # Mars grid
#
# In this section, the main features of the MARS grid and the implications in Ichthyop are summarized.
#
# ## Horizontal
#
# ### Indexing
#
# The horizontal indexing of the Mars grid is shown below. It resembles the one shown in NEMO, with `T` points at the center of the grid, `U` points on the eastern face and `V` points on the northern faces. The numbers of `T`, `U` and `V` points is the same.
#
# <img src=http://www.ifremer.fr/docmars/html/_images/schema_discretisation2.jpg>

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
    plt.savefig(os.path.join(outdir, figname + '.pdf'), bbox_inches=bbox)


# +
import matplotlib.pyplot as plt
import numpy as np

xmax = 10
ymax = 8

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.floor(jy)
    i = np.floor(ix)
    for ii in range(2):
        cox = 1 - abs(ix - (i + ii))
        for jj in range(2):
            print(i + ii, j + jj)
            coy = 1 - abs(jy - (j + jj))
            print(cox, coy)
            plt.plot(i + ii, j + jj, marker='x', color=color, markersize=4, linestyle='none')

plt.figure()
ax = plt.gca()
plt.xlim(-0.5, xmax + 0.5)
plt.ylim(-0.5, ymax + 0.5)
ax.set_xticks(np.arange(-0.5, xmax + 0.5), minor=True)
ax.set_yticks(np.arange(-0.5, ymax + 0.5), minor=True)
[plt.plot([x], [y], marker='.', color='k') for x in range(xmax + 1) for y in range(ymax + 1)]
plt.grid(True, which='minor')
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
plt.title('Interpolation of T variables')
# -

# #### Interpolation of U variables
#
# Interpolation of `U` variables is done as follows:
#
# - First, the `i` index of the `U` point right of the particle is found by using `round(x)`. 
# - Then, the `j` index of the `U` grid line below the particle is found. This is done by using `floor` on the `y` value
# - The box used to average the variable is therefore defined by the `[i - 1, i]` and `[j, j + 1]` squares.

# +
xmax = 10
ymax = 8

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.floor(jy)
    i = np.round(ix)
    for ii in range(2):
        cox = 1 - abs(ix - (i - ii + 0.5))
        for jj in range(2):
            print(i - ii + 0.5, j + jj)
            coy = 1 - abs(jy - (j + jj))
            print(cox, coy)
            plt.plot(i - ii + 0.5, j + jj, marker='x', color=color, markersize=4, linestyle='none')


plt.figure()
ax = plt.gca()
plt.xlim(-0.5, xmax + 0.5)
plt.ylim(-0.5, ymax + 0.5)
ax.set_xticks(np.arange(-0.5, xmax + 0.5), minor=True)
ax.set_yticks(np.arange(-0.5, ymax + 0.5), minor=True)
[plt.plot([x + 0.5], [y], marker='.', color='k') for x in range(xmax + 1) for y in range(ymax + 1)]
plt.grid(True, which='minor')
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
plt.title('Interpolation of U variables')
# -

# #### Interpolation of V variables

# +
xmax = 10
ymax = 8

plt.rcParams['lines.markersize'] = 2

def plot_points(ix, jy, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@', ix,  jy)
    plt.plot(ix, jy, marker='o', color=color, markersize=4)
    j = np.round(jy)
    i = np.floor(ix)
    for ii in range(2):
        cox = 1 - abs(ix - (i + ii))
        for jj in range(2):
            print(i + ii, j - jj + 0.5)
            coy = 1 - abs(jy - (j - jj + 0.5))
            print(cox, coy)
            plt.plot(i + ii, j - jj + 0.5, marker='x', color=color, markersize=4, linestyle='none')

plt.figure()
ax = plt.gca()
plt.xlim(-0.5, xmax + 0.5)
plt.ylim(-0.5, ymax + 0.5)
ax.set_xticks(np.arange(-0.5, xmax + 0.5), minor=True)
ax.set_yticks(np.arange(-0.5, ymax + 0.5), minor=True)
[plt.plot([x], [y + 0.5], marker='.', color='k') for x in range(xmax + 1) for y in range(ymax + 1)]
plt.grid(True, which='minor')
l = ax.set_xticks(np.arange(xmax + 1))
l = ax.set_yticks(np.arange(ymax + 1))
plot_points(5.7, 6.1, 'r')
plot_points(2.7, 2.7, 'b')
plot_points(8.2, 6.1, 'g')
plot_points(8.3, 2.7, 'orange')
plt.title('Interpolation of V variables')
# -

# ## Vertical
#
# The vertical coordinate of the Mars model is called $\sigma$, which varies from -1 at seabed to 0 at the surface. In Mars, if the number of `T` points on the vertical is `kmax`, the number of `W` points is `kmax + 1`. For a given `T` cell located at the `k` index, the corresponding `W` point is located below. `k=0` corresponds to the bottom, while `k=kmax` corresponds to the surface.
#
# <img src=http://www.ifremer.fr/docmars/html/_images/schema_discretisation.jpg>
#
# The conversion from $\sigma$ to $z$, using generalized $\sigma$ levels, is given in [Dumas and Langlois (2009)](http://www.ifremer.fr/docmars/html/_static/2009_11_22_DocMARS_GB.pdf):
#
# $z = \xi (1 + \sigma) + H_c \times [\sigma - C(\sigma)]  + H C(\sigma)$
#
# where $\xi$ is the free surface, $H$ is the bottom depth and $H_c$ is either the minimum depth or a shallow water depth above
# which we wish to have more resolution. $C$ is defined as:
#
# $C(\sigma) = (1 - \beta) \dfrac{\sinh(\theta \sigma)}{\sinh(\theta)} + \beta \dfrac{\tanh[\theta(\sigma + \frac{1}{2})]-\tanh(\frac{\theta}{2})}{2 \tanh(\frac{\theta}{2})}$
#
# However, if the $H_c$ variable is not found, the following formulation will be used:
#
# $z = \xi (1 + \sigma) + \sigma H$
#
# Note that the $C(\sigma)$ variable is read from the input file.

# ### Interpolation
#
# #### T variable

# +
def plot_point(x, kz, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@ ', kz)
    k = np.floor(kz)
    plt.plot(x, kz, marker='o', color=color, markersize=4)
    
    for kk in range(2):
        print(k + kk)
        coz = 1 - abs(kz - (k + kk))
        print(coz)
        plt.plot(0, k + kk, marker='x', color=color, markersize=4, linestyle='none')

zmax = 10
plt.figure(figsize=(4, 10))
ax = plt.gca()
plt.title('Ichthyop MARS layout')
ax.set_yticks(np.arange(-0.5, zmax + 0.5), minor=True)
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.grid(True, which='minor')
for v in np.arange(0, zmax + 1):
    plt.plot(0, v, color='k', marker='.')
ax.get_xaxis().set_visible(False)  # removes xlabel
plt.xlim(-0.5, 0.5)
plt.ylim(-0.52, zmax + 0.5)
plot_point(-0.3, 9.7, 'red')
plot_point(0.2, 4.3, 'blue')


# -

# #### Interpolation of W variable

# +
def plot_point(x, kz, color):
    print('@@@@@@@@@@@@@@@@@@@@@@@@ ', kz)
    k = np.round(kz)
    plt.plot(x, kz, marker='o', color=color, markersize=4)
    
    for kk in range(2):
        print(k + kk - 0.5)
        coz = 1 - abs(kz - (k + kk - 0.5))
        print(coz)
        plt.plot(0, k + kk - 0.5, marker='x', color=color, markersize=4, linestyle='none')


zmax = 10
plt.figure(figsize=(4, 10))
ax = plt.gca()
plt.title('Ichthyop MARS layout')
ax.set_yticks(np.arange(-0.5, zmax + 0.5), minor=True)
ax.set_yticks(np.arange(0, zmax + 1), minor=False)
plt.grid(True, which='minor')
for v in np.arange(0, zmax + 1 + 1):
    plt.plot(0, v - 0.5, color='k', marker='.')
ax.get_xaxis().set_visible(False)  # removes xlabel
plt.xlim(-0.5, 0.5)
plt.ylim(-0.52, zmax + 0.5)
plot_point(-0.3, 9.7, 'red')
plot_point(0.2, 4.3, 'blue')
