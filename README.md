# Gephi visualization engine

![stability-wip](https://img.shields.io/badge/stability-work_in_progress-lightgrey.svg)

This is a new visualization engine for Gephi based on modern OpenGL techniques.

It aims to be:

* Retro-compatible with old OpenGL versions through feature discovery, falling back to the best supported by the graphics card
* High performance using most modern OpenGL when available, specially due to instancing, manual buffer management, using simple shaders and avoiding memory allocation when possible
* Extensible with plugins (rendering and input)
* JOGL and LWJGL3 version
* Usable in AWT/Swing/NEWT/SWT with JOGL. For LWJGL3 GLFW/AWT is available, but AWT only for Linux and Windows at the moment.
* Nicely interactive with mouse, directional zooming, etc with default input handler
* Only a 2D engine for the moment
* The only gephi-related dependency is graphstore

Currently, in comparison to Gephi 0.9.2 renderer it's lacking:

* Self loops
* Node/edge text labels
* Selected nodes animation (should be doable with a simple uniform variable)

NOTE: to build it, first you will need to build the graphstore branch at https://github.com/gephi/graphstore/tree/viz-engine
