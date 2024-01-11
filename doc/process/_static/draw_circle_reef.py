import numpy as np
import matplotlib.pyplot as plt

plt.figure(figsize = (18, 7))

#draw point at orgin
# plt.plot(0, 0, color = 'red', marker = 'o')
# plt.gca().annotate('O (0, 0)', xy=(0 - 0.1, 0 + 0.1), xycoords='data', fontsize=10)

#draw circle
r = 1.5
angles = np.linspace(0 * np.pi, 2 * np.pi, 100 )
xs = r * np.cos(angles)
ys = r * np.sin(angles)
# plt.plot(xs, ys, color = 'k')

angle_old = np.pi / 6.
rold = 1.5
xold = rold * np.cos(angle_old)
yold = rold * np.sin(angle_old)

plt.plot(xold, yold, marker='o', color='blue')
plt.gca().annotate('$P_{t - 1}$', xy=(xold + 0.1, yold), xycoords='data', fontsize=10, color='blue')

angle_new = np.pi / 5.
rnew = 1.
xnew = rnew * np.cos(angle_new)
ynew = rnew * np.sin(angle_new)

plt.plot(xnew, ynew, marker='o', color='red')
plt.gca().annotate('$P_{t}$', xy=(xnew + 0.1, ynew), xycoords='data', fontsize=10, color='red')

angle_reef = -np.pi / 2.
rreef = 0.5
xreef = rreef * np.cos(angle_reef)
yreef = rreef * np.sin(angle_reef)

plt.plot(xreef, yreef, marker='o', color='orange')
plt.gca().annotate('$R$', xy=(xreef + 0.1, yreef), xycoords='data', fontsize=10, color='orange')

plt.axvline(xnew, color='k', ls='--', lw=0.5)
plt.axhline(ynew, color='k', ls='--', lw=0.5)



# #draw daimeter
# plt.plot(0, 1.5, marker = 'o', color = 'blue')
# plt.plot(0, -1.5, marker = 'o', color = 'blue')
# plt.plot([0, 0], [1.5, -1.5])
# # plt.gca().annotate('Diameter', xy=(-0.25, -0.25), xycoords='data', fontsize=10, rotation = 90)

# #draw radius
# #plt.plot(0, 0, marker = 'o', color = 'purple')
# plt.plot(1.5, 0, marker = 'o', color = 'purple')
# plt.plot([0, 1.5], [0, 0], color = 'purple')
# # plt.gca().annotate('Radius', xy=(0.5, -0.2), xycoords='data', fontsize=10)

# #draw arc
# arc_angles = np.linspace(0 * np.pi, np.pi/4, 20)
# arc_xs = r * np.cos(arc_angles)
# arc_ys = r * np.sin(arc_angles)
# plt.plot(arc_xs, arc_ys, color = 'red', lw = 3)

# plt.gca().annotate(r'Arc = r * $\theta$', xy=(1.3, 0.4), xycoords='data', fontsize=10, rotation = 120)

# #draw another radius
# plt.plot(r * np.cos(np.pi /4), r * np.sin( np.pi / 4), marker = 'o', color = 'red')
# plt.plot([0, r * np.cos(np.pi /4)], [0, r * np.sin( np.pi / 4)], color = "purple")

# # draw theta angle and annotation
# r1 = 0.5
# arc_angles = np.linspace(0 * np.pi, np.pi/4, 20)
# arc_xs = r1 * np.cos(arc_angles)
# arc_ys = r1 * np.sin(arc_angles)
# plt.plot(arc_xs, arc_ys, color = 'green', lw = 3)
# # plt.gca().annotate(r'$\theta$', xy=(0.5, 0.2), xycoords='data', fontsize=15, rotation = 90)
# # plt.gca().annotate('<----- r = 1.5 ---->', xy=(0 - 0.2, 0 + 0.2), xycoords='data', fontsize=15, rotation = 45)

plt.xlim(-2, 2)
plt.ylim(-2, 2)
plt.gca().set_aspect('equal')
plt.show()
