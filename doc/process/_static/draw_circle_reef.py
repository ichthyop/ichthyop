import numpy as np
import matplotlib.pyplot as plt

plt.rcParams['font.size'] = 15

plt.figure(figsize = (10, 10))

xnew = 0
ynew = 0

#draw point at orgin
plt.plot(xnew, ynew, color = 'red', marker = 'o')
plt.gca().annotate('$P_{t}$', xy=(0 + 0.05, 0 - 0.07), xycoords='data', color='red')

#draw circle
r = 1.5
angles = np.linspace(0 * np.pi, 2 * np.pi, 100 )
xs = r * np.cos(angles)
ys = r * np.sin(angles)
plt.plot(xs, ys, color = 'k', ls='--', lw=0.5)

angle_old = np.pi / 6.
rold = 1.
xold = rold * np.cos(angle_old)
yold = rold * np.sin(angle_old)

plt.plot(xold, yold, marker='o', color='blue')
plt.gca().annotate('$P_{t - 1}$', xy=(xold + 0.05, yold), xycoords='data', color='blue')

angle_reef = np.pi + np.pi / 3.
print(np.rad2deg(angle_reef))
rreef = 1
xreef = rreef * np.cos(angle_reef)
yreef = rreef * np.sin(angle_reef)
print(yreef)

plt.plot(xreef, yreef, marker='o', color='orange')
plt.gca().annotate('$R$', xy=(xreef + 0.1, yreef), xycoords='data', color='orange')

plt.gca().annotate(r'$D$', xy=(0.5*(xreef) + 0.02, 0.5*yreef), xycoords='data', color='orange', ha='left')

plt.axvline(xnew, color='k', ls='--', lw=0.5)
plt.axhline(ynew, color='k', ls='--', lw=0.5)

p = np.polyfit([xold, xnew], [yold, ynew], deg=1)
xtemp = np.linspace(xold, xold -2, 100)
plt.plot(xtemp, np.polyval(p, xtemp), color='black', ls='--')

angle_current = angle_old + np.pi
tmp_angle = np.linspace(0, angle_current, 100)
rtmp = 0.2
plt.plot(0 + rtmp * np.cos(tmp_angle), 0 + rtmp * np.sin(tmp_angle), color='k', ls='--')
plt.gca().annotate(r'$\theta_{actual}$', xy=(xnew + 0.2, ynew+0.05), xycoords='data', color='k')


plt.plot([xnew, xreef], [ynew, yreef], color='orange', ls='--')
angle_current = angle_reef
tmp_angle = np.linspace(0, angle_current, 100)
rtmp = 0.3
plt.plot(0 + rtmp * np.cos(tmp_angle), 0 + rtmp * np.sin(tmp_angle), color='orange', ls='--')
plt.gca().annotate(r'$\theta_{reef}$', xy=(xnew - 0.3, ynew+0.1), xycoords='data', color='orange',ha='right')

angle_current = angle_old + np.pi
tmp_angle = np.linspace(angle_current, angle_reef, 100)
rtmp = 0.4
plt.plot(0 + rtmp * np.cos(tmp_angle), 0 + rtmp * np.sin(tmp_angle), color='plum', ls='--')
plt.gca().annotate(r'$\theta_{turning}$', xy=(-0.43, -0.34), xycoords='data', color='plum')

off = 0.05
plt.xlim(xreef - off, xold + off)
plt.ylim(yreef - off, yold + off)
plt.gca().set_aspect('equal')
plt.axis('off')
plt.show()

# plt.savefig('temp.png')