import xarray as xr
import cartopy.crs as ccrs
import cartopy.feature as cfeature
import os
from glob import glob
import matplotlib.pyplot as plt
import numpy as np
import matplotlib as mpl

config = 'reef'

if os.path.isdir('ichthyop_output'):
    pattern = os.path.join('ichthyop_output', 'reef', '*nc')
else :
    pattern = os.path.join('..', '..', 'ichthyop_output', 'reef', '*nc')
pattern

filelist = glob(pattern)
filelist

data = xr.open_mfdataset(filelist, decode_times=False)
data

# +
lon = data['lon'].values
lat = data['lat'].values
mort = data['mortality'].values

lon = np.ma.masked_where(mort > 0, lon)
lat = np.ma.masked_where(mort > 0, lat)
ntime, ndrifters = lon.shape

# +
time = np.arange(ntime)
drifters = np.arange(ndrifters)

d2d, t2d = np.meshgrid(drifters, time)

# +
plt.figure()

projin = ccrs.PlateCarree()
projout = ccrs.PlateCarree()

ax = plt.axes(projection = projout)
ax.scatter(lon[:, :], lat[:, :], c=t2d, marker='.', transform=projin, s=0.5, cmap=mpl.colormaps['jet'])
feat = ax.add_feature(cfeature.LAND)
feat = ax.add_feature(cfeature.COASTLINE)
xp = np.array([51.625, 52.625, 52.625,51.625, 51.625])
yp = np.array([-1.179878E1, -1.179878E1, -1.079878E1, -1.079878E1, -1.179878E1])
ax.plot(xp, yp, transform=projin, color='k', ls='--')
xp = np.array([51.625, 52.625 - 0.5, 52.625 - 0.5,51.625, 51.625]) - 1
yp = np.array([-1.179878E1 + 0.5, -1.179878E1 + 0.5, -1.079878E1, -1.079878E1, -1.179878E1 + 0.5]) - 0.25
ax.plot(xp, yp, transform=projin, color='k', ls='--')
# -