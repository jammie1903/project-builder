## Project builder

A project for building other projects that are constructed from multiple libraries. 

### Releases

Releases will contain a zip folder which contains the compiled jar, 
as well as an app folder which will allow the jar to be run via an exe in windows.

#### Non windows
For non windows users, just the project-builder.jar file is required.
#### Windows
for windows users, copy the app folder to anywhere on your machine (I go for program files, because why not?) .

If you want the project to create symlinks for you, you must give it admin permissions!. 
to do this: 
- right-click project-builder.exe and open "properties"
- go to the compatibilty tab and check "Run this program as an administrator"