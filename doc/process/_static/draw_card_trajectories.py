import xarray as xr
import cartopy.crs as ccrs
import cartopy.feature as cfeature
import os
from glob import glob
import matplotlib.pyplot as plt
import numpy as np
import matplotlib as mpl

config = 'card'

if os.path.isdir('ichthyop_output'):
    pattern = os.path.join('ichthyop_output', config, '*nc')
else :
    pattern = os.path.join('..', '..', 'ichthyop_output', config, '*nc')
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
ax.set_extent([49, 55.25, -13.13, -9.45], crs=projin)
