package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;



class DocProcessor{
    public String body;
    public String title;
    public int index;
    public File[] file;
    public Scanner sc;
    public boolean simple;
	private BufferedReader br;


    public DocProcessor(String path) throws FileNotFoundException{
    	if(path.equals("data/simple")){
    		String  corpusFile = path + "/corpus.tsv";
    		file = new File[0];
    		sc = new Scanner(new FileInputStream(corpusFile));
    		simple = true;
    	}
    	else{
    		File dir = new File(path);
    		file = dir.listFiles();
    		simple = false;
    	}
        index = 0;
    }

    public Boolean hasNextDoc(){
    	if(simple){
    		return sc.hasNextLine();
    	}
    	else{
    		return index < file.length;
    	}
    }

    public void nextDoc(){
    	if(simple){
    		if(sc.hasNextLine()){
    			String content = sc.nextLine();
				Scanner s = new Scanner(content).useDelimiter("\t");
    			title = s.next();
    			body = s.next();
                index++;
                if(index % 100 == 0)
                    System.out.println(index);
    		}
            else{
                title = null;
                body = null;
            }
		}
    	else{
            if(index < file.length){
                title = file[index].getName();
                String fileAsString = null;
				try {
					fileAsString = FileUtils.readFileToString(file[index]);
				} catch (IOException e) {
					e.printStackTrace();
				}
                body = docProcess(fileAsString);
                index++;
            }
            else{
                title = null;
                body = null;
            }
    	}
    }

    // To convert html file to a string 
    static public String docProcess(String input){
        String docString = Jsoup.parse(input).text();

        //Pattern nonASCII = Pattern.compile("[^\\x00-\\x7f]");
        //str = nonASCII.matcher(str).replaceAll();
        return docString;
    }
    
    static public void main(String[] args){
    	File f = new File("data/wiki/'03_Bonnie_&_Clyde");
        StringBuilder sb= new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while(line != null){
                sb.append(line);
                line = br.readLine();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        String output = docProcess(sb.toString());
        System.out.print(output);
    }
}
