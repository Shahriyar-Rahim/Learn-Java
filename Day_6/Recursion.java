public class Recursion{
      public static int sum(int k) {
              if(k > 0){
                  return k + sum(k - 1);
              } else {
                  return 0;
              }
      }

      public static void main(String[] args) {
            int result = sum(100);
            System.out.println("The sum of 1 to 100 is = " + result);
      }
}