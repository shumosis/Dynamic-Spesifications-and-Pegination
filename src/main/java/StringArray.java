public class StringArray {

    public static void main(String args[]) {

        String str = "Shubham";
        char target = 'k';
     System.out.println(findChar(str,target));

    }
    public static boolean findChar(String str,char target){

        for(char letter : str.toCharArray()){
            if(target==letter){
                return true;
            }
        }
        return false ;
    }
}
