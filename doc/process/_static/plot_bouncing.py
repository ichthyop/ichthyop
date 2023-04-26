# +
import os
from matplotlib.patches import Ellipse, Polygon
import matplotlib.pyplot as plt
import numpy as np
plt.rcParams['lines.linewidth'] = 3
plt.rcParams['font.size'] = 17

working_directory = os.getcwd()
working_directory
if working_directory.endswith('_static'):
    outdir = './'
else:
    outdir = os.path.join('process', '_static')


xmax = 2
ymax = 2

offx = 53
offy = 34

fig = plt.figure()
ax = plt.gca()
plt.xlim(0, xmax)
plt.ylim(0, ymax)
ax.set_xticks(np.arange(0, xmax) + 0.5)
ax.set_yticks(np.arange(0, ymax) + 0.5)
ax.set_xticks(np.arange(0, xmax + 1), minor=True)
ax.set_yticks(np.arange(0, ymax + 1), minor=True)
ax.set_xticklabels(ax.get_xticks() + offx)
ax.set_yticklabels(ax.get_yticks() + offy)
plt.grid(True, which='minor', linewidth=1)

x0 = 1
y0 = 1
p = ax.add_patch(Polygon([(x0, y0), (x0 + 1, y0), (x0 + 1, y0 + 1), (x0, y0 + 1)], closed=True,
                                 hatch='\\', facecolor='none', edgecolor='gray'))

xor = 0.7
yor = 0.5
dx = 0.8
dy = 0.9

slope = (dy / dx)
xxx = np.linspace(xor, xor + dx, 1000)
yyy = yor + slope * (xxx - xor)

iok = np.nonzero(yyy <= 1)[0]
ibis = np.nonzero(yyy > 1)[0]

ybis = 1 - slope * (xxx[ibis] - xxx[iok][-1])


plt.scatter(xor, yor, color='red', marker='o', zorder=100)
plt.scatter(xor + dx, yor + dy, color='red', marker='o', zorder=100, alpha=0.5)
plt.scatter(xxx[ibis][-1], ybis[-1], color='gold', marker='o', zorder=100, alpha=1)
plt.plot(xxx[iok], yyy[iok], color='k')
plt.plot(xxx[ibis], yyy[ibis], color='k', linestyle=':')
plt.plot(xxx[ibis], ybis, color='k')

plt.annotate('$(x, y)$', (xor, yor - 0.1), ha='center')
plt.annotate('$(x + \Delta x, y + \Delta y)$', (xor + dx, yor + 0.1 + dy), ha='center')

c1 = plt.plot([xxx[iok][-1], xxx[iok][-1]], [yor, yor + 1 - yor], linestyle=':')
plt.annotate('$\Delta y_1$', (xxx[iok][-1] + 0.1, 0.5 * (yor + yyy[iok][-1])), ha='center', va='center', color=c1[0].get_color())

c2 = plt.plot([xxx[ibis][-1], xxx[ibis][-1]], [ybis[-1], ybis[-1] + 1 - ybis[-1]], linestyle=':', color='blue')
plt.annotate('$-\Delta y_2$', (xxx[ibis][-1] + 0.15, 0.5 * (ybis[-1] + ybis[-1] + 1 - ybis[-1])), 
             ha='center', va='center', color=c2[0].get_color())

c3 = plt.plot([xxx[ibis][-1], xxx[ibis][-1]], [yor + dy, yor + dy + 1 - (yor + dy)], color='c', linestyle=':')
plt.annotate('$\Delta y_2$', (xxx[ibis][-1] + 0.15, 0.5 * (yor + dy + yor + dy + 1 - (yor + dy))), 
             ha='center', va='center', color=c3[0].get_color())

y1 = yor
y2 = ybis[-1]
c4 = plt.plot([xxx[ibis][-1], xxx[ibis][-1]], [y1, y2], color='r', linestyle=':')
plt.annotate('$\Delta y_{cor}$', (xxx[ibis][-1] + 0.15, 0.5 * (y1 + y2)), 
             ha='center', va='center', color=c4[0].get_color())


plt.savefig(os.path.join(outdir, 'bouncing.svg'), bbox_inches='tight')
plt.savefig(os.path.join(outdir, 'bouncing.pdf'), bbox_inches='tight')
# -


