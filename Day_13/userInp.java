import java.util.Scanner;

public class userInp{
    public static void main(String[] args) {
        Scanner myobj = new Scanner(System.in);
        System.out.print("Enter username: ");

        String username = myobj.nextLine();
        System.out.println("Username is: "+ username);
    }
}