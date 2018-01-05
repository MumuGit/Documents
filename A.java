class  A
{
	public static void main(String[] args) 
	{
		 double sum=13000;
        for(int i=0;i<24;i++){
            sum=(sum+2000)*1.035;
        }
        for(int i=0;i<10;i++){
            sum=(sum+4000)*1.035;
        }
       // for(int i=0;i<10;i++){
      //      sum=(sum+6000)*1.035;
      //  }
       // for(int i=0;i<8;i++){
       //     sum=(sum+8000)*1.035;
       // }
        System.out.println("muqi"+sum);
		System.out.println("Hello World!");
	}
}
