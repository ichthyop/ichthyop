import matplotlib.pyplot as plt
import numpy as np
from scipy.special import i0

x = np.linspace(-np.pi, np.pi, 200)

def von_misses(kappa):
    mu = 0
    y = np.exp(kappa*np.cos(x-mu))/(2*np.pi*i0(kappa))
    return y

plt.figure()

for i in [0.5, 1, 2, 5, 10, 20]:
    plt.plot(x, von_misses(i), label=f'$\kappa = {i}$')
plt.legend()
plt.xlim(x.min(), x.max())
plt.show()
