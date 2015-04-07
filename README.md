# Graphical Alfresco DataModel
Generate graphical representation of an Alfresco data model.

This first implementation generate a graph in _dot_ language from [Graphviz](http://www.graphviz.org/) project.

## Requirements

* [Gradle](http://gradle.org/downloads/)
* [GraphViz](http://www.graphviz.org/)

## Installation

``` bash
git clone https://github.com/jeci-sarl/Graphical-Alfresco-DataModel.git
cd Graphical-Alfresco-DataModel
gradle installDist
```

## Run

``` bash
cd build/install/Graphical-Alfresco-DataModel/

# Printing standard Alfresco Content Model
java -jar graphical-alfresco-datamodel-0.2.jar -a contentModel.xml | xdot -

# Print your own content model
java -jar graphical-alfresco-datamodel-0.2.jar -c MyCustomModel.xml > MyCustomModel.gv
dot -Tpng MyCustomModel.gv -o MyCustomModel.png
```



