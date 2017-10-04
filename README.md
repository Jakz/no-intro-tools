## NoIntro Tools

This program is meant to scan, verify and organize romsets through DATs files released by NoIntro group (more information can be found [here](http://datomatic.no-intro.org/). I decided to start working on this because the alternatives are really outdated, or Windows only, or really cumbersome to use.

This is really in a alpha stage!

### Scanner
It's able to scan and verify against a DAT, by checking file size, CRC32, SHA-1 and MD5, 3 kinds of file at the moment:

- binary files
- archives
- archives nested inside other archives

All archive types supported by [7-Zip-JBinding](http://sevenzipjbind.sourceforge.net/) are theoretically supported but for now just ZIP, 7Z and RAR have been tested.

### Organizer

The organizer is able to provide 4 kinds of organizatons at the moment:

- uncompressed files
- compressed in a single archive
- one archive per game
- one archive per clone (so multiple clones of the same game are compressed in same solid archive)

The last mode is especially useful to save a lot of space but it requires additional clone data for the specific DAT to let the program know how to group games.

### Future an what not

The architecture of the whole project is really modular, most of the structure related stuff comes from another project of mine: [RomLib](https://github.com/Jakz/romlib), which is a framework supposed to provide generic classes to work on roms and games.

This means that it's easy to parser for a new format which could be supported, it's easy to add custom renamers to provide specific behavior to the organizer and what not. Feel free to contribute or fix/add functionality.

### Building
I'm working with Eclipse and this project relies on other two Maven based projects. The simple solution for me has been to just add these two modules as related project in Eclipse.

Other solutions would have required to be able to get them as Maven dependency but this would have forced me to commit every change and whatever.

To keep this brief: __you can't use the root `pom.xml` to build the project. Rather you must go inside `build` folder and execute `build.sh` which will clone the two dependencies and compile all the source together with Maven based on a different `pom.xml`.__ It's ugly I know and it will be fixed one day.

### Running
The only enabled subcommand at the moment is `organize`. You can try with

    java -jar nit.jar organize --help
    
to get an idea of the various options. A simple example which would scan and merge a set from a folder to another folder is the following:

    java -jar nit.jar organize 
      --dat-file gameboy.dat 
      --dat-format logiqx 
      --clones-file gameboy.xmdb 
      --roms-path /myromsfolder
      --fast
      --skip-rename
      --merge-path /mymergedfolder
      --merge-mode archive-by-clone
      --auto-merge-clones
      
Mind that it's a good idea to backup your test data before using the software because it hasn't been thoroughly tested. Please report any issue/feature you'd like to see.