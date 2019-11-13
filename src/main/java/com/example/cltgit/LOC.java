package com.example.cltgit;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LOC {
	public long CountNumberOfTextLines( String filePath )
	{
        long lineCount = 0;

		// finally ブロックの中で BufferedReader のストリームを閉じて解放できるように、try ブロックの外で変数を定義します。
        BufferedReader br = null;

        try
        {
            FileReader fr = new FileReader( filePath );
            br = new BufferedReader(fr);

            String line;
			line = br.readLine();
			
            while( line != null )
            {
                lineCount++;
                line = br.readLine();
            }
        }
		catch( FileNotFoundException e )
		{
            System.out.println(e);
		}
        catch( IOException e )
        {
            System.out.println(e);
        }
        finally
        {
			// 例外が発生したときでもファイルを閉じて解放する。ここでも例外がスローされる可能性があるので、例外処理をする。
        	try
        	{
                if( br != null )
            		br.close();
            }
            catch( IOException e )
            {
	            System.out.println(e);
            }
        }

        return lineCount;
	}
}
