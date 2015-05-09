# Introduction
Using wro4j with Ant build tool is not supported out of the box, but you can easily use it with wro4j-runner help. Below is a configuration example:

```xml
<project name="wro4j" default="minify" basedir=".">
        
        <!-- properties file --> 
        <property file="build.properties"/>

        <!-- macro for interacting with wro4j --> 
        <macrodef name="wro4j">
                <attribute name="processor"/>
                <attribute name="wro-file" default="wro.xml"/>
                <attribute name="target-groups"/>
                <attribute name="context-dir" default="${basedir}"/>
                <attribute name="output-dir" default="${basedir}/bin"/>
                <sequential>
                        <exec executable="java" failonerror="true">
                                <arg value="-jar"/>
                                <arg value="lib-build/wro4j-runner-1.4.1-jar-with-dependencies.jar"/>
                                <arg value="--wroFile"/>
                                <arg value="@{wro-file}"/>
                                <arg value="--contextFolder"/>
                                <arg value="@{context-dir}"/>
                                <arg value="--targetGroups"/>
                                <arg value="@{target-groups}"/>
                                <arg value="--destinationFolder"/>
                                <arg value="@{output-dir}"/>
                                <arg value="-c"/>
                                <arg value="@{processor}"/>
                        </exec>
                </sequential>
        </macrodef>

        <!-- minify target --> 
        <target name="minify">
                <wro4j processor="yuiCssMin" target-groups="global-css"/>
                <wro4j processor="yuiJsMin" target-groups="global-js"/>
                <echo>Minification Completed Successfully</echo>
        </target>

</project>
```