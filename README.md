SolarNBody
==========
Overarching functionality goals:
1. Create a functional n-body simulator of the solar system.
2. Google maps but in space.

Learning goals:
1. Develop general programming skills, as well as OpenGL, LWJGL, and Java
2. Learn about astrophysics
3. Learn how to use GitHub ;)

This project requires LWJGL and a csv file to load bodies from.
The CSV file should be in this format:

ID, Name, Parent, Mass(kg), Radius (m), Satellite Count, Satellite's Mass, System Mass, Semi-Major Axis, Eccentricity, Longitude of Ascending Node, Arg of Peri, True Anomaly, <space>, Type

All angles should be in degrees. Recognizable types are "Star", "Planet", "Dwarf Planet", "Moon", and "Asteroid". The "Parent" column refers to the ID of the parent body. If that body has no parent, set it to -1.
