#Chat

This chat program supports the following:
* chatroom anyone can join
* private chat among two people
* file sharing in private chat

As it is, I would like to polish it and add other features (like tabs instead of seperate windows)

##Compiling
I wrote this using Eclipse, so the easiest way to compile would be to import using Eclipse->General->Existing Files From Workspace and then compiling using Eclipse's compiler.
If you wish to compile from the command line, something like this should work:

    mkdir bin
    javac -d bin src/chatprogram/*
    cd bin
    java chatprogram.Main

