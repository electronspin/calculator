package calculator;

import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Equation eq = new Equation();

        while (true) {
            String line = scanner.nextLine();
            if ("".equals(line)) {
                continue;
            }
            if ("/exit".equals(line)) {
                System.out.println("Bye!");
                break;
            }
            if ("/help".equals(line)) {
                System.out.println("The program calculates equation");
                continue;
            }
            if (line.matches("^/.*")) {
                System.out.println("Unknown command");
                continue;             
            }

            Token token = new Token(line);
            if (token.errFlag) {
                errout(token.getErrCode());
                continue;
            }
            RPN rpn = new RPN(token.get());
//            System.out.println(rpn.getString());
            eq.calculate(rpn.get());
            if (eq.errFlag) {
                errout(eq.getErrCode());
                continue;
            }
            eq.output();
//            eq.printVariables();
        }

        scanner.close();
    }

    static void errout(ErrCode e) {
        String msg = "";
        switch (e) {
            case IDENTITY: 
                 msg = "Invalid identifier";
                 break;
            case ASSIGNMENT:
                msg = "Invalid assignment";
                break;
            case UNKNOWN:
                msg = "Unknown variable";
                break;
            case EXPRESSION:
                msg = "Invalid expression";
                break;
        }
        System.out.println(msg);
    }
}

enum ErrCode {
    IDENTITY,
    ASSIGNMENT,
    UNKNOWN,
    EXPRESSION
}

class Token {
    String[] tokens;
    boolean errFlag = false;
    String errToken = "";
    int eqcounter = 0;
    int lpcounter = 0;
    int rpcounter = 0;

    Token(String line) {
        if (line.matches(".*\\*\\*+.*")) {
            errFlag = true;
            return;
        }

        if (line.matches(".*\\/\\/+.*")) {
            errFlag = true;
            return;
        }

        line = line.replaceAll("--", "+")
                   .replaceAll("\\++", "+")           
                   .replaceAll("\\+-", "-")
                   .replaceAll("=", " = ")
                   .replaceAll("\\+", " + ")
                   .replaceAll("-", " - ")
                   .replaceAll("\\*", " * ")
                   .replaceAll("/", " / ")
                   .replaceAll("\\(", " ( ")
                   .replaceAll("\\)", " ) ");
                   
        ArrayList<String> list = new ArrayList<>();
        String[] strs = line.split(" ");
    
        for (String str: strs) {
            if ("".equals(str)) {
                continue;
            }

            String type = "";
            if (str.matches("[a-zA-Z]*")) {
                type = "v";
            } else if (str.matches("[0-9]*")) {
                type = "n";
            } else if ("*".equals(str) || "/".equals(str)) {
                type = "*";
            } else if ("+".equals(str) || "-".equals(str)) {
                type = "+";
            } else if ("=".equals(str)) {
                type = "=";
                eqcounter++;
            } else if ("(".equals(str)) {
                type = "(";
                lpcounter++;
            } else if (")".equals(str)) {
                type = ")";
                rpcounter++;
            } else {
                type = "e";
           
                if (!errFlag) {
                    errFlag = true;
                    errToken = str;
                }
            }
    
            list.add(type + ":" + str);
        }

        if (eqcounter == 1) {
            String[] tv = list.get(0).split(":");
            String type = tv[0];
            String value = tv[1];
            if ("v".equals(type)) {
                list.set(0, "f:" + value);
            }
        }
    
        tokens = list.toArray(new String[list.size()]);

        if (eqcounter > 1) {
            errFlag = true;
        }

        if (lpcounter != rpcounter) {
            errFlag = true;
        }
    }

    String[] get() {
        return this.tokens;
    }  

    ErrCode getErrCode() {
        if (eqcounter > 0) {
            if (tokens[0].matches("e:.+")) {
                return ErrCode.IDENTITY;
            }
            return ErrCode.ASSIGNMENT;     
        }

        return ErrCode.EXPRESSION;
    }

    String getString() {
        return this.tokens.toString();
    } 
}

class Stack {
    Deque<String> stack;

    Stack() {
        stack = new ArrayDeque<>();
    }

    void push(String item) {
        stack.offerLast(item);
    }

    String pop() {
        return stack.pollLast();
    }

    String peek() {
        return stack.peekLast();
    }

    boolean isEmpty() {
        return stack.isEmpty();
    }
}

class RPN {
    String[] rpns;
    Stack stack;
    List<String> list;

