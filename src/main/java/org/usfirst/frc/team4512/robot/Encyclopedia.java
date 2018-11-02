package org.usfirst.frc.team4512.robot;
public class Encyclopedia{// a referendum of coding methods you may see
    /** Ternary operators
     * 
     Basically, a shortened if loop.
     Ternary operators are usually used to perform basic true/false switches, 
     although they have the capability of a normal if 'control' statement.
     Take this translation for example:

     if(statement){
         int a = b;
     }else{
         int a = 1;
     }

      vv into vv

     int a = (statement)?b:1;

     (statement) -- parentheses for boolean evaluation
     
     ? -- denotes that this is a ternary operator
     
     ?b:1; if (statement) is true, the value(a) equals first term(b), otherwise the second(1)      seen as ?if(true):if(false);
     
     Use this operator if you need to do simple one-line if statements. Anything that needs multiple statements should just use a normal if.
    */


    /** Boolean conditions
     * 
      In if statements or the like, some phrases can be used to replicate
      simple logic gates.

      if(a || b){}
      if 'a' is true, OR 'b' is true evaluate {}

      if(a && b)
      if 'a' is true AND 'b' is true evaluate {}

      if(!a)
      if 'a' is false, evaluate {} (opposite)
    */


    /** Static modifier
     * 
      The 'static' denotation is mainly in usage relating to multiple classes.
      When a field is 'static', it essentially means that that field is similar
      across all classes.
      Consider the following:

      public class Stat{
          public static int count;
          public Stat(){
              count++;
            }
        }
      public class Unstat{
          public int count;
          public Unstat(){
              count++;
            }
        }
      public class Run{
          public static void main(String[] args){
              Stat stat1 = new Stat();
              Stat stat2 = new Stat();
              Stat stat3 = new Stat();

              Unstat unstat1 = new Unstat();
              Unstat unstat2 = new Unstat();
              Unstat unstat3 = new Unstat();

              System.out.println(stat1.count);
              System.out.println(stat2.count);
              System.out.println(stat3.count);
              System.out.println();
              System.out.println(unstat1.count);
              System.out.println(unstat2.count);
              System.out.println(unstat3.count);
          }
      }
      
      This program would output:
      >1
      >2
      >3
      >
      >1
      >1
      >1

      In this case, the static field 'count' in Stat instances all pointed to the same memory,
      so increasing it by one after adding 3 instances increased it by a total of 3.
      The Unstat instances did not have static fields, so different instances' fields were seperate
      and not similar.
    */
}