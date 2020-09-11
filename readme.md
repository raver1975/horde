# The Horde

AI powered music groovebox.

[![The Horde](https://img.youtube.com/vi/FjkpVbbDtMY/0.jpg)](https://www.youtube.com/watch?v=FjkpVbbDtMY)

## Current Features

* 11 instrument MIDI synthesizers
* 1 drum MIDI synthesizers
* 2 simulated Roland bass synthesizers (TB-303)
* 2 simulated Roland drum synthesizers (TR-808 or TR-909) 
* records entire session into WAV file
* Grab a track from spotify, split into stem files, and then chop it up.
* Spleeter stem separation
* Tatum based sampler

## Roadmap

* UI work
* more expressive sequencers
* live waveform display
* automation
* record/playback midi 
* physical midi input/input
* AI power

## Getting Started
prerequisites:
Java 1.8+, Python 3.6 and Gradle

```
git clone https://github.com/raver1975/horde.git
gradle run
```
## Command Line

(in any order)

* spotify uri or file to open


* Use Spleeter to divide song into stems
  * STEM0 - none
  * STEM2 - vocals, other
  * STEM4 - vocals,bass,drum,other
  * STEM5 - vocals,piano,bass,drum,other

* tempo in beats per minute to timeshift song
(0 to change tempo to match song)


```
gradle run --args="spotify:track:0gtZnyaA8Et7P4PqSowzu3 STEM4 60"
```