# Manager initialization

When an Ichthyop simulation is launched, managers are mobilized in the following order (cf. `SimulationManager.mobiliseManagers()` method):

```{eval-rst}
.. mermaid:: _static/mermaid/setup.md
    :caption: Order in which managers are mobilized.
    :align: center
```

This order is the one in which the managers will be setup and then initialized.

During the setup process, the `setupPerformed` methods, implemented on all the manager classes, will be called.

After this setup process, the managers will be initialized. This will be achieved by calling
the `initializePerformed` methods,  implemented on all the manager classes.
