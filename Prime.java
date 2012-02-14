import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Prime {

	public static void main(String[] args) {
		
		InputStreamReader isr = new InputStreamReader(System.in); 
		BufferedReader br = new BufferedReader(isr);
		
		int n = 0;
		int j;
	
		try
		{
		System.out.println("Enter a number");
		n = Integer.parseInt(br.readLine());
		}
		catch (Exception ex)
        {
                System.out.println(ex.toString());
        }
		
		System.out.println("All prime numbers from 1 to " + n + " are:");
		
		for(int i = 1; i <= n; i++)
		{
			j = 0;
			
			for(int k = 1; k <= i; k++)
			{
				if(i % k == 0)
				{
					j++;
					if (j>2)
					{
						// Don't need to search further
						break; // leave k-loop
					}
				}
			}
			if(j == 2)
				{
					System.out.println(i);
				}
		}

	}

}
