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

# +
import cartopy.crs as ccrs
import cartopy.feature as cfeature
import matplotlib.pyplot as plt
import numpy as np
import matplotlib as mp
import os
import cartopy.io.shapereader as shapereader
import shapely.geometry as sgeom
from shapely.ops import unary_union
from shapely.prepared import prep

np.random.seed(0)

dirout = os.path.join('./')
dirout = os.path.join('release', '_static')

plt.rcParams['font.size'] = 15
figparams = {'facecolor': 'white', 'figsize': (12, 8)}
projin = ccrs.PlateCarree()

# +
lonzone1 = np.array([5, 6, 7])
latzone1 = np.array([41, 37, 41])

col1 = 'steelblue'

lonmin, lonmax = 11, 13
latmin, latmax = 39, 43
lonzone2 = np.array([lonmin, lonmax, lonmax, lonmin, lonmin])
latzone2 = np.array([latmin, latmin, latmax, latmax, latmin])
col2 = 'firebrick'

# +
MAP_RES  = '110m'
MAP_TYPE = 'physical'
MAP_NAME = 'land'

land_shp_fname = shapereader.natural_earth(resolution=MAP_RES, category=MAP_TYPE, name=MAP_NAME)
land_geom = unary_union(list(shapereader.Reader(land_shp_fname).geometries()))
land = prep(land_geom)

def is_over_land(x, y):
    return land.contains(sgeom.Point(x, y))


# -

def savefig(figname):
    
    plt.savefig(os.path.join(dirout, figname + '.svg'), bbox_inches='tight')
    plt.savefig(os.path.join(dirout, figname + '.pdf'), bbox_inches='tight')


def inpolygon(x, y, x_pol, y_pol):

    from matplotlib import path

    x_pol = np.array(x_pol)
    y_pol = np.array(y_pol)

    # If the polynom is not closed, we close it
    if (x_pol[0] != x_pol[-1]) | (y_pol[0] != y_pol[-1]):
        x_pol = np.append(x_pol, x_pol[0])
        y_pol = np.append(y_pol, y_pol[0])

    # creation the input of the path.Path command:
    # [(x1, y1), (x2, y2), (x3, y3)]
    path_input = [(xtemp, ytemp) for xtemp, ytemp in zip(x_pol, y_pol)]

    # initialisation of the path object
    temppath = path.Path(path_input)

    # creation of the list of all the points within the domain
    # it must have a N x 2 shape
    point = [x, y]

    # Calculation of the mask (True if within the polygon)
    mask = temppath.contains_point(point)

    return mask


def plot_bkg():
    lonmin, lonmax = -2, 18
    latmin, latmax = 35, 45
    ax = plt.axes(projection=projin)
    ax.add_feature(cfeature.LAND)
    ax.add_feature(cfeature.COASTLINE)
    ax.set_extent([lonmin, lonmax, latmin, latmax])


def _plot_single_zone(lonzone, latzone, color='gray', hatch='/'):
    xy = np.transpose(np.array([lonzone, latzone])) # 7 x 2
    p = mp.patches.Polygon(xy, closed=True, 
               fill=False, hatch=hatch, color=color, linewidth=2)
    plt.gca().add_artist(p)


def zone_release_particles(N, xzone, yzone):
    xmin, xmax = np.min(xzone), np.max(xzone)
    ymin, ymax = np.min(yzone), np.max(yzone)
    cpt = 0
    xout = np.zeros([N])
    yout = np.zeros([N])
    niter = 0
    while(cpt != N):
        xp = np.random.uniform(xmin, xmax)
        yp = np.random.uniform(ymin, ymax)
            
        if inpolygon(xp, yp, xzone, yzone) & ~is_over_land(xp, yp):
            xout[cpt] = xp
            yout[cpt] = yp
            cpt += 1
        if(niter == 30000):
            print('Maximum iteration reached')
            return
    return xout, yout


# +
def geodesicDistance(lat1, lon1, lat2, lon2) :

    lat1_rad = np.pi * lat1 / 180
    lat2_rad = np.pi * lat2 / 180
    lon1_rad = np.pi * lon1 / 180
    lon2_rad = np.pi * lon2 / 180

    d = 2 * 6367000 * np.arcsin(np.sqrt(np.power(np.sin((lat2_rad - lat1_rad) / 2), 2) + np.cos(lat1_rad) * np.cos(lat2_rad) * np.power(np.sin((lon2_rad - lon1_rad) / 2), 2)))

    return d


