

import java.io.*;
import java.util.*;

public class Vsim {
    static boolean isBreak=false;
    static int currAddress=256;
    static int dataStartAddress=0;
    static List<String> disassemblyList = new ArrayList<>();
    static List<String> simulationList = new ArrayList<>();
    static Map<String,String>rOpMap=new HashMap<>();
    static Map<String,String>iOpMap=new HashMap<>();
    static Map<String,String>sOpMap=new HashMap<>();
    static Map<String,String>uOpMap=new HashMap<>();
    static Map<String,Integer>regMap=new HashMap<>();
    static Map<Integer,Integer>dataMap=new HashMap<>();
    static Map<Integer,String>instrMap=new HashMap<>();
    public static void main(String[] args) {
        try {
            String inputFileName=args[0];
            FileReader reader = new FileReader(inputFileName);
            BufferedReader bufferedReader = new BufferedReader(reader);
 
            String line;
            List<String>instructions=new ArrayList<>();
 
            while ((line = bufferedReader.readLine()) != null) {
                instructions.add(line);
            }
            reader.close();
            generateDisassembly(instructions);
            currAddress=256;
            generateSimulation();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void generateDisassembly(List<String> instructions){
        Map<String,String>instrType=new HashMap<>();
        instrType.put("00","S-type");
        instrType.put("01","R-type");
        instrType.put("10","I-type");
        instrType.put("11","U-type");
        createROpMap(rOpMap);
        createIOpMap(iOpMap);
        createSOpMap(sOpMap);
        createUOpMap(uOpMap);
        for(String instruction:instructions){
            if(isBreak){
                generateData(instruction);
                continue;
            }
            String key=instruction.substring(30);
            String type=instrType.get(key);
            switch(type){
                case "S-type":
                    generateSType(instruction);
                    break;
                case "R-type":
                    generateRType(instruction);
                    break;
                case "I-type":
                    generateIType(instruction);
                    break;
                default:
                    generateUType(instruction);
                
            }
        }
        writeToFile("disassembly.txt",disassemblyList);
    }
    public static void writeToFile(String filename, List<String>list){
        try {
            FileWriter writer = new FileWriter(filename, true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for(String str:list){
                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void createROpMap(Map<String,String>map){
        map.put("00000","add");
        map.put("00001","sub");
        map.put("00010","and");
        map.put("00011","or");
    }
    public static void createIOpMap(Map<String,String>map){
        map.put("00000","addi");
        map.put("00001","andi");
        map.put("00010","ori");
        map.put("00011","sll");
        map.put("00100","sra");
        map.put("00101","lw");
    }
    public static void createSOpMap(Map<String,String>map){
        map.put("00000","beq");
        map.put("00001","bne");
        map.put("00010","blt");
        map.put("00011","sw");

    }
    public static void createUOpMap(Map<String,String>map){
        map.put("00000","jal");
        map.put("11111","break");
    }
    public static void generateSType(String instruction){
        String opcode = instruction.substring(25,30);
        String pnemonic = sOpMap.get(opcode);
        String immediate1=instruction.substring(0,7);
        String rs2="x"+Integer.parseInt(instruction.substring(7,12),2);
        String rs1="x"+Integer.parseInt(instruction.substring(12, 17),2);
        String immediate2=instruction.substring(20,25);
        String immediate=immediate1+immediate2;
        int imm=immediate.charAt(0)=='1'?-calculateTwosComplement(immediate):Integer.parseInt(immediate,2);
        String decode="";
        if(pnemonic.equals("sw")){
            decode=String.format("%s %s, %d(%s)",pnemonic,rs1,imm,rs2);
        }
        else{
            decode=String.format("%s %s, %s, #%d",pnemonic,rs1,rs2,imm);
        }
        disassemblyList.add(String.format("%s\t%d\t%s",instruction,currAddress,decode));
        currAddress+=4;

    }
    public static void generateRType(String instruction){
        String opcode = instruction.substring(25,30);
        String pnemonic = rOpMap.get(opcode);
        String rs2="x"+Integer.parseInt(instruction.substring(7,12),2);
        String rs1="x"+Integer.parseInt(instruction.substring(12, 17),2);
        String rd="x"+Integer.parseInt(instruction.substring(20,25),2);
        String decode = pnemonic+" "+rd+", "+rs1+", "+rs2;
        disassemblyList.add(String.format("%s\t%d\t%s",instruction,currAddress,decode));
        currAddress+=4;
    }
    public static void generateIType(String instruction){
        String opcode = instruction.substring(25,30);
        String pnemonic = iOpMap.get(opcode);
        String immediate = instruction.substring(0,12);
        String rs1="x"+Integer.parseInt(instruction.substring(12,17),2);
        String rd="x"+Integer.parseInt(instruction.substring(20,25),2);
        int imm=immediate.charAt(0)=='1'?-calculateTwosComplement(immediate):Integer.parseInt(immediate,2);
        String decode="";
        if(pnemonic.equals("lw")){
            decode=String.format("%s %s, %d(%s)",pnemonic,rd,imm,rs1);
        }
        else{
            decode=String.format("%s %s, %s, #%d",pnemonic,rd,rs1,imm);
        }
        disassemblyList.add(String.format("%s\t%d\t%s",instruction,currAddress,decode));
        currAddress+=4;

    }
    public static void generateUType(String instruction){
        String opcode = instruction.substring(25,30);
        String pnemonic = uOpMap.get(opcode);
        String decode="";
        if(pnemonic.equals("break")){
            decode="break";
            isBreak=true;
        }
        else{
            String immediate = instruction.substring(0,20);
            String rd="x"+Integer.parseInt(instruction.substring(20,25),2);
            int imm=immediate.charAt(0)=='1'?-calculateTwosComplement(immediate):Integer.parseInt(immediate,2);
            decode=String.format("%s %s, #%d",pnemonic,rd,imm);
        }
        disassemblyList.add(String.format("%s\t%d\t%s",instruction,currAddress,decode));
        currAddress+=4;

    }
    public static void generateData(String instruction){
        int data=instruction.charAt(0)=='1'?-calculateTwosComplement(instruction):Integer.parseInt(instruction,2);
        String decode=data+"";
        disassemblyList.add(String.format("%s\t%d\t%s",instruction,currAddress,decode));
        currAddress+=4;
    }
    public static int calculateTwosComplement(String str){
        String oneComp="";
        for(int i=0;i<str.length();++i){
            char bit=str.charAt(i);
            oneComp+=(bit=='0')?"1":"0";
        }
        String b="1";
        int i=oneComp.length()-1,j=0,carry=0;
        StringBuilder sb=new StringBuilder();
        while(i>=0 || j>=0){
            int sum = carry;
            if (j >= 0) sum += b.charAt(j--) - '0';
            if (i >= 0) sum += oneComp.charAt(i--) - '0';
            sb.append(sum % 2);
            carry = sum / 2;
        }
        if (carry != 0) sb.append(carry);
        String result = sb.reverse().toString();
        return Integer.parseInt(result,2);

    }
    public static void generateSimulation(){
        for(int num=0;num<32;++num){
            String key="x"+num;
            regMap.put(key,0);
        }
        initializeDataMap();
        initializeInstrMap();
        int cycleNum=1;
        boolean breakFlag=false;
        while(true){
            String instruction=instrMap.get(currAddress);
            int addr=currAddress;
            String[] str=instruction.split(" ");
            String pnemonic=str[0];
            switch(pnemonic){
                case "add":
                    performAdd(str);
                    break;
                case "sub":
                    performSub(str);
                    break;
                case "and":
                    performAnd(str);
                    break;
                case "or":
                    performOr(str);
                    break;
                case "addi":
                    performAddI(str);
                    break;
                case "andi":
                    performAndI(str);
                    break;
                case "ori":
                    performOrI(str);
                    break;
                case "sll":
                    performSll(str);
                    break;
                case "sra":
                    performSra(str);
                    break;
                case "lw":
                    performLoad(str);
                    break;
                case "sw":
                    performStore(str);
                    break;
                case "beq":
                    checkBeq(str);
                    break;
                case "bne":
                    checkBne(str);
                    break;
                case "blt":
                    checkBlt(str);
                    break;
                case "jal":
                    performJal(str);
                    break;
                case "break":
                    breakFlag=true;
            }
            simulationList.add("--------------------");
            simulationList.add(String.format("Cycle %d:\t%d\t%s",cycleNum,addr,instruction));
            simulationList.add("Registers");
            simulationList.add(String.format("x00:\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d",regMap.get("x0"),regMap.get("x1"),regMap.get("x2"),regMap.get("x3"),regMap.get("x4"),regMap.get("x5"),regMap.get("x6"),regMap.get("x7")));
            simulationList.add(String.format("x08:\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d",regMap.get("x8"),regMap.get("x9"),regMap.get("x10"),regMap.get("x11"),regMap.get("x12"),regMap.get("x13"),regMap.get("x14"),regMap.get("x15")));
            simulationList.add(String.format("x16:\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d",regMap.get("x16"),regMap.get("x17"),regMap.get("x18"),regMap.get("x19"),regMap.get("x20"),regMap.get("x21"),regMap.get("x22"),regMap.get("x23")));
            simulationList.add(String.format("x24:\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d",regMap.get("x24"),regMap.get("x25"),regMap.get("x26"),regMap.get("x27"),regMap.get("x28"),regMap.get("x29"),regMap.get("x30"),regMap.get("x31")));
            simulationList.add("Data");
            int dataAddress=dataStartAddress;
            while(true){
                StringBuilder sb=new StringBuilder();
                int flag=0;
                for(int i=0;i<8;++i){
                    if(dataMap.get(dataAddress)!=null){
                        if(sb.length()==0) sb.append(""+dataAddress+":\t");
                        sb.append(dataMap.get(dataAddress));
                        sb.append("\t");
                        dataAddress+=4;
                    }
                    else{
                        flag=1;
                        break;
                    }
                }
                String s=sb.toString();
                if(!s.equals("")) simulationList.add(s.substring(0,s.length()-1));
                if(flag==1) break;
            }
            cycleNum++;
            if(breakFlag) break;
        }
        writeToFile("simulation.txt",simulationList);

    }
    public static void performAdd(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        String src2=str[3];
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) + (regMap.get(src2)!=null ? regMap.get(src2) : 0));
        currAddress+=4;
    }
    public static void performSub(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        String src2=str[3];
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) - (regMap.get(src2)!=null ? regMap.get(src2) : 0));
        currAddress+=4;
    }
    public static void performAnd(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        String src2=str[3];
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) & (regMap.get(src2)!=null ? regMap.get(src2) : 0));
        currAddress+=4;
    }
    public static void performOr(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        String src2=str[3];
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) | (regMap.get(src2)!=null ? regMap.get(src2) : 0));
        currAddress+=4;
    }
    public static void performAddI(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        int imm=Integer.parseInt(str[3].substring(1));
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) + imm);
        currAddress+=4;
    }
    public static void performAndI(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        int imm=Integer.parseInt(str[3].substring(1));
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) & imm);
        currAddress+=4;
    }
    public static void performOrI(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        int imm=Integer.parseInt(str[3].substring(1));
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) | imm);
        currAddress+=4;
    }
    public static void performSll(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        int imm=Integer.parseInt(str[3].substring(1));
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) << imm);
        currAddress+=4;
    }
    public static void performSra(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(0,str[2].length()-1);
        int imm=Integer.parseInt(str[3].substring(1));
        regMap.put(destKey,(regMap.get(src1)!=null?regMap.get(src1):0) >> imm);
        currAddress+=4;
    }
    public static void performLoad(String[] str){
        String destKey=str[1].substring(0,str[1].length()-1);
        String src1=str[2].substring(str[2].indexOf("(")+1,str[2].length()-1);
        int imm=Integer.parseInt(str[2].substring(0,str[2].indexOf("(")));
        int regValue=regMap.get(src1)!=null?regMap.get(src1):0;
        int result = dataMap.get(imm+regValue)!=null?dataMap.get(imm+regValue):0;
        regMap.put(destKey,result);
        currAddress+=4;
    }
    public static void performStore(String[] str){
        String src=str[1].substring(0,str[1].length()-1);
        int value=regMap.get(src)!=null?regMap.get(src):0;
        String reg=str[2].substring(str[2].indexOf("(")+1,str[2].length()-1);
        int imm=Integer.parseInt(str[2].substring(0,str[2].indexOf("(")));
        int regValue=regMap.get(reg)!=null?regMap.get(reg):0;
        dataMap.put(imm+regValue,value);
        currAddress+=4;
    }
    public static void checkBeq(String[] str){
        String src1=str[1].substring(0,str[1].length()-1);
        String src2=str[2].substring(0,str[2].length()-1);
        int offset=Integer.parseInt(str[3].substring(1));
        int value1=regMap.get(src1)!=null?regMap.get(src1):0;
        int value2=regMap.get(src2)!=null?regMap.get(src2):0;
        if(value1==value2){
            currAddress=currAddress+(offset<<1);
        }
        else currAddress+=4;
    }
    public static void checkBne(String[] str){
        String src1=str[1].substring(0,str[1].length()-1);
        String src2=str[2].substring(0,str[2].length()-1);
        int offset=Integer.parseInt(str[3].substring(1));
        int value1=regMap.get(src1)!=null?regMap.get(src1):0;
        int value2=regMap.get(src2)!=null?regMap.get(src2):0;
        if(value1!=value2){
            currAddress=currAddress+(offset<<1);
        }
        else currAddress+=4;
    }
    public static void checkBlt(String[] str){
        String src1=str[1].substring(0,str[1].length()-1);
        String src2=str[2].substring(0,str[2].length()-1);
        int offset=Integer.parseInt(str[3].substring(1));
        int value1=regMap.get(src1)!=null?regMap.get(src1):0;
        int value2=regMap.get(src2)!=null?regMap.get(src2):0;
        if(value1<value2){
            currAddress=currAddress+(offset<<1);
        }
        else currAddress+=4;
    }
    public static void performJal(String[] str){
        String reg=str[1].substring(0,str[1].length()-1);
        regMap.put(reg,currAddress+4);
        int offset=Integer.parseInt(str[2].substring(1));
        currAddress=currAddress+(offset<<1);
    }
    public static void initializeDataMap(){
        boolean b=false;
        int count=0;
        for(String disassembly:disassemblyList){
            String[] str=disassembly.split("\t");
            if(b){
                int key=Integer.parseInt(str[1]);
                int value=Integer.parseInt(str[2]);
                dataMap.put(key,value);
                if(count==0) dataStartAddress=key;
                count++;
                continue;
            }
            if(str[2].equals("break")){
                b=true;
            }

        }
    }
    public static void initializeInstrMap(){
        for(String disassembly:disassemblyList){
            String[] str=disassembly.split("\t");
            int addr=Integer.parseInt(str[1]);
            if(str[2].equals("break")){
                instrMap.put(addr,str[2]);
                break;
            }
            instrMap.put(addr,str[2]);
        }
    }

 
}
