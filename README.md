Tank Game


## Steps to Import project into IDE:
Go into terminal and run, git clone https://github.com/SKTC4444/TankGame.git
Then cd into the project and open it into an IDE.

## Steps to Build your Project:
On Mac:
cd into project: cd ~/Desktop/csc413-tankgame-SKTC4444
compile: javac -d out -sourcepath src src/TankGame/Launcher.java
make JAR jar cvfe LauncherJAR.jar TankGame.Launcher -C out .
run JAR: java -jar LauncherJAR.jar

On Windows:
cd into project: cd C:\Users\YourUsername\Desktop\csc413-tankgame-SKTC4444
compile: javac -d out -sourcepath src src\TankGame\Launcher.java
make JAR: jar cvfe LauncherJAR.jar TankGame.Launcher -C out .
run JAR: java -jar LauncherJAR.jar

## Steps to run your Project:
You could use the steps above in terminal for running the JAR,
Or you could run the JAR through the IDE by running "LauncherJAR".

## Controls to play your Game:

|              | Player 1 | Player 2    |
|--------------|----------|-------------|
| Forward      | W        | up arrow    |
| Backward     | S        | down arrrow |
| Rotate left  | A        | left arrow  |
| Rotate Right | D        | right arrow |
| Shoot        | spacebar | enter       |
| Item         | F        | L           |
    
