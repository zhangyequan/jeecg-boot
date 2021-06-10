package org.jeecg.modules.songdd.controller;

public class main{
//    public static void main(String[]args){
//
//          print();
//    }

    private static int count(int i,int j){
        int sum = 0;
        for(int o= i;o <= j;o++){
            sum = sum + o;
        }

        return sum;
    }

    private static void print(){
        int a = count(0,100);
        System.out.println(a);
        int b = count(101,115);
        System.out.println(b);
    }
}