    RPN(String[] tokens) {
        stack = new Stack();
        list = new ArrayList<>();
        for (String token: tokens) {
            String[] tv = token.split(":");
            String tokenType = tv[0];

            if ("v".equals(tokenType) || "n".equals(tokenType) || "f".equals(tokenType)) {
                list.add(token);
                continue;
            }

            if (stack.isEmpty()) {
                stack.push(token);
                continue;
            }

            String stackToken = stack.peek();
            String[] stv = stackToken.split(":");
            String stackType = stv[0];

            if ("(".equals(stackType)) {
                stack.push(token);
                continue;
            }
          
            if ("+".equals(tokenType)) {
                if ("=".equals(stackType)) {
                    stack.push(token);
                } else {
                    while ("*".equals(stackType) || "+".equals(stackType)) {
                        stack.pop();
                        list.add(stackToken);
                        if (stack.isEmpty()) {
                            break;
                        }
                        stackToken = stack.peek();
                        stv = stackToken.split(":");
                        stackType = stv[0];
                    }
                    stack.push(token);
                }
                continue;
            }

            if ("*".equals(tokenType)) {
                if ("+".equals(stackType) || "=".equals(stackType)) {
                    stack.push(token);
                } else {
                    while ("*".equals(stackType)) {
                        stack.pop();
                        list.add(stackToken);
                        if (stack.isEmpty()) {
                            break;
                        }
                        stackToken = stack.peek();
                        stv = stackToken.split(":");
                        stackType = stv[0];
                    }
                    stack.push(token);
                }
                continue;
            }

            if ("(".equals(tokenType)) {
                stack.push(token);
                continue;
            }

            if (")".equals(tokenType)) {
                while (!"(".equals(stackType)) {
                    stack.pop();
                    list.add(stackToken);
                    if (stack.isEmpty()) {
                        break;
                    }
                    stackToken = stack.peek();
                    stv = stackToken.split(":");
                    stackType = stv[0];
                }
                if (!stack.isEmpty()) {
                    stack.pop();
                }
                continue;
            }
        }

        while (!stack.isEmpty()) {
            list.add(stack.pop());
        }
        rpns = list.toArray(new String[list.size()]);
    }

    String[] get() {
        return this.rpns;
    }

    String getString() {
        String str = "";
        for (String rpn: rpns) {
            String[] strs = rpn.split(":");
            str += strs[1];
        }

        return str;
    } 
}

class Equation {
    HashMap<String, String> variables = new HashMap<>();
    boolean errFlag = false;
    boolean unknownFlag = false;
    Stack stack;

    void calculate(String[] rpns) {
        errFlag = false;
        unknownFlag = false;
        stack = new Stack();
        
        for (String rpn: rpns) {
            String[] strs = rpn.split(":");
            String type = strs[0];
            String value = strs[1];
            String operand;

            if ("n".equals(type)) {
                stack.push(value);
                continue;
            }

            if ("f".equals(type)) {
                stack.push(value);
                continue;
            }

            if ("v".equals(type)) {
                if (variables.containsKey(value)) {
                    operand = variables.get(value);
                    stack.push(operand);
                } else {
                    errFlag = true;
                    unknownFlag = true;
                }
                continue;
            }

            String result = "";
            String operand2 = stack.pop();
            if (stack.isEmpty() || !"=".equals(type) && stack.peek().matches("[a-zA-Z]*")) {
                result = unary(operand2, value);
                stack.push(result);
                continue;
            }

            String operand1 = stack.pop();
            switch (value) {
                case "+":
                case "-":
                case "*":
                case "/":
                     result = binary(operand1, operand2, value);
                     stack.push(result);
                     break;
                case "=":
                     variables.put(operand1, operand2);
                     break;
            }
        }
    }

    String binary(String str1, String str2, String operator) {
        BigInteger operand1 = new BigInteger(str1);    
        BigInteger operand2 = new BigInteger(str2);
        BigInteger result = BigInteger.ZERO;    
        switch (operator) {
            case "+":
                 result = operand1.add(operand2);
                 break;
            case "-":
                 result = operand1.subtract(operand2);
                 break;
            case "*":
                 result = operand1.multiply(operand2);
                 break;     
            case "/":
                 result = operand1.divide(operand2);
                 break; 
        }
        return String.valueOf(result);
    }

    String unary(String operand, String operator) {
        String result = "";
        switch (operator) {
            case "+":
                 result = operand;
                 break;
            case "-":
                 result = "-" + operand;
                 break;
        }

        return result;
    }

    ErrCode getErrCode() {
        if (unknownFlag) {
            return ErrCode.UNKNOWN;
        }
        return ErrCode.EXPRESSION;
    }
    void output() {
        if (stack.isEmpty()) {
            return;
        }
        System.out.println(stack.pop());       
    }

    String printVariables() {
        String str = "";
        for (var entry: variables.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        return str;
    }
}