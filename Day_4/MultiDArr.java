public class MultiDArr {
      public static void main(String[] args) {
            int [][] myNumbers = { { 1, 4, 2 }, { 3, 6, 8, 5, 2 } };
            System.out.println("Printing using basic code");
            for (int row = 0; row < myNumbers.length; row++) {
                  for (int col = 0; col < myNumbers[row].length; col++) {
                        System.out.println("MyNumbers[" + row + "] = " + myNumbers[row][col]);
                  }
            }

            System.out.println("\nPrinting using For-Ech");
            for (int[] row : myNumbers) {
                  for (int num : row ) {
                        System.out.println(num);
                  }
            }
      }
}
