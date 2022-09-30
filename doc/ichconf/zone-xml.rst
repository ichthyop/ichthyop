.. _zone-xml_config:

Zone configuration file
####################################

Zone configuration files are also managed via a dedicated XML file. The file must be as follows:


.. code-block:: HTML

    <?xml version="1.0" encoding="UTF-8"?>
    <zones>

    </zones>
    
Each zone is defined on a ``zone`` tag, which contain the following tags:

- ``key`` is the name of the zone
- ``enabled`` specifies whether this zone must be considered or not.
- ``type`` specifies whether the zone should be used for release (see :numref:``, ``release`` value) or recruitment processes (``recruitment`` value)
- ``polygon`` specifies the different points used to define the area
- ``bathy_mask`` specifies the bathymetric zone (for instance 0 to 200m, i.e. continental shelf) where the zone is defined.
- ``thickness`` specifies the upper and lower depths where this zone is defined (**only valid for 3D runs**).
- ``color`` specifies the display color of the zone (format is ``[r=102,g=51,b=255]``).
- ``proportion_particles`` specifies the proportion (values in :math:`[0-1]`) of particles to be released in the area. Only used when ``type`` is ``release`` and if the ``user_defined_nparticles`` parameter is set to True (cf. :numref:`zone_release`)


An example of a zone definition is provided below.

.. code-block:: HTML
    
    <zone>
        <key>Release zone 2</key>
        <enabled>true</enabled>
        <type>release</type>
        <polygon>
            <point>
                <index>0</index>
                <lon>54.0</lon>
                <lat>-11.5</lat>
            </point>
            <point>
                <index>1</index>
                <lon>54.0</lon>
                <lat>-12.5</lat>
            </point>
            <point>
                <index>2</index>
                <lon>53.0</lon>
                <lat>-12.5</lat>
            </point>
            <point>
                <index>3</index>
                <lon>53.0</lon>
                <lat>-11.5</lat>
            </point>
        </polygon>
        <bathy_mask>
            <enabled>true</enabled>
            <line_inshore>0.0</line_inshore>
            <line_offshore>12000.0</line_offshore>
        </bathy_mask>
        <thickness>
            <enabled>true</enabled>
            <upper_depth>0.0</upper_depth>
            <lower_depth>50.0</lower_depth>
            </thickness>
        <color>[r=102,g=51,b=255]</color>
        <proportion_particles>0.2</proportion_particles>
    </zone>
