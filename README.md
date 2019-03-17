<p align="center">
  This library is used and taught by:
  <a href="https://www.spigotcourse.org/?utm_source=github&utm_medium=github">
    <img src="https://i.imgur.com/Xr0p2g3.png" />
  </a>
</p>

---

# CompatBridge

> NOTICE: Requires 'api-version: 1.13' set in your plugin.yml!

## Usage
This library allows you to develop plugin using the latest Minecraft API while maintaining backward compatibility down to Minecraft 1.7.10.

## Installation
We use Maven to compile it. See below for a step-by-step tutorial.

0. Set the following in your plugin.yml file:
```yaml
    api-version: 1.13
```

1. Place this to your repositories:

```xml
<repository>
	<id>jitpack.io</id>
	<url>https://jitpack.io</url>
</repository>
```

2. Place this to your dependencies (Replace `{COMMIT}` with the latest [commit-hash]):

```xml
<dependency>
	<groupId>com.github.kangarko</groupId>
	<artifactId>CompatBridge</artifactId>
	<version>{COMMIT}</version>
	<scope>compile</scope>
</dependency>
```

3. Make sure that the library shades into your final .jar when you compile your plugin. Here is an example of a shade plugin that will do it for you:

IF YOU ALREADY HAVE A SHADE PLUGIN, ONLY USE THE RELOCATION SECTION FROM BELOW.

```xml
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
				<shadedPattern>you.yourplugin.compatbridge</shadedPattern>
			</relocation>
		</relocations>
	</configuration>
</plugin>
```

Copyright (C) 2019. All Rights Reserved. Commercial and non-commercial use allowed as long as you provide a clear reference for the original author.  

[commit-hash]: https://github.com/kangarko/CompatBridge/commits/master
