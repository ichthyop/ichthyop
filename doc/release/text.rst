Text release
############################

If the user wants to simulate some specific trajectories, for instance DCPs or buoys, particles can be relased
by providing a text file containing the release coordinates (`TxtFileRelease.java`). The name of the text file is
given by the `txtfile` parameter.

The file must be formatted as follows:

.. code:: 
    
    # 3D simulations
    # longitude latitude depth
    -5.45 48.30 -5
    -5.45 48.30 -10
    -5.45 48.30 -15
    -5.45 48.30 -20

Each line is a drifter. Each character starting with `#` is considered as a comment. 
If the depth column is not provided, depth will be set equal to 0.

For 2D simulations, the depth column will be ignored.

.. danger::
    
    Note that the columns must be separated by spaces.
