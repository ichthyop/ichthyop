Particles
############################

Ichthyop particles (``Particle.java`` class) contains the attributes described in :numref:`table-attr-part`.

.. _table-attr-part:

.. csv-table:: Particle state variables
   :file: _static/particle.csv
   :delim: ;
   :header-rows: 1
   :class: tight-table

When using Ichthyop with additional processes, such as growth or DEB processes, additional variables need to be tracked.

This is done by firsr creating a layer class, which should extend the ``ParticleLayer.java`` class. An exemple if provided as follows:

.. code:: java
   
   package org.previmer.ichthyop.particle;

   public class LengthParticleLayer extends ParticleLayer {

      private double length;

      public LengthParticleLayer(IParticle particle) {
         super(particle);
      }

      @Override
      public void init() {
         length = 0;
      }

      public double getLength() {
         return length;
      }

      public void incrementLength(double dlength) {
         length += dlength;
      }
   }
   
This class contains additional particle attributes (here, for instance ``length``) and methods that can return and modify these attributes.

These new variables can be accessed as follows:

.. code:: java
   
   LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
   lengthLayer.setLength(length_init);
   
During the first call to the ``getLayer`` method applied to a given layer class, the latter will be instanciated, initialized and add to the
particle's ``layers`` list attribute, as shown below.

.. mermaid:: _static/mermaid/particle_layer.md
    :caption: Getting particle additional layer
    :align: center