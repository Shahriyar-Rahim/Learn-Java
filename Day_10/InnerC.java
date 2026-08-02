public class InnerC{
    static class OC{
        int x = 10;

        static class INc{
            int y = 34;
        }
    }

        public static void main(String[] args) {
            OC moc = new OC();
            OC.INc mic = new OC.INc();
            System.out.println(moc.x + mic.y);
        }

}