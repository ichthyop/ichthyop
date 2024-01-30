(new-tracker)=

# Adding output variable

When including new processes to Ichthyop (cf. {numref}`new-action`), the storage of additional variable may be required.
For instance, in the growth processes  (see {numref}`growth-proc`), in which particle length is a state variable, it is necessary to
save length in the output NetCDF file. This is done by creating a new Java class associated with
a property file.

# Creating java class

Creating the Java class depends on what type of variable you want to save, as shown
in {numref}`fig-output-diag`.

(fig-output-diag)=

```{eval-rst}
.. mermaid:: _static/mermaid/output.md
    :caption: Adding new variables to the output file.
    :align: center
```

## General case

Adding new variables can be achieved by creating new tracker class in the `org.previmer.ichthyop.io` package, which inherits
from the `AbstractTracker` java class. It must override the 4 methods, as shown below for the `LengthTracker` class:

```java
public class LengthTracker extends AbstractTracker {

    @Override
    public void setDimensions() {
    }

    @Override
    public void addRuntimeAttributes() {
    }

    @Override
    public Array createArray() {
    }

    @Override
    public void track() {
    }

}
```

`setDimensions` defines the dimensions associated with the variable. Time and drifter dimensions must be added by
using the `addTimeDimension()` and `addDrifterDimension()` methods, respectively. A zone dimension can be added by
calling the `addZoneDimension(TypeZone zoneType)` method. Custom dimensions can be added by using the
`addCustomDimension(Dimension dim)` method.

`addRuntimeAttributes` defines additional attributes associated with the variable to be saved. Attributes are
added by calling the `addAttribute(Attribute attribute)` method.

:::{note}
Compulsory attributes and variable names
are defined using properties files, see {numref}`tracker-prop`
:::

`createArray` initializes the `Array` object that will be used to store the output variable.
The dimensions of the array depends on the dimension of the output variables.

`track()` is the method that is called at each output time-step and which writes the variable in the NetCDF.

## Simple case

Usually, new variables consist in tracking one single particle property, such as length for instance. In this case, the new
tracker class can inherits either from the `FloatTracker` or the `IntegerTracker` classes as follows:

```java
public class LengthTracker extends FloatTracker {

    @Override
    float getValue(IParticle particle) {
        ...
    }
}
```

In this case, the only method to define is the `getValue(IParticle particle)`, which specifies which particle's
state variable is to be extracted for the given tracker.

(tracker-prop)=

# Creating property file

In addition to the tracker java class, a property file must be included in the `io/resources/` folder. The name of this
file must me the same as the Java class, except for the `.properties` suffix. For instance, the property file associated
with the `LengthTracker.java` class will be named `LengthTracker.properties`. It must contain the following three lines:

```bash
tracker.shortname = length
tracker.longname = particle length
tracker.unit = millimeter
```

`tracker.shortname` is the name of the variable in the NetCDF, `tracker.longname` and `tracker.unit` are the values of the
variable's longname and unit attributes.
