package FileManager;

import Indexer.Indexer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileManager {

    TreeMap<String, TreePointerToQ> Cache;
    int PriorityAll;
    HashMap<String,String> cities;
    public static int DocNum;
    public static String postingpath;
    public String DocInfo;


    public FileManager(String docId, String path) {
        Cache=new TreeMap<String,TreePointerToQ>();
        cities=new HashMap<String,String>();
        PriorityAll=0;
        DocInfo="";
        postingpath=path;
    }

    public static String geturl(String pointer){
        char firstLetter=pointer.charAt(0);
        if(Character.isLetter(firstLetter)&&Character.isLowerCase(firstLetter)){
            return postingpath+"\\Indexing\\"+firstLetter+".txt";
        }else if(Character.isDigit(firstLetter)||firstLetter=='$'||firstLetter=='%'){
            return postingpath+"\\Indexing\\Numbers.txt";
        }else if(Character.isUpperCase(firstLetter)){
            return postingpath+"\\Indexing\\CapitalLetters.txt";
        }
        return postingpath+"\\Indexing\\Else.txt";
    }

    public void AllTermToDisk() throws InterruptedException {
       PushTermsToDisk(); //because of error (last element)
     }
    public void DocPosting(String ID, String City, int maxtf, int uniqueterms, String mostTf, String cityplaces){
        DocNum++;
        AddDocToCityIndex(ID,City);
        DocInfo+=("|"+ ID+","+ City + "," + maxtf+ ","+ uniqueterms+ ","+ mostTf+","+cityplaces);
        if(DocInfo.length()>1000000){
            AllDocumentsToDisk();
        }
      //  System.out.println(DocNum);
    }
    void AddDocToCityIndex(String DocId,String City){
        if(City.equals(""))return;
        if (cities.containsKey(City)) {
            cities.put(City,cities.get(City)+ "," + DocId);
        } else {
            cities.put(City, DocId);
        }
    }

    public void CitiesToDisk(){
        System.out.println("cities to disk");
        Iterator<Map.Entry<String,String>> it= cities.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> currCity=it.next();
            File file =new File(postingpath+"\\Cities\\"+currCity.getKey()+".txt");
            try {
                file.createNewFile();
            } catch (IOException e) {
               e.printStackTrace();
            }
            try (FileWriter fw = new FileWriter(postingpath+"\\Cities\\"+currCity.getKey()+".txt", true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                out.print(currCity.getValue());
            } catch (IOException e) {
             //   e.printStackTrace();
            }
        }

    }

    public void AddToPosting(String key, Integer value, String docID,int line) {
        if (Cache.containsKey(key)) {
            Cache.put(key,new TreePointerToQ(null,Cache.get(key).value + "|" + docID + "," + value));
        } else {
            Cache.put(key,new TreePointerToQ(null, "|" + docID + "," + value));
        }
        if(Cache.size()>100000){
            PushTermsToDisk();
        }
    }

    private void PushTermsToDisk() {
        System.out.println("====DELETING");
        TreeMap<String ,TreePointerToQ> TermToFile=Cache;
        Cache=new TreeMap<String, TreePointerToQ>();
        char currletter = '*';
        StringBuilder [] currentfile=null;
        for (Map.Entry<String, TreePointerToQ> entry : TermToFile.entrySet()) {
            if(entry.getKey().charAt(0)!=currletter){
                if(currentfile!=null){
                    StringJoiner sj=new StringJoiner("\n");
                    for (int k = 0; k <currentfile.length ;k++) {
                        sj.add(currentfile[k]);
                    }
                    try (FileWriter fw = new FileWriter(geturl(""+currletter), false);
                         BufferedWriter bw = new BufferedWriter(fw);
                         PrintWriter out = new PrintWriter(bw)) {
                        out.print(sj);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currletter=entry.getKey().charAt(0);
                try {
                    String[] arrFromFile=new String(Files.readAllBytes(Paths.get(geturl(""+currletter))), Charset.defaultCharset()).split("\n");
                    if(Character.isLetter(currletter)&&Character.isLowerCase(currletter)){
                        currentfile=new StringBuilder[Indexer.linenumber[currletter-97]];
                    }else if(Character.isLetter(currletter)&&Character.isUpperCase(currletter)){
                        currentfile=new StringBuilder[Indexer.linenumber[27]];
                    }
                    else{
                        currentfile=new StringBuilder[Indexer.linenumber[26]];
                    }
                    for (int j = 0; j < arrFromFile.length; j++) {
                        currentfile[j]=new StringBuilder(arrFromFile[j]);
                    }
                    for (int j = arrFromFile.length; j <currentfile.length ; j++) {
                        currentfile[j]=new StringBuilder("");
                    }
                    arrFromFile=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            currentfile[entry.getValue().lineNumber].append(entry.getValue().value);


        }
        System.out.println("====STOP- DELETING");
    }

    public void SetCapitalToLoweCasePosting(String key, String value) {
        if (Cache.containsKey(key)) {
            Cache.get(key).value=Cache.get(key).value +value;
            Cache.get(key).pc.priority++;
            Cache.put(key, Cache.get(key));
        } else {
            Cache.put(key,new TreePointerToQ(null, value));
        }
        if(Cache.size()>50000){
            PushTermsToDisk();
        }
    }

    public void AddCapitalLettersToDisk(TreeMap<String, String> capitalLetterPosting) {
        for (Map.Entry<String, String> entry : capitalLetterPosting.entrySet()) {
            Cache.put(entry.getKey(),new TreePointerToQ(null, entry.getValue()));
        }

    }

    public void AllDocumentsToDisk() {
        File file =new File(postingpath+"\\Documents.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter fw = new FileWriter(postingpath+"\\Documents.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print( DocInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DocInfo="";
    }
}

class PointerCache {
    int priority;
    String pointerterm;

    public PointerCache(String pointerterm, int priority) {
        this.pointerterm = pointerterm;
        this.priority = priority;
    }
}
class TreePointerToQ{
    public PointerCache pc;
    public String value;
    public int lineNumber;

    public TreePointerToQ(PointerCache pc, String value) {
        this.pc = pc;
        this.value = value;
    }
}