def stain_release_particles(N, xp, yp, radius):
    
    xout = np.zeros([N])
    yout = np.zeros([N])

    cpt = 0 
    niter = 0
    ONE_DEG_LATITUDE_IN_METER = 111138.
    
    while(cpt != N):
        
        lat = yp + 2 * radius * (np.random.uniform() - 0.5) / ONE_DEG_LATITUDE_IN_METER
        one_deg_longitude_meter = ONE_DEG_LATITUDE_IN_METER * np.cos(np.pi * yp / 180)
        lon = xp + 2 * radius * (np.random.uniform() - 0.5) / one_deg_longitude_meter
        if (geodesicDistance(lat, lon, yp, xp) <= radius) & (~is_over_land(lon, lat)):
            xout[cpt] = lon
            yout[cpt] = lat
            cpt += 1

        if niter == 6000:
            print("Max iter reached")
            return
    
        niter += 1
    
    return xout, yout    


# -

def plot_zones():
    _plot_single_zone(lonzone1, latzone1, color=col1)
    _plot_single_zone(lonzone2, latzone2, color=col2)


# ## Plotting release stain

# +
plt.figure(**figparams)
plot_bkg()

xp, yp = stain_release_particles(300, 12, 41, 100000)
# xp, yp = stain_release_particles(50, 2, 43, 100000)
plt.plot(xp, yp, color=col2, marker='.', linestyle='none')

savefig('release_stain')
# -

# ## Plotting release zones

# +
plt.figure(**figparams)
plot_bkg()
plot_zones()

xp, yp = zone_release_particles(300, lonzone1, latzone1)
plt.plot(xp, yp, color=col1, marker='.', linestyle='none')

xp, yp = zone_release_particles(300, lonzone2, latzone2)
plt.plot(xp, yp, color=col2, marker='.', linestyle='none')
plt.title('Zone release')

savefig('release_zones')
# -

# ## Plotting Patchy zone release

# +
plt.figure(**figparams)
plot_bkg()
plot_zones()

xxxp, yyyp = zone_release_particles(10, lonzone1, latzone1)
for xp, yp in zip(xxxp, yyyp):
    lonp, latp = stain_release_particles(50, xp, yp, 10000)
    plt.plot(lonp, latp, marker='.', linestyle='none', color=col1)
    
xxxp, yyyp = zone_release_particles(10, lonzone2, latzone2)
for xp, yp in zip(xxxp, yyyp):
    lonp, latp = stain_release_particles(50, xp, yp, 10000)
    plt.plot(lonp, latp, marker='.', linestyle='none', color=col2)

plt.title('Patchy zone release')
    
savefig('release_patchy_zones')

# +
plt.figure(**figparams)
plot_bkg()
plot_zones()

xmin = np.min([lonzone1.min(), lonzone2.min()])
xmax = np.max([lonzone1.max(), lonzone2.max()])
ymin = np.min([latzone1.min(), latzone2.min()])
ymax = np.max([latzone1.max(), latzone2.max()])
xzone = [xmin, xmax, xmax, xmin, xmin]
yzone = [ymin, ymin, ymax, ymax, ymin]

_plot_single_zone(xzone, yzone, hatch='\\')

xxxp, yyyp = zone_release_particles(20, xzone, yzone)
plt.plot(xxxp, yyyp, marker='.', linestyle='none', color='k')
for xp, yp in zip(xxxp, yyyp):
    lonp, latp = stain_release_particles(50, xp, yp, 10000)
    plt.plot(lonp, latp, marker='.', linestyle='none', color='gray')

plt.title('Patchy uniform release')
    
savefig('release_patchy_uniform')

# +
plt.figure(**figparams)
plot_bkg()

xmin, xmax = -2, 18
ymin, ymax = 35, 44

xzone = [xmin, xmax, xmax, xmin, xmin]
yzone = [ymin, ymin, ymax, ymax, ymin]

xxxp, yyyp = zone_release_particles(1000, xzone, yzone)
plt.plot(xxxp, yyyp, marker='.', linestyle='none', color='gray')
    
plt.title('Netcdf release')
    
savefig('release_netcdf')
# -


