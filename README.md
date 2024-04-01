# gltf-io  

Copyright Rickard Sahlin
This project is licensed under the terms of the MIT license.

A Java based serializer/deserializer and memory model for glTF  

This project is based on https://github.com/rsahlin/graphics-by-opengl  
The main changes are that the code for glTF loading and the mappings of JSON to java are moved to a separate project.  

Goal of this project is:  

Provide a java class mapping of glTF JSON.  
Support loading of glTF sourcefiles into java classes.  
Provide means to prepare loaded sourcefiles for easy access, for instance resolving objects being referenced by indexes (nodes, meshes etc).  
Allow manipulation of data buffers (for instance BufferViews) - adding normals/tangents or removal of unused buffers/attributes.  
Basic functionallity to create and save glTF files.  


# Build instructions  

This is a Maven based project, based on a java module.  

Navigate to the /java folder and build using maven:  

'mvn clean install'

This will build the classes and install in local maven repository so that other projects that depend on it can be built.  
