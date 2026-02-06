import matplotlib.pyplot as plt
import numpy as np
from scipy.special import i0

PLD = 10
x = np.linspace(0, PLD, 1000)[1:]

vhatch = 10
vsettle = 200

vel = vhatch + np.power(vsettle - vhatch, np.log10(x)/np.log10(PLD))

plt.figure()
plt.plot(x, vel)
plt.xlabel('Age (days)')
plt.ylabel('$V (cm.s^{-1})$')
plt.xlim(x.min(), x.max())
plt.title('$PLD = %dd, V_{hatch}=%d cm.s^{-1}, V_{settle}=%d cm.s^{-1}$' %(PLD, vhatch, vsettle))
plt.show()