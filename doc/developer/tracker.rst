.. _new_tracker:

Adding output variable
============================================

When including new processes to Ichthyop (cf. :numref:`new_action`), the storage of additional variable may be required. 
For instance, in the growth processes  (see :numref:``), in which particle length is a state variable, it is necessary to
save it in the output NetCDF file.

Adding new variables can be achieved by creating new tracker class in the ``org.previmer.ichthyop.io`` package.