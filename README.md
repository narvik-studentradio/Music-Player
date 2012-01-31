# nsr-mp, Narvik Student Radio Music Player
A project to make a radio music player based on gstreamer-java to play music and spots.

This is an IDLE radio player, its meant to play music, radiospots, update icecast while the radio staff does NOTHING.
The program will run on a server hidden somewhere, it loads music, spots, it even logs what has been played and then, updates icecast.
The Radio Music Machine For Lazy Student Radio Members

## Installation
1. Place nsr-mp.jar in a sutable folder
1. Copy nsrmp.example.properties to the same folder and rename it nsrmp.properties
1. Modefy nsrmp.properties to suit your needs
1. Launch the application with "java -jar nsr-mp.jar"

## Requirements
- Java 1.6
- Linux based OS (or something that plays nice with gstreamer)

# License
NSR-MP's source code is licensed under the
[GNU General Public License](http://www.gnu.org/licenses/gpl.html),
except for the  external libraries listed below.

# External Libraries
* [gstreamer-java-1.5](http://code.google.com/p/gstreamer-java/) (included)
* [jna-3.2.4](http://code.google.com/p/gstreamer-java/) (included)

# Authors
Versjon 0.2 - 2012 - [Vegard Lang&aring;s](http://sjefen6.no)  
Versjon 0.1 - 2011 - [Viktor Basso](http://basso.cc)

For [Narvik Studentradio](http://nsr.samfunnet.no)

## Thanks to
Christoffer M&uuml;ller Nordeng for reviewing the code and offering design assistance.