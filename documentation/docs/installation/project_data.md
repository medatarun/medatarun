# Project data and versioning

When you run medatarun in a directory, that directory becomes the project directory.
Medatarun creates a subdirectory named .medatarun. It contains the data used by Medatarun and its extensions. 
Each extension can have its own subdirectory inside it. This is the Medatarun directory.

`.medatarun` stores your models and any data managed by the extensions.
If the project directory is under version control (for example with Git), you should commit `.medatarun`.

Medatarun is designed to let AI agents manage your models. Committing `.medatarun` lets you see what these agents change 
over time and inspect it with standard version-control tools.