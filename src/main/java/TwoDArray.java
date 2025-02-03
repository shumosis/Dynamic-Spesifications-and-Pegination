public class TwoDArray {

    public static void main(String args[]){

        int[][] accounts={
            {1,5},
            {7,3},

                {3,5}
        };

        System.out.println(maximumWealth(accounts));

    }
    public static int maximumWealth(int[][] accounts){
        //person = row
        //bank = col
        int ans =Integer.MIN_VALUE;
        for(int person=0 ;person< accounts.length ;person++){
            int sum =0;

            for(int bank =0;bank<accounts[person].length;bank++){
                sum+=accounts[person][bank];
            }

            if(sum>ans){
                ans=sum;
            }
        }
        return ans;
    }
}
