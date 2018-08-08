# CompatBridge

> NOTICE: Only works correctly if you have 'api-version: 1.13' in your plugin.yml!

## Usage
This library allows you to develop plugin using the latest 1.13 API libraries while maintaining backward compatibility with 1.12 and older.

## Installation
We use Maven to compile and so you need to, to use this library easily. See below for a step-by-step tutorial.

Notice: If you are having a builder Ant task, you should head over to /releases page (add it to the URL bar) to download the jar and install it as a plugin to have classes available for testing conditions. Otherwise, always shade the classes directly and ship them with your plugin.

Copyright: All Rights Reserved (C) 2018. Commercial and non-commercial use allowed as long as you provide a clear link on your (sales) page that you are using this library.  

0. Set the following in your plugin.yml file:
		api-version: 1.13

1. Place this to your repositories:

		<repository>
			<id>jitpack.io</id>
			<url>https://jitpack.io</url>
		</repository>

2. Place this to your dependencies:

		<dependency>
			<groupId>com.github.kangarko</groupId>
			<artifactId>CompatBridge</artifactId>
			<version>1.0.0</version> <!-- change to the latest version -->
			<scope>compile</scope>
		</dependency>
    
2. Make sure that the library shades into your final .jar when you compile your plugin. Here is an example of a shade plugin that will do it for you:

IF YOU ALREADY HAVE A SHADE PLUGIN, ONLY USE THE RELOCATION SECTION FROM BELOW.

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-shade-plugin</artifactId>
				<version>3.1.1</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>shade</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<createDependencyReducedPom>false</createDependencyReducedPom>
					<relocations>
						<relocation>
							<pattern>me.kangarko.compatbridge</pattern>
							<shadedPattern>${project.groupId}.ui</shadedPattern>
						</relocation>
					</relocations>
				</configuration>
			</plugin>
