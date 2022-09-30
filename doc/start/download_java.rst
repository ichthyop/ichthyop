Prerequisites
---------------------------------

Java
############

In order to run Ichthyop, **Java (>= 11)**  needs to be installed. Beforehand, let us clarify some of the acronyms regarding the Java
technologies.

:samp:`JVM`: Java Virtual Machine. It is a set of software programs that interprets the Java byte code.

:samp:`JRE`: Java Runtime Environment. It is a kit distributed by Sun to execute Java programs. A :samp:`JRE` provides a :samp:`JVM` and some basic Java libraries.

:samp:`JDK` or :samp:`SDK`: Java (or Software) Development Kit bound to the programmer. It provides a :samp:`JRE`, a compiler, useful programs, examples and the source of the API (Application Programming Interface: some standard libraries). 

It is strongly recommended to download a :samp:`JDK`, in order to both compile and run the model. Builds for different platforms can be found `here <https://www.oracle.com/java/technologies/downloads/>`_.

.. _nc_inst:

NetCDF4 
####################

The Java library that manages input/outputs of
NetCDF files requires the external NetCDF C library, which can be installed as follows:

Mac Os X
@@@@@@@@@@@@@@@

To install the library on a Mac Os system, open a Terminal and type:

.. code-block:: bash

    sudo port install netcdf4

Linux
@@@@@@@@@@@@@@@


To install the library on a Linux system, open a Terminal and type:

.. code-block:: bash

    sudo apt-get install netcdf4

Windows
@@@@@@@@@@@@@@@

To install the library on a Windows system, download the pre-built libraries ib
`Unidata website <https://docs.unidata.ucar.edu/netcdf-c/current/winbin.html>`_

.. danger::

    During the install process, make sure that the location of the library is added to 
    the :samp:`PATH`


Conda environmenmt
########################

There also is the possibility to use Conda environments in order to install Maven, OpenJDK and NetCDF4 easily. Instructions can be found on https://github.com/ichthyop/ichthyop-conda
