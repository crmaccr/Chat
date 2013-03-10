

package chatprogram;
//custom exception class to be used when a user's username has already been added to the chat

public class usernameTaken extends Exception{
    public usernameTaken(){
        super("Username is already taken. Please choose another.");
    }
}
