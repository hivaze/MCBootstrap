![Logo](https://i.ibb.co/HTL2W75/1.png "Logo")

# Quik introduction
This is very simple and powerful standalone tool for **minecraft** servers **administrators**, for **atomization** and improvement of your **network management** using **GNU Screen** utility . MCBootstrap helps to manage full lifecycle of yout servers and scripts, log their work and can restart it automatically. An **important feature** is the ability to **generate** clones of servers in runtime and create **custom addons**.

## Main features
* Easy to use. All settings are in one file _config.yml_.
* Can start and stop **ANYTHING** you want _(all kind of servers, proxy, scripts, etc)_ **ANYTIME**
* **Easy to manage**. This application uses console with commands like minecraft server.
* Total and clear logging. The logging system create new file at each system startuo
* Very convenient for minigames networks. **MCBootstrap can generate many servers based on one** (useful for arenas generation).
* Configurable autorestart for any object
* **Extensions system**. If you are a developer, you can supplement this system for yourself.

## So why to use this?
If you have a huge Minecraft network it's hard to manage all servers statuses, so now you can install this application, setup [config.yml](https://github.com/LITEFINE/MCBootstrap/blob/master/src/main/resources/config.yml) and manage servers/scripts through the MCBootstrap console, saving a huge amount of time on configuration of each server and its start / stop. If you are interested, I recommend you to go to the [wiki](https://github.com/LITEFINE/MCBootstrap/wiki).

## Installation
* Download builded jar: https://github.com/LITEFINE/MCBootstrap/raw/master/target/MCBootstrap-2.0-SNAPSHOT.jar or build by your own with git
* (optional) Rename your jar to MCBootstrap.jar
* Now create somewhere a new folder and put jar in
* To run MCBootstrap open console, create new screen (optional), go to your folder and type ```java -Xmx256M -XX:+UseBiasedLocking -jar MCBootstrap.jar```
* Stop application using ```shutdown``` command and setup your config.yml settings as you want, [help here](https://github.com/LITEFINE/MCBootstrap/wiki/Config-setup)

## Run MCBootrap on LINUX startup
MCBootrap has feature to start all your booting objects from config on application startup, so you if you want to make MCBootrap start on LINUX startup you need read [this wiki page](https://github.com/LITEFINE/MCBootstrap/wiki/Boot-on-LINUX-startup)

## Appearance of UI
![Logo](https://i.ibb.co/k9th1jD/2019-01-27-3-28-00.png "Screenshot of work")
