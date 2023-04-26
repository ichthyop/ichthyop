Release schedule
==========================

In Ichthyop, there is the possibility to schedule release events. These events are parameterized in the ``release.schedule`` option block.

It mainly contains two parameters:

- ``is_enabled`` specifies whether release events schedule is activated or not
- ``events`` is list of release date strings, which are formatted as the beginning simulation parameter (``year YYYY month MM day DD at YY:MM``).

The resulting output file will contain the same number of time-steps as in the case of a simple release, but the number of particles will be
multiplied by the number of release events.

.. warning:: 
    
    The output time array will be consistent with the beginning simulation time (``initial_time`` parameter), **not with the release schedule dates**.